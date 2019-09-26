package com.analyzer.constants;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IndicatorOperation {
    NO_OPERATION(false),
    DIFFERENCE_PREVIOUS(false),
    DIFFERENCE_LATEST_CLOSE_PRICE(true);

    public boolean isDifferenceWithLatest;
}
