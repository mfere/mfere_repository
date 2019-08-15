package com.analyzer.learner.stopcondition;

import com.analyzer.constants.StopConditionType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class StopConditionFactory {
    public static StopCondition getStopCondition(
            StopConditionType value, MultiLayerNetwork model,
            DataSetIterator trainIterator,
            DataSetIterator validationIterator,
            int numOutput) {
        switch (value) {
            case FIXED_EPOC_LENGTH_500: return new FixedEpocLength(500, model);
            case FIXED_EPOC_LENGTH_1000: return new FixedEpocLength(1000, model);
            case FIXED_EPOC_LENGTH_1500: return new FixedEpocLength(1500, model);
            case FIXED_EPOC_LENGTH_2000: return new FixedEpocLength(2000, model);
            case FIXED_EPOC_LENGTH_10000: return new FixedEpocLength(10000, model);
            case LEAST_ERROR_LAST_100: return new LeastError(100, model);
            case LEAST_ERROR_LAST_1000: return new LeastError(1000, model);
            case BEST_TRAIN_SCORE_LAST_100: return new BestScore(10, 100, model, trainIterator, numOutput);
            case BEST_VALIDATION_SCORE_LAST_1000: return new BestScore(100, 10, model,validationIterator, numOutput);
            case BEST_VALIDATION_SCORE_LAST_10000: return new BestScore(100, 100, model, validationIterator, numOutput);
            case BEST_VALIDATION_SCORE_LAST_30000: return new BestScore(100, 300, model, validationIterator, numOutput);
            case BEST_VALIDATION_SCORE_LAST_50000: return new BestScore(100, 500, model, validationIterator, numOutput);
            default: return null;
        }
    }
}
