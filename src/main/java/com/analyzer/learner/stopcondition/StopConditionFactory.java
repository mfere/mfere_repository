package com.analyzer.learner.stopcondition;

import com.analyzer.constants.StopConditionType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class StopConditionFactory {
    public static StopCondition getStopCondition(
            StopConditionType value, MultiLayerNetwork model,
            DataSetIterator trainIterator,
            DataSetIterator testIterator) {
        switch (value) {
            case FIXED_EPOC_LENGTH_500: return new FixedEpocLength(500, model);
            case FIXED_EPOC_LENGTH_1000: return new FixedEpocLength(1000, model);
            case FIXED_EPOC_LENGTH_1500: return new FixedEpocLength(1500, model);
            case FIXED_EPOC_LENGTH_2000: return new FixedEpocLength(2000, model);
            case FIXED_EPOC_LENGTH_10000: return new FixedEpocLength(10000, model);
            case LEAST_ERROR_LAST_100: return new LeastError(100, model);
            case LEAST_ERROR_LAST_1000: return new LeastError(1000, model);
            case BEST_SCORE_TRAIN_LAST_10: return new BestF1Score(10, 10, model, trainIterator);
            case BEST_SCORE_TEST_LAST_10: return new BestF1Score(10, 10, model, testIterator);
            default: return null;
        }
    }
}
