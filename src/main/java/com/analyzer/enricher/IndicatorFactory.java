package com.analyzer.enricher;

import com.analyzer.constants.IndicatorValue;
import com.analyzer.constants.TrendForcastValue;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.SimpleLinearRegressionIndicator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndicatorFactory {

    Map<IndicatorValue, Indicator<Decimal>> indicators = new HashMap<>();

    IndicatorFactory(TimeSeries timeSeries, List<IndicatorValue> indicatorList) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
        indicators.put(IndicatorValue.CLOSE_PRICE_RAW, closePriceIndicator);

        // TODO: need to improve : brian
        for (IndicatorValue requiredIndicator : indicatorList) {
            switch (requiredIndicator) {
                case SMA_5_RAW:
                    indicators.put(IndicatorValue.SMA_5_RAW, new SMAIndicator(closePriceIndicator, 5));
                case MACD_RAW:
                    indicators.put(IndicatorValue.MACD_RAW, new MACDIndicator(closePriceIndicator, 12, 26));
                case RSI_RAW:
                    indicators.put(IndicatorValue.RSI_RAW, new RSIIndicator(closePriceIndicator, 14));
                case BOLLINGER_BAND_RAW:
                    BollingerBandsMiddleIndicator BBMiddle = new BollingerBandsMiddleIndicator(closePriceIndicator);
                    BollingerBandsUpperIndicator BBUpper = new BollingerBandsUpperIndicator(BBMiddle, closePriceIndicator);
                    BollingerBandsLowerIndicator BBLower = new BollingerBandsLowerIndicator(BBMiddle, closePriceIndicator);
                    indicators.put(IndicatorValue.BOLLINGER_BAND_MIDDLE_RAW, BBMiddle);
                    indicators.put(IndicatorValue.BOLLINGER_BAND_UPPER_RAW, BBUpper);
                    indicators.put(IndicatorValue.BOLLINGER_BAND_LOWER_RAW, BBLower);
                    indicators.put(IndicatorValue.BOLLINGER_BAND_WIDTH_RAW, new BollingerBandWidthIndicator(BBUpper, BBMiddle, BBLower));
                case STOCHASTIC_OSCILLATOR_K_RAW:
                    StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(timeSeries,5);
                    indicators.put(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW, kIndicator);
                    indicators.put(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW, new StochasticOscillatorDIndicator(kIndicator));
            }
        }
    }

    /**
     * return value of a candle for training based on the strategy
     * @param strategyValue
     * @param candleId
     * @return
     */
    public Double getIndicatorValue(IndicatorValue strategyValue, int candleId) {
        switch (strategyValue) {
            case SMA_5_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelowSMA(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_5_RAW), candleId);

        }
        System.out.println("fail to get indicator value");
        return null;
    }

    /**
     * check if first indicator is close above or below the second indicator
     *
     * @param firstIndicator
     * @param secondIndicator
     * @param candleId
     * @return
     */
    private Double closeAboveOrBelowSMA(Indicator<Decimal> firstIndicator, Indicator<Decimal> secondIndicator, int candleId) {
        if (firstIndicator.getValue(candleId).isGreaterThanOrEqual(secondIndicator.getValue(candleId))) {
            return TrendForcastValue.BULLISH.getValue();
        }
        return TrendForcastValue.BEARLISh.getValue();
    }

//    private Double upwardOrDownwardSloping(IndicatorValue strategyValue) {
//        SimpleLinearRegressionIndicator test = new SimpleLinearRegressionIndicator(new SMAIndicator(indicator, 5),5);
//        test.getValue()
//    }

}
