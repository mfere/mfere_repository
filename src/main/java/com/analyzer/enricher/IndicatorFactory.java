package com.analyzer.enricher;

import com.analyzer.constants.IndicatorValue;
import com.analyzer.constants.TrainingValue;
import com.analyzer.model.RawCandlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.candles.BearishEngulfingIndicator;
import org.ta4j.core.indicators.candles.BearishHaramiIndicator;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.indicators.candles.BullishHaramiIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IndicatorFactory {

    private static final Logger log = LoggerFactory.getLogger(IndicatorFactory.class);

    private Map<IndicatorValue, Indicator<Decimal>> indicators = new HashMap<>();
    private Map<IndicatorValue, Indicator<Boolean>> candlesIndicator = new HashMap<>();

    IndicatorFactory(List<RawCandlestick> rawCandlestickList) {
        TimeSeries timeSeries = TimeSeriesLoader.loadTimeSeries(rawCandlestickList);

        // put all indicators we will use
        candlesIndicator.put(IndicatorValue.BULLISH_ENGULFING_CANDLE, new BullishEngulfingIndicator(timeSeries));
        candlesIndicator.put(IndicatorValue.BEARISH_ENGULFING_CANDLE, new BearishEngulfingIndicator(timeSeries));
        candlesIndicator.put(IndicatorValue.BULLISH_HARAM_CANDLE, new BullishHaramiIndicator(timeSeries));
        candlesIndicator.put(IndicatorValue.BEARISH_HARAM_CANDLE, new BearishHaramiIndicator(timeSeries));

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
        StandardDeviationIndicator sdIndicator = new StandardDeviationIndicator(closePriceIndicator,20);
        indicators.put(IndicatorValue.CLOSE_PRICE_RAW, closePriceIndicator);
        indicators.put(IndicatorValue.VOLUME_RAW, new VolumeIndicator(timeSeries));

        indicators.put(IndicatorValue.RSI_RAW, new SMAIndicator(closePriceIndicator, 5));

        indicators.put(IndicatorValue.SMA_5_RAW, new SMAIndicator(closePriceIndicator, 5));
        indicators.put(IndicatorValue.SMA_10_RAW, new SMAIndicator(closePriceIndicator, 10));
        indicators.put(IndicatorValue.SMA_50_RAW, new SMAIndicator(closePriceIndicator, 50));
        indicators.put(IndicatorValue.SMA_100_RAW, new SMAIndicator(closePriceIndicator, 100));
        indicators.put(IndicatorValue.SMA_200_RAW, new SMAIndicator(closePriceIndicator, 200));

        indicators.put(IndicatorValue.MACD_RAW, new MACDIndicator(closePriceIndicator, 12, 26));

        // bollinger band
        BollingerBandsMiddleIndicator BBMiddle = new BollingerBandsMiddleIndicator(new SMAIndicator(closePriceIndicator, 20));
        BollingerBandsUpperIndicator BBUpper = new BollingerBandsUpperIndicator(BBMiddle, sdIndicator);
        BollingerBandsLowerIndicator BBLower = new BollingerBandsLowerIndicator(BBMiddle, sdIndicator);
        indicators.put(IndicatorValue.BOLLINGER_BAND_MIDDLE_RAW, BBMiddle);
        indicators.put(IndicatorValue.BOLLINGER_BAND_UPPER_RAW, BBUpper);
        indicators.put(IndicatorValue.BOLLINGER_BAND_LOWER_RAW, BBLower);
        indicators.put(IndicatorValue.BOLLINGER_BAND_WIDTH_RAW, new BollingerBandWidthIndicator(BBUpper, BBMiddle, BBLower));

        //KD
        StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(timeSeries,5);
        indicators.put(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW, kIndicator);
        // D indicator default time frame is 3
        indicators.put(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW, new StochasticOscillatorDIndicator(kIndicator));
    }

    /**
     * return value of a candle for training based on the strategy
     * @param indicatorValue
     * @param candleId
     * @return
     */
    public Double getIndicatorValue(IndicatorValue indicatorValue, int candleId) {

        switch (indicatorValue) {
            case BULLISH_ENGULFING_CANDLE:
                return candlesIndicator.get(IndicatorValue.BULLISH_ENGULFING_CANDLE).getValue(candleId) ?
                        TrainingValue.CANDLE_PATTERN_EXIST.getValue() : TrainingValue.NO_SIGNAL.getValue();
            case BEARISH_ENGULFING_CANDLE:
                return candlesIndicator.get(IndicatorValue.BEARISH_ENGULFING_CANDLE).getValue(candleId) ?
                        TrainingValue.CANDLE_PATTERN_EXIST.getValue() : TrainingValue.NO_SIGNAL.getValue();
            case BULLISH_HARAM_CANDLE:
                return candlesIndicator.get(IndicatorValue.BULLISH_HARAM_CANDLE).getValue(candleId) ?
                        TrainingValue.CANDLE_PATTERN_EXIST.getValue() : TrainingValue.NO_SIGNAL.getValue();
            case BEARISH_HARAM_CANDLE:
                return candlesIndicator.get(IndicatorValue.BEARISH_HARAM_CANDLE).getValue(candleId) ?
                        TrainingValue.CANDLE_PATTERN_EXIST.getValue() : TrainingValue.NO_SIGNAL.getValue();

            // SMA 5
            case SMA_5_RAW:
                return indicators.get(IndicatorValue.SMA_5_RAW).getValue(candleId).toDouble();
            case SMA_5_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_5_RAW), candleId);
            case SMA_5_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.SMA_5_RAW), candleId, 5);
            case SMA_5_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_5_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // SMA 10
            case SMA_10_RAW:
                return indicators.get(IndicatorValue.SMA_10_RAW).getValue(candleId).toDouble();
            case SMA_10_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_10_RAW), candleId);
            case SMA_10_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.SMA_10_RAW), candleId, 10);
            case SMA_10_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_10_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // SMA 50
            case SMA_50_RAW:
                return indicators.get(IndicatorValue.SMA_50_RAW).getValue(candleId).toDouble();
            case SMA_50_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_50_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 50);
            case SMA_50_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_50_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // SMA 100
            case SMA_100_RAW:
                return indicators.get(IndicatorValue.SMA_100_RAW).getValue(candleId).toDouble();
            case SMA_100_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_100_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 100);
            case SMA_100_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_100_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);

            // SMA 200
            case SMA_200_RAW:
                return indicators.get(IndicatorValue.SMA_200_RAW).getValue(candleId).toDouble();
            case SMA_200_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_200_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 200);
            case SMA_200_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_200_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // MACD
            case MACD_RAW:
                return indicators.get(IndicatorValue.MACD_RAW).getValue(candleId).toDouble();
            case MACD_div_positive_or_negative:
                return indicators.get(IndicatorValue.MACD_RAW).getValue(candleId).isGreaterThanOrEqual(Decimal.ZERO) ?
                        TrainingValue.POSITIVE.getValue() : TrainingValue.NEGATIVE.getValue();

            //RSI
            case RSI_RAW:
                return  indicators.get(IndicatorValue.RSI_RAW).getValue(candleId).toDouble();
            case RSI_OVER_BROUGHT_OR_SOLD:
                return overBroughtOrSold(indicators.get(IndicatorValue.RSI_RAW), candleId, 80, 20);
            case RSI_UPWARD_OR_DOWNWARD_SLOPING:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.RSI_RAW), candleId, 14);

            // BOLLING BAND
            case BOLLINGER_BAND_EXPANDING_OR_CONTRACTING:
                boolean upperUpwardMoving = upwardOrDownwardSloping(indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW),
                        candleId, 5) == TrainingValue.UPWARD.getValue();
                boolean lowerUpwardMoving = upwardOrDownwardSloping(indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW),
                        candleId, 5) == TrainingValue.DOWNWARD.getValue();
                return upperUpwardMoving && lowerUpwardMoving ? TrainingValue.EXPANDING.getValue() : TrainingValue.CONTRACTING.getValue();
            case BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW), candleId);
            case BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW), candleId);
            case BOLLINGER_BAND_WIDTH_RAW:
                return indicators.get(IndicatorValue.BOLLINGER_BAND_WIDTH_RAW).getValue(candleId).toDouble();
            case BOLLINGER_BAND_LOWER_DIFF:
                return difference(indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);
            case BOLLINGER_BAND_UPPER_DIFF:
                return difference(indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);
            case BOLLINGER_BAND_MIDDLE_DIFF:
                return difference(indicators.get(IndicatorValue.BOLLINGER_BAND_MIDDLE_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // stochastic oscillator
            case STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D:
                return closeAboveOrBelow(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW),
                        indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId);
            case STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD:
                Double kOver = overBroughtOrSold(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 80, 20);
                Double dOver = overBroughtOrSold(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 80, 20);
                if (TrainingValue.OVER_BROUGHT.getValue() == kOver && TrainingValue.OVER_BROUGHT.getValue() == dOver)
                    return TrainingValue.OVER_BROUGHT.getValue();
                if (TrainingValue.OVER_SOLD.getValue() == kOver && TrainingValue.OVER_SOLD.getValue() == dOver)
                    return TrainingValue.OVER_SOLD.getValue();
                return TrainingValue.NO_SIGNAL.getValue();
            case STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 5);
            case STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING:
                return upwardOrDownwardSloping(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 3);
            case VOLUME_RAW:
                return indicators.get(IndicatorValue.VOLUME_RAW).getValue(candleId).toDouble();
            default:
                log.info("fail to get indicator value : " + indicatorValue.getName());
                return null;
        }
    }

    /**
     * check if first indicator is close above or below the second indicator
     *
     * @param firstIndicator
     * @param secondIndicator
     * @param candleId
     * @return
     */
    private Double closeAboveOrBelow(Indicator<Decimal> firstIndicator, Indicator<Decimal> secondIndicator, int candleId) {
        if (firstIndicator.getValue(candleId).isGreaterThanOrEqual(secondIndicator.getValue(candleId))) {
            return TrainingValue.CLOSE_ABOVE.getValue();
        }
        return TrainingValue.CLOSE_BELOW.getValue();
    }

    /**
     * check if first indicator is close above or below the second indicator
     *
     * @param firstIndicator
     * @param secondIndicator
     * @param candleId
     * @return
     */
    private Double difference(Indicator<Decimal> firstIndicator, Indicator<Decimal> secondIndicator, int candleId) {
        return firstIndicator.getValue(candleId).minus(secondIndicator.getValue(candleId)).toDouble();
    }

    /**
     * check if the indicator is upward sloping or downward sloping.
     * calculate by least squares and determined by slope
     * @param candleId
     * @param timeFrame
     * @return
     */
    private Double upwardOrDownwardSloping(Indicator<Decimal> indicator, int candleId, int timeFrame) {
        int startIndex = Math.max(0, candleId - timeFrame + 1);
        int endIndex = candleId;
        if (candleId - startIndex + 1 < 2) {
            // Not enough candle to compute a regression line. i.e. only 1 candle
            log.info("Not enough candle to compute a regression line for candle " + candleId);
            return 0d;
        }

        // linear regression parameters
        Decimal slope;

        // compute xBar and yBar
        Decimal sumX = Decimal.ZERO;
        Decimal sumY = Decimal.ZERO;
        for (int i = startIndex; i <= endIndex; i++) {
            sumX = sumX.plus(Decimal.valueOf(i));
            sumY = sumY.plus(indicator.getValue(i));
        }
        Decimal nbObservations = Decimal.valueOf(endIndex - startIndex + 1);
        Decimal xBar = sumX.dividedBy(nbObservations);
        Decimal yBar = sumY.dividedBy(nbObservations);

        // compute slope
        Decimal xxBar = Decimal.ZERO;
        Decimal xyBar = Decimal.ZERO;
        for (int i = startIndex; i <= endIndex; i++) {
            Decimal dX = Decimal.valueOf(i).minus(xBar);
            Decimal dY = indicator.getValue(i).minus(yBar);
            xxBar = xxBar.plus(dX.multipliedBy(dX));
            xyBar = xyBar.plus(dX.multipliedBy(dY));
        }

        // if slope +ve --> upward sloping
        slope = xyBar.dividedBy(xxBar);
        if (slope.isGreaterThanOrEqual(Decimal.ZERO))
            return TrainingValue.UPWARD.getValue();
        return TrainingValue.DOWNWARD.getValue();
    }

    private Double overBroughtOrSold(Indicator<Decimal> indicator, int candleId, int overBroughValue, int overSoldValue) {
        // indicator is in over brought area
        if (indicator.getValue(candleId).isGreaterThan(Decimal.valueOf(overBroughValue))) {
            return TrainingValue.OVER_BROUGHT.getValue();
        // indicator is in over sold area
        } else if (indicator.getValue(candleId).isLessThan(Decimal.valueOf(overSoldValue))) {
            return TrainingValue.OVER_SOLD.getValue();
        }
        return TrainingValue.NO_SIGNAL.getValue();
    }
}
