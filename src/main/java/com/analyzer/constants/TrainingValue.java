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
    INDICATOR_NOT_EXIST(0);
    double value;
}
