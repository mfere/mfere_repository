package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * value for training purpose.
 */
@AllArgsConstructor
@Getter
public enum TrainingValue {

    INDICATOR_EXIST(1),
    INDICATOR_NOT_EXIST(0),

    CLOSE_ABOVE(1),
    CLOSE_BELOW(-1),

    UPWARD(1),
    DOWNWARD(-1),

    POSITIVE(1),
    NEGATIVE(-1),

    OVER_SOLD(1),
    OVER_BROUGHT(-1),

    EXPANDING(1),
    CONTRACTING(-1);

    double value;
}
