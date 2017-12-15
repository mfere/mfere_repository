package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrendForcastValue {
    BULLISH(1),
    BEARLISh(0);

    double value;
}
