package com.analyzer.learner;

import com.analyzer.constants.*;
import com.analyzer.learner.stopcondition.StopCondition;
import com.analyzer.learner.stopcondition.StopConditionFactory;
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
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class LearnerController {

    private static final Logger log = LoggerFactory.getLogger(LearnerController.class);

    private final RawCandlestickRepository rawCandlestickRepository;
    private final UIServer uiServer;

    LearnerController(RawCandlestickRepository rawCandlestickRepository,
                      UIServer uiServer) {
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.uiServer = uiServer;
    }

    @RequestMapping(value = "/learn", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> read(
            @Valid @RequestBody LearnerRequestForm learnerRequestForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        RawCandlestick rawCandlestick;
        File tmpTrainFile;
        File tmpTestFile;
        FileWriter writer = null;
        PrintWriter printWriter = null;
        try {
            GranularityValue granularity = GranularityValue.getGranularityValue(
                    learnerRequestForm.getGranularity());
            if (granularity == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            InstrumentValue instrument = InstrumentValue.getInstrumentValue(
                    learnerRequestForm.getInstrument());
            if (instrument == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            RewardFunctionValue rewardFunction = RewardFunctionValue.getRewardFunctionValue(
                    learnerRequestForm.getRewardFunction());
            if (rewardFunction == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // TRAIN DATA CREATION
            rawCandlestick = rawCandlestickRepository.findOne(
                    ReaderUtil.parse(learnerRequestForm.getTrainFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity,
                    instrument);

            //BufferedWriter writer = new BufferedWriter(new FileWriter("test.csv"));
            tmpTrainFile = File.createTempFile("train_"+new Date().getTime(), ".csv");
            writer = new FileWriter(tmpTrainFile);
            printWriter = new PrintWriter(writer);

            int trainSize = 0;
            while (rawCandlestick.getNextDateTime() != null &&
                    !rawCandlestick.getNextDateTime().isAfter(
                            ReaderUtil.parse(learnerRequestForm.getTrainToDate(),ReadRequestForm.DATE_TIME_PATTERN)
                    )){
                rawCandlestick = writeCsvFile(learnerRequestForm, rawCandlestick, printWriter, granularity, instrument);
                trainSize++;
            }
            log.info("saved train temporary file: "+tmpTrainFile.getAbsolutePath());

            // TEST DATA CREATION
            rawCandlestick = rawCandlestickRepository.findOne(
                    ReaderUtil.parse(learnerRequestForm.getTestFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity,
                    instrument);

            //BufferedWriter writer = new BufferedWriter(new FileWriter("test.csv"));
            tmpTestFile = File.createTempFile("test_"+new Date().getTime(), ".csv");
            writer = new FileWriter(tmpTestFile);
            printWriter = new PrintWriter(writer);

            int testSize = 0;
            while (rawCandlestick.getNextDateTime() != null &&
                    !rawCandlestick.getNextDateTime().isAfter(
                            ReaderUtil.parse(learnerRequestForm.getTestToDate(),ReadRequestForm.DATE_TIME_PATTERN)
                    )){
                rawCandlestick = writeCsvFile(learnerRequestForm, rawCandlestick, printWriter, granularity, instrument);
                testSize++;
            }

            log.info("saved test temporary file: "+tmpTestFile.getAbsolutePath());

            int numInputs = learnerRequestForm.getIndicators().size();
            if (learnerRequestForm.getTestConvergance()) {
                numInputs++;
            }
            int numOutputs = rewardFunction.getLabelNumber();
            int batchNumber=50;
            int trainBatchSize=trainSize/batchNumber;
            int testBatchSize=testSize/batchNumber;
            log.info("batchNumber: "+ batchNumber);
            log.info("trainBatchSize: "+ trainBatchSize);
            log.info("testBatchSize: "+ testBatchSize);

            RecordReader rrTest = new CSVRecordReader();
            rrTest.initialize(new FileSplit(new File(tmpTestFile.getAbsolutePath())));

            DataSetIterator testIterator = new RecordReaderDataSetIterator(
                    rrTest, testBatchSize, 0, rewardFunction.getLabelNumber());

            RecordReader rrTrain = new CSVRecordReader();
            rrTrain.initialize(new FileSplit(new File(tmpTrainFile.getAbsolutePath())));

            RecordReader rrScoreTrain = new CSVRecordReader();
            rrScoreTrain.initialize(new FileSplit(new File(tmpTrainFile.getAbsolutePath())));


            RecordReader rrScoreTest = new CSVRecordReader();
            rrScoreTest.initialize(new FileSplit(new File(tmpTestFile.getAbsolutePath())));


            DataSetIterator trainIterator = new RecordReaderDataSetIterator(
                    rrTrain, trainBatchSize, 0, rewardFunction.getLabelNumber());

            DataSetIterator testScoreIterator = new RecordReaderDataSetIterator(
                    rrScoreTest, testBatchSize, 0, rewardFunction.getLabelNumber());

            DataSetIterator trainScoreIterator = new RecordReaderDataSetIterator(
                    rrScoreTrain, trainBatchSize, 0, rewardFunction.getLabelNumber());

            // Normalize train data in range 0-1 for better learning
            NormalizerStandardize testNormalizer = new NormalizerStandardize();
            //NormalizerMinMaxScaler testNormalizer = new NormalizerMinMaxScaler();
            testNormalizer.fitLabel(true);
            testNormalizer.fit(testIterator);
            testIterator.setPreProcessor(testNormalizer);

            // Normalize test data in range 0-1 for more accurate scoring
            //NormalizerStandardize trainNormalizer = new NormalizerStandardize();
            NormalizerMinMaxScaler trainNormalizer = new NormalizerMinMaxScaler();
            trainNormalizer.fitLabel(true);
            trainNormalizer.fit(trainIterator);
            trainIterator.setPreProcessor(trainNormalizer);

            MultiLayerConfiguration conf = createMultiLayerConfiguration(learnerRequestForm, numInputs, numOutputs);

            MultiLayerNetwork model = new MultiLayerNetwork(conf);
            model.init();

            StatsStorage statsStorage = new InMemoryStatsStorage();
            uiServer.attach(statsStorage);
            model.setListeners(new StatsListener(statsStorage));
            model.setListeners(new ScoreIterationListener(1000));

            StopCondition stopCondition = StopConditionFactory.getStopCondition(
                    StopConditionValue.valueOf(learnerRequestForm.getStopCondition()), model,
                    trainScoreIterator, testScoreIterator);
            if (stopCondition == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            do {
                while(trainIterator.hasNext())
                {
                    model.fit(trainIterator.next());
                }
                trainIterator.reset();
            } while (!stopCondition.isConditionMet());

            //ModelSerializer.writeModel(model,this.filePath,true);
            model = stopCondition.getBestConfiguration();
            log.info("Evaluate train model....");
            Evaluation eval = new Evaluation(numOutputs);
            while(trainIterator.hasNext()){
                DataSet t = trainIterator.next();
                INDArray features = t.getFeatureMatrix();
                INDArray labels = t.getLabels();
                INDArray predicted = model.output(features,true);
                eval.eval(labels, predicted);
            }
            //Print the evaluation statistics
            log.info(eval.stats());

            log.info("Evaluate test model....");
            eval = new Evaluation(numOutputs);
            while(testIterator.hasNext()){
                DataSet t = testIterator.next();
                INDArray features = t.getFeatureMatrix();
                INDArray labels = t.getLabels();
                INDArray predicted = model.output(features,false);
                eval.eval(labels, predicted);
            }
            //Print the evaluation statistics
            log.info(eval.stats());

            return new ResponseEntity<>("ok", HttpStatus.OK);
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    }

    @NotNull
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

    private RawCandlestick writeCsvFile(
            @Valid @RequestBody LearnerRequestForm learnerRequestForm,
            RawCandlestick rawCandlestick,
            PrintWriter printWriter,
            GranularityValue granularity,
            InstrumentValue instrument) throws Exception {
        rawCandlestick = rawCandlestickRepository.findOne(
                rawCandlestick.getNextDateTime(),
                granularity,
                instrument);
        List<IndicatorValue> indicatorValues = new ArrayList<>();
        for (String indicatorName : learnerRequestForm.getIndicators()) {
            indicatorValues.add(IndicatorValue.getIndicatorValue(indicatorName));
        }
        RewardFunctionValue rewardFunctionValue = RewardFunctionValue.getRewardFunctionValue(
                learnerRequestForm.getRewardFunction());
        printWriter.println(rawCandlestick.toCsvLine(rewardFunctionValue, indicatorValues,
                learnerRequestForm.getTestConvergance() != null ? learnerRequestForm.getTestConvergance() : false));
        printWriter.flush();
        return rawCandlestick;
    }
}
