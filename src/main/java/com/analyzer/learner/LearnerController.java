package com.analyzer.learner;

import com.analyzer.constants.*;
import com.analyzer.enricher.strategy.StrategyFactory;
import com.analyzer.learner.stopcondition.StopCondition;
import com.analyzer.learner.stopcondition.StopConditionFactory;
import com.analyzer.model.FxIndicator;
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
import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

            // TRAIN DATA CREATION
            tmpTrainFile = File.createTempFile("train_"+new Date().getTime(), ".csv");
            int trainSize = writeToCsv(
                    tmpTrainFile,
                    ReaderUtil.parse(learnerRequestForm.getTrainFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    ReaderUtil.parse(learnerRequestForm.getTrainToDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity, instrument, strategyType, indicatorTypes, true);
            RecordReader rrTrain = new CSVRecordReader();
            rrTrain.initialize(new FileSplit(new File(tmpTrainFile.getAbsolutePath())));
            log.info("saved train temporary file: "+tmpTrainFile.getAbsolutePath());

            // VALIDATION DATA CREATION
            tmpValidateFile = File.createTempFile("valid_"+new Date().getTime(), ".csv");
            int validationSize = writeToCsv(
                    tmpValidateFile,
                    ReaderUtil.parse(learnerRequestForm.getValidateFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    ReaderUtil.parse(learnerRequestForm.getValidateToDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity, instrument, strategyType, indicatorTypes, false);
            RecordReader rrValidate = new CSVRecordReader();
            rrValidate.initialize(new FileSplit(new File(tmpValidateFile.getAbsolutePath())));
            log.info("saved validation temporary file: "+tmpValidateFile.getAbsolutePath());

            // TEST DATA CREATION
            tmpTestFile = File.createTempFile("test_"+new Date().getTime(), ".csv");
            int testSize = writeToCsv(
                    tmpTestFile,
                    ReaderUtil.parse(learnerRequestForm.getTestFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    ReaderUtil.parse(learnerRequestForm.getTestToDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity, instrument, strategyType, indicatorTypes, false);
            RecordReader rrTest = new CSVRecordReader();
            rrTest.initialize(new FileSplit(new File(tmpTestFile.getAbsolutePath())));
            log.info("saved test temporary file: "+tmpTestFile.getAbsolutePath());

            int numOutputs = strategyType.getLabelNumber();
            int batchNumber=learnerRequestForm.getBatchNumber();
            int trainBatchSize=trainSize/batchNumber;
            int numInputs = learnerRequestForm.getIndicators().size();
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
                    new TestScoreIterationListener(1000, numOutputs, validateIterator));

            StopCondition stopCondition = StopConditionFactory.getStopCondition(
                    StopConditionType.valueOf(learnerRequestForm.getStopCondition()), model,
                    trainIterator, validateIterator, numOutputs);
            if (stopCondition == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            do {
                trainIterator.reset();
                while(trainIterator.hasNext())
                {
                    DataSet next = trainIterator.next();
                    next.shuffle();
                    model.fit(next);
                }
            } while (!stopCondition.isConditionMet());

            model = stopCondition.getBestConfiguration();
            log.info("Evaluate train model....");
            evaluateModel(testIterator, model, numOutputs);
            log.info("Evaluate validation models...");
            evaluateModel(validateIterator, model, numOutputs);
            log.info("Evaluate test model....");
            Evaluation testEval = evaluateModel(testIterator, model, numOutputs);

            // Save the models
            int score = (int) (testEval.f1()*10000);
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
                             boolean doShuffle) throws Exception {
        FileWriter writer = null;
        PrintWriter printWriter = null;
        int size = 0;
        try {
            writer = new FileWriter(file);
            printWriter = new PrintWriter(writer);

            log.info("saved train temporary file: "+file.getAbsolutePath());
            List<RawCandlestick> rawCandleSticks = new ArrayList<>();
            RawCandlestick rawCandlestick = rawCandlestickRepository.findOne(
                    fromDate,
                    granularity,
                    instrument);
            do {
                rawCandleSticks.add(rawCandlestick);
                if (rawCandlestick.getNextDateTime() != null &&
                        !rawCandlestick.getNextDateTime().isAfter(toDate)) {
                    rawCandlestick = rawCandlestickRepository.findOne(
                            rawCandlestick.getNextDateTime(),
                            granularity,
                            instrument);
                } else {
                    rawCandlestick = null;
                }
            } while (rawCandlestick != null);


            if (doShuffle) {
                Collections.shuffle(rawCandleSticks);
            }
            for (RawCandlestick shuffledCandlestick : rawCandleSticks) {
                printWriter.println(shuffledCandlestick.toCsvLine(strategy, indicators));
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
        log.info(eval.stats());
        return eval;
    }

}
