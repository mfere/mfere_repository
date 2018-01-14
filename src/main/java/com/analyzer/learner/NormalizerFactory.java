package com.analyzer.learner;

import com.analyzer.constants.NormalizerValue;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.AbstractDataSetNormalizer;
import org.nd4j.linalg.dataset.api.preprocessor.AbstractNormalizer;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

public class NormalizerFactory {
    public static AbstractDataSetNormalizer getNormalizer(
            NormalizerValue value) {
        switch (value) {
            case MIN_MAX: return new NormalizerMinMaxScaler();
            case STANDARD: return new NormalizerStandardize();
            default: return null;
        }
    }
}
