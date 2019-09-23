package com.analyzer.learner;

import com.analyzer.constants.NormalizerType;
import org.nd4j.linalg.dataset.api.preprocessor.AbstractDataSetNormalizer;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

public class NormalizerFactory {
    public static AbstractDataSetNormalizer getNormalizer(
            NormalizerType value) {
        switch (value) {
            case MIN_MAX: return new NormalizerMinMaxScaler();
            case STANDARDIZE: return new NormalizerStandardize();
            default: return null;
        }
    }
}
