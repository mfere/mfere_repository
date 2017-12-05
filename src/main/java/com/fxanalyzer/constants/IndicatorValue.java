package com.fxanalyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IndicatorValue {
    STANDARD_MACD("macd");

    String name;

    /**
     * convert Indicator name to exact value
     *
     * @param name: name of Indicator
     * @return a IndicatorValue
     */
    public static IndicatorValue getIndicatorValue(String name) {
        for (IndicatorValue value : IndicatorValue.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
