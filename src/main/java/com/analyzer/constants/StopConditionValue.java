package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StopConditionValue {
    FIXED_EPOC_LENGTH_500,
    FIXED_EPOC_LENGTH_1000,
    FIXED_EPOC_LENGTH_1500,
    FIXED_EPOC_LENGTH_2000,
    LEAST_ERROR_LAST_100,
    BEST_SCORE_TRAIN_LAST_10,
    BEST_SCORE_TEST_LAST_10
}
