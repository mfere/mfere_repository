package com.analyzer.learner;

import com.analyzer.constants.*;
import com.analyzer.enricher.strategy.StrategyFactory;
import com.analyzer.learner.stopcondition.StopCondition;
import com.analyzer.learner.stopcondition.StopConditionFactory;
import com.analyzer.model.FxLearnData;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.reader.ReadRequestForm;
import com.analyzer.reader.ReaderUtil;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.AbstractDataSetNormalizer;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;
import java.time.Instant;
import java.util.*;

@RestController
public class LearnerController {

    private static final Logger log = LoggerFactory.getLogger(LearnerController.class);

    private final RawCandlestickRepository rawCandlestickRepository;
    private final UIServer uiServer;
    private String trainedNetworksPath;

    LearnerController(RawCandlestickRepository rawCandlestickRepository,
                      UIServer uiServer,
                      @Value("${trained.network.path}") String trainedNetworksPath) {
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.uiServer = uiServer;
        this.trainedNetworksPath = trainedNetworksPath;
    }

    @RequestMapping(value = "/learn", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> learn(
            @Valid @RequestBody LearnerRequestForm learnerRequestForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        File tmpTrainFile;
        File tmpValidateFile;
        File tmpTestFile;
        try {
            GranularityType granularity = GranularityType.valueOf(
                    learnerRequestForm.getGranularity());
            InstrumentValue instrument = InstrumentValue.valueOf(
                    learnerRequestForm.getInstrument());
            StrategyType strategyType = StrategyType.valueOf(
                    learnerRequestForm.getStrategy());
            List<IndicatorType> indicatorTypes = new ArrayList<>();
            for (String indicatorName : learnerRequestForm.getIndicators()) {
                indicatorTypes.add(IndicatorType.valueOf(indicatorName));
            }
            List<InstrumentValue> watchInstruments = new ArrayList<>();
            for (String watchInstrumentName : learnerRequestForm.getWatchInstruments()) {
                watchInstruments.add(InstrumentValue.valueOf(watchInstrumentName));
            }

            // TRAIN DATA CREATION
            tmpTrainFile = File.createTempFile("train_"+new Date().getTime(), ".csv");
            int trainSize = writeToCsv(
                    tmpTrainFile,
                    ReaderUtil.parse(learnerRequestForm.getTrainFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    ReaderUtil.parse(learnerRequestForm.getTrainToDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity, instrument, strategyType, indicatorTypes, watchInstruments,
                    learnerRequestForm.getPastValuesNumber(), learnerRequestForm.isShuffleData());
            RecordReader rrTrain = new CSVRecordReader();
            rrTrain.initialize(new FileSplit(new File(tmpTrainFile.getAbsolutePath())));
            log.info("saved train temporary file: "+tmpTrainFile.getAbsolutePath());

            // VALIDATION DATA CREATION
            tmpValidateFile = File.createTempFile("valid_"+new Date().getTime(), ".csv");
            int validationSize = writeToCsv(
                    tmpValidateFile,
                    ReaderUtil.parse(learnerRequestForm.getValidateFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    ReaderUtil.parse(learnerRequestForm.getValidateToDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity, instrument, strategyType, indicatorTypes, watchInstruments,
                    learnerRequestForm.getPastValuesNumber(), false);
            RecordReader rrValidate = new CSVRecordReader();
            rrValidate.initialize(new FileSplit(new File(tmpValidateFile.getAbsolutePath())));
            log.info("saved validation temporary file: "+tmpValidateFile.getAbsolutePath());

            // TEST DATA CREATION
            tmpTestFile = File.createTempFile("test_"+new Date().getTime(), ".csv");
            int testSize = writeToCsv(
                    tmpTestFile,
                    ReaderUtil.parse(learnerRequestForm.getTestFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    ReaderUtil.parse(learnerRequestForm.getTestToDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity, instrument, strategyType, indicatorTypes, watchInstruments,
                    learnerRequestForm.getPastValuesNumber(), false);
            RecordReader rrTest = new CSVRecordReader();
            rrTest.initialize(new FileSplit(new File(tmpTestFile.getAbsolutePath())));
            log.info("saved test temporary file: "+tmpTestFile.getAbsolutePath());

            int numOutputs = strategyType.getLabelNumber();
            int batchNumber=learnerRequestForm.getBatchNumber();
            int trainBatchSize=trainSize/batchNumber;
            int numInputs = learnerRequestForm.getIndicators().size() * watchInstruments.size() * (1 + learnerRequestForm.getPastValuesNumber());
            log.info("batchNumber: "+ batchNumber);
            log.info("trainBatchSize: "+ trainBatchSize);
            log.info("validationSize: "+ validationSize);
            log.info("testBatchSize: "+ testSize);

            // Create train, validation and test iterators
            DataSetIterator trainIterator = new RecordReaderDataSetIterator(
                    rrTrain, trainBatchSize, 0, strategyType.getLabelNumber());
            DataSetIterator validateIterator = new RecordReaderDataSetIterator(
                    rrValidate, validationSize, 0, strategyType.getLabelNumber());
            DataSetIterator testIterator = new RecordReaderDataSetIterator(
                    rrTest, testSize, 0, strategyType.getLabelNumber());

            // If needed, train normalizer using train data
            AbstractDataSetNormalizer normalizer = NormalizerFactory.getNormalizer(
                    NormalizerType.valueOf(learnerRequestForm.getNormalizer()));
            if (normalizer != null) {
                // Train normalizer
                normalizer.fitLabel(true);
                normalizer.fit(trainIterator);
                // Normalize train, validation and test iterators
                trainIterator.setPreProcessor(normalizer);
                validateIterator.setPreProcessor(normalizer);
                testIterator.setPreProcessor(normalizer);
            }

            MultiLayerConfiguration conf = createMultiLayerConfiguration(learnerRequestForm, numInputs, numOutputs);

            MultiLayerNetwork model = new MultiLayerNetwork(conf);
            model.init();

            StatsStorage statsStorage = new InMemoryStatsStorage();
            uiServer.attach(statsStorage);
            model.setListeners(new StatsListener(statsStorage), new ScoreIterationListener(1000),
                    new ValidationScoreIterationListener(1000, numOutputs, validateIterator));

            StopCondition stopCondition = StopConditionFactory.getStopCondition(
                    StopConditionType.valueOf(learnerRequestForm.getStopCondition()), model,
                    trainIterator, validateIterator, numOutputs);
            if (stopCondition == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            int seed = 1000;
            do {
                trainIterator.reset();
                while(trainIterator.hasNext())
                {
                    DataSet next = trainIterator.next();
                    if (learnerRequestForm.isShuffleData()) {
                        next.shuffle(seed);
                    }
                    model.fit(next);
                }
            } while (!stopCondition.isConditionMet());

            model = stopCondition.getBestConfiguration();
            log.info("Evaluate train model....");
            Evaluation eval = evaluateModel(trainIterator, model, numOutputs);
            log.info(eval.stats());
            log.info("Evaluate validation models...");
            eval = evaluateModel(validateIterator, model, numOutputs);
            log.info(eval.stats());
            log.info("Evaluate test model....");
            eval = evaluateModel(testIterator, model, numOutputs);
            log.info(eval.stats());

            // Save the models
            int score = (int) (eval.f1()*10000);
            String filePath = trainedNetworksPath+"/" + (new Date().getTime())+"_"+learnerRequestForm.getName()+"_"+score;
            ModelSerializer.writeModel(model, filePath + ".model",true);
            if (normalizer != null) {
                // Save the normalizer
                // Now we want to save the normalizer to a binary file. For doing this, one can use the NormalizerSerializer.
                NormalizerSerializer serializer = NormalizerSerializer.getDefault();
                serializer.write(normalizer, filePath + ".normalizer");
            }

            // Save the used strategyType
            FileOutputStream fos = new FileOutputStream(filePath + ".str");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(StrategyFactory.getStrategy(strategyType));
            oos.close();
            fos.close();

            // Save the used indicators
            fos = new FileOutputStream(filePath + ".ind");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(learnerRequestForm.getIndicators());
            oos.close();
            fos.close();

            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MultiLayerConfiguration createMultiLayerConfiguration(
            LearnerRequestForm learnerRequestForm, int numInputs, int numOutputs) {
        String jsonConfiguration = learnerRequestForm.getNetworkConfiguration();

        MultiLayerConfiguration conf = MultiLayerConfiguration.fromJson(jsonConfiguration);

        NeuralNetConfiguration configuration = conf.getConfs().get(0);
        Layer layer = configuration.getLayer();
        if (layer instanceof DenseLayer) {
            DenseLayer denseLayer = (DenseLayer) layer;
            denseLayer.setNIn(numInputs);
        }
        configuration = conf.getConfs().get(conf.getConfs().size() - 1);
        layer = configuration.getLayer();
        if (layer instanceof OutputLayer) {
            OutputLayer outputLayer = (OutputLayer) layer;
            outputLayer.setNOut(numOutputs);
        }
        for (NeuralNetConfiguration aConfiguration : conf.getConfs()){
            layer = aConfiguration.getLayer();
            if (layer instanceof DenseLayer) {
                DenseLayer denseLayer = (DenseLayer)layer;
                denseLayer.setLearningRate(learnerRequestForm.getLearningRate());
            } else if (layer instanceof OutputLayer) {
                OutputLayer outputLayer = (OutputLayer) layer;
                outputLayer.setLearningRate(learnerRequestForm.getLearningRate());
            }
        }
        return conf;
    }

    private int writeToCsv(File file, Instant fromDate, Instant toDate,
                           GranularityType granularity, InstrumentValue instrument,
                           StrategyType strategy, List<IndicatorType> indicators,
                           List<InstrumentValue> watchInstruments,
                           Integer pastValuesNumber,
                           boolean doShuffle) throws Exception {
        FileWriter writer = null;
        PrintWriter printWriter = null;
        int size = 0;
        try {
            writer = new FileWriter(file);
            printWriter = new PrintWriter(writer);

            log.info("creating temporary file: "+file.getAbsolutePath());
            List<FxLearnData> fxLearnDataList = new ArrayList<>();
            RawCandlestick mainInstrumentRawCandlestick = findRawCandleStick(
                    fromDate,
                    granularity,
                    instrument);

            if (mainInstrumentRawCandlestick == null) {
                throw new Exception(
                        "Could not find candlestick for date " + fromDate +
                                ", instrument: " + instrument +
                                ", granularity: " + granularity);
            }

            Map<InstrumentValue, RawCandlestick> watchRawCandlesticks = new LinkedHashMap<>();
            // We should always watch the instrument we are going to perform action on
            watchRawCandlesticks.put(instrument, mainInstrumentRawCandlestick);

            // Add other instruments we might want to watch when taking a decision
            if (watchInstruments != null && watchInstruments.size() > 0) {
                for (InstrumentValue watchInstrument : watchInstruments) {
                    if (watchInstrument != instrument) {
                        RawCandlestick watchRawCandlestick = findRawCandleStick(
                                fromDate,
                                granularity,
                                watchInstrument);
                        if (watchRawCandlestick == null) {
                            throw new Exception(
                                    "Could not find candlestick for date " + fromDate +
                                    ", instrument: " + watchInstrument +
                                    ", granularity: " + granularity);
                        }
                        watchRawCandlesticks.put(watchInstrument, watchRawCandlestick);
                    }
                }
            }

            // We might also want some historical data to be added to all the instruments we are watching
            Map<InstrumentValue, LinkedList<RawCandlestick>> watchPreviousCandleSticks = new LinkedHashMap<>();
            for (InstrumentValue watchedInstrument : watchRawCandlesticks.keySet()) {
                watchPreviousCandleSticks.put(
                        watchedInstrument,
                        getPreviousCandleSticks(
                        watchRawCandlesticks.get(watchedInstrument),
                        granularity,
                        watchedInstrument,
                        pastValuesNumber));
            }

            // Combine all the data (all the watched instruments + historical data of each of them)
            List<RawCandlestick> allDataToWatch = new ArrayList<>();
            for (LinkedList<RawCandlestick> watchPreviousCandleStick : watchPreviousCandleSticks.values()) {
                allDataToWatch.addAll(watchPreviousCandleStick);
            }

            // This is all the data we need to learn how to make a decision in a single instant
            FxLearnData fxLearnData = new FxLearnData(mainInstrumentRawCandlestick, allDataToWatch, strategy, indicators);

            fxLearnDataList.add(fxLearnData);

            do {
                // Move forward to next instant, but exit if no next or is after the end date
                Instant nextDateTime = mainInstrumentRawCandlestick.getNextDateTime();
                if (nextDateTime != null &&
                        !nextDateTime.isAfter(toDate)) {
                    allDataToWatch = new ArrayList<>();

                    // Update the historical data list
                    for (InstrumentValue watchedInstrument : watchPreviousCandleSticks.keySet()) {
                        LinkedList<RawCandlestick> watchPreviousCandleStick = watchPreviousCandleSticks.get(watchedInstrument);
                        // Add new data in front if available, otherwise use previous date data
                        try {
                            RawCandlestick nextWatchRawCandlestick = findRawCandleStick(
                                    nextDateTime,
                                    granularity,
                                    watchedInstrument);
                            watchPreviousCandleStick.addFirst(nextWatchRawCandlestick);
                            // Drop oldest historical data
                            watchPreviousCandleStick.removeLast();
                        } catch (Exception e) {
                            log.debug("Using previous date values for date " + nextDateTime +
                                    ", instrument: " + watchedInstrument +
                                    ", granularity: " + granularity);
                        }

                        // Combine all the data (all the watched instruments + historical data of each of them)
                        allDataToWatch.addAll(watchPreviousCandleStick);
                    }

                    // Update also the main instrument that is used for the strategy
                    mainInstrumentRawCandlestick = watchPreviousCandleSticks.get(instrument).getFirst();

                    // This is all the data we need to learn how to make a decision in the next single instant
                    fxLearnData = new FxLearnData(mainInstrumentRawCandlestick, allDataToWatch, strategy, indicators);

                    fxLearnDataList.add(fxLearnData);
                } else {
                    mainInstrumentRawCandlestick = null;
                }
            } while (mainInstrumentRawCandlestick != null);

            if (doShuffle) {
                Collections.shuffle(fxLearnDataList);
            }
            for (FxLearnData fxLearnDataRecord : fxLearnDataList) {
                printWriter.println(fxLearnDataRecord.toCsvLine());
                printWriter.flush();
                size++;
            }

        } catch (IOException e) {
            log.warn("Could not create file: "+e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }

    private LinkedList<RawCandlestick> getPreviousCandleSticks(
            RawCandlestick startCandlestick,
            GranularityType granularity,
            InstrumentValue instrument, int pastValuesNumber) throws Exception {
        LinkedList<RawCandlestick> prevRawCandlesticks = new LinkedList<>();
        prevRawCandlesticks.addFirst(startCandlestick);
        while (pastValuesNumber > 0) {
            if (startCandlestick.getPrevDateTime() == null) {
                throw new Exception("This candle has missing previous: "
                        + startCandlestick.getRawCandlestickKey().getDateTime().toString());
            }
            startCandlestick = rawCandlestickRepository.findOne(
                    startCandlestick.getPrevDateTime(),
                    granularity,
                    instrument);
            prevRawCandlesticks.addLast(startCandlestick);
            pastValuesNumber --;

        }
        return prevRawCandlesticks;
    }

    private RawCandlestick findRawCandleStick(Instant date, GranularityType granularity, InstrumentValue instrument)
        throws Exception{
        RawCandlestick rawCandlestick = rawCandlestickRepository.findOne(
                date,
                granularity,
                instrument);
        if (rawCandlestick == null) {
            throw new Exception(
                    "Could not find candlestick for date " + date +
                            ", instrument: " + instrument +
                            ", granularity: " + granularity);
        }
        return rawCandlestick;
    }

    public static Evaluation evaluateModel(DataSetIterator dataSetIterator, MultiLayerNetwork model, int numOutputs) {
        Evaluation eval = new Evaluation(numOutputs);
        dataSetIterator.reset();
        while(dataSetIterator.hasNext()){
            DataSet t = dataSetIterator.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray predicted = model.output(features,false);
            eval.eval(labels, predicted);
        }
        return eval;
    }

}
