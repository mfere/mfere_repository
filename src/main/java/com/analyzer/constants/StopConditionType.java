package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StopConditionType {
    FIXED_EPOC_LENGTH_500,
    FIXED_EPOC_LENGTH_1000,
    FIXED_EPOC_LENGTH_1500,
    FIXED_EPOC_LENGTH_2000,
    FIXED_EPOC_LENGTH_10000,
    LEAST_ERROR_LAST_100,
    LEAST_ERROR_LAST_1000,
    BEST_SCORE_TRAIN_LAST_10,
    BEST_SCORE_TEST_LAST_10
}
