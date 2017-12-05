package com.analyzer.enricher;

import com.analyzer.constants.IndicatorValue;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.MACDIndicator;

public class IndicatorFactory {

    static Indicator<Decimal> getIndicator(Indicator<Decimal> indicator, IndicatorValue value) {
        switch (value) {
            case STANDARD_MACD: return new MACDIndicator(indicator, 12, 26);
            default: return null;
        }
    }
}
