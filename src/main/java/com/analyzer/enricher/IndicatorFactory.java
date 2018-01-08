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
        // candle stick
        candlesIndicator.put(IndicatorValue.BULLISH_ENGULFING_CANDLE, new BullishEngulfingIndicator(timeSeries));
        candlesIndicator.put(IndicatorValue.BEARISH_ENGULFING_CANDLE, new BearishEngulfingIndicator(timeSeries));
        candlesIndicator.put(IndicatorValue.BULLISH_HARAM_CANDLE, new BullishHaramiIndicator(timeSeries));
        candlesIndicator.put(IndicatorValue.BEARISH_HARAM_CANDLE, new BearishHaramiIndicator(timeSeries));

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
        StandardDeviationIndicator sdIndicator = new StandardDeviationIndicator(closePriceIndicator,20);
        indicators.put(IndicatorValue.CLOSE_PRICE_RAW, closePriceIndicator);
        indicators.put(IndicatorValue.VOLUME_RAW, new VolumeIndicator(timeSeries));

        // RSI, time frame default as 5
        indicators.put(IndicatorValue.RSI_RAW, new SMAIndicator(closePriceIndicator, 5));

        // SMA
        indicators.put(IndicatorValue.SMA_5_RAW, new SMAIndicator(closePriceIndicator, 5));
        indicators.put(IndicatorValue.SMA_10_RAW, new SMAIndicator(closePriceIndicator, 10));
        indicators.put(IndicatorValue.SMA_50_RAW, new SMAIndicator(closePriceIndicator, 50));
        indicators.put(IndicatorValue.SMA_100_RAW, new SMAIndicator(closePriceIndicator, 100));
        indicators.put(IndicatorValue.SMA_200_RAW, new SMAIndicator(closePriceIndicator, 200));

        //MACD, using standard time frame
        indicators.put(IndicatorValue.MACD_RAW, new MACDIndicator(closePriceIndicator, 12, 26));

        // bollinger band, using standard time frame
        BollingerBandsMiddleIndicator BBMiddle = new BollingerBandsMiddleIndicator(new SMAIndicator(closePriceIndicator, 20));
        BollingerBandsUpperIndicator BBUpper = new BollingerBandsUpperIndicator(BBMiddle, sdIndicator);
        BollingerBandsLowerIndicator BBLower = new BollingerBandsLowerIndicator(BBMiddle, sdIndicator);
        indicators.put(IndicatorValue.BOLLINGER_BAND_MIDDLE_RAW, BBMiddle);
        indicators.put(IndicatorValue.BOLLINGER_BAND_UPPER_RAW, BBUpper);
        indicators.put(IndicatorValue.BOLLINGER_BAND_LOWER_RAW, BBLower);
        indicators.put(IndicatorValue.BOLLINGER_BAND_WIDTH_RAW, new BollingerBandWidthIndicator(BBUpper, BBMiddle, BBLower));

        //KD, using standard time frame
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
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BEARISH_ENGULFING_CANDLE:
                return candlesIndicator.get(IndicatorValue.BEARISH_ENGULFING_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BULLISH_HARAM_CANDLE:
                return candlesIndicator.get(IndicatorValue.BULLISH_HARAM_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BEARISH_HARAM_CANDLE:
                return candlesIndicator.get(IndicatorValue.BEARISH_HARAM_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();

            // SMA 5
            case SMA_5_RAW:
                return indicators.get(IndicatorValue.SMA_5_RAW).getValue(candleId).toDouble();
            case SMA_5_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_5_RAW), candleId);
            case SMA_5_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_5_RAW), candleId);
            case SMA_5_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.SMA_5_RAW), candleId, 5);
            case SMA_5_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.SMA_5_RAW), candleId, 5);
            case SMA_5_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_5_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // SMA 10
            case SMA_10_RAW:
                return indicators.get(IndicatorValue.SMA_10_RAW).getValue(candleId).toDouble();
            case SMA_10_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_10_RAW), candleId);
            case SMA_10_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_10_RAW), candleId);
            case SMA_10_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.SMA_10_RAW), candleId, 10);
            case SMA_10_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.SMA_10_RAW), candleId, 10);
            case SMA_10_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_10_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // SMA 50
            case SMA_50_RAW:
                return indicators.get(IndicatorValue.SMA_50_RAW).getValue(candleId).toDouble();
            case SMA_50_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_50_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_50_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 50);
            case SMA_50_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 50);
            case SMA_50_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_50_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // SMA 100
            case SMA_100_RAW:
                return indicators.get(IndicatorValue.SMA_100_RAW).getValue(candleId).toDouble();
            case SMA_100_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_100_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_100_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 100);
            case SMA_100_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 100);
            case SMA_100_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_100_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);

            // SMA 200
            case SMA_200_RAW:
                return indicators.get(IndicatorValue.SMA_200_RAW).getValue(candleId).toDouble();
            case SMA_200_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_200_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.SMA_50_RAW), candleId);
            case SMA_200_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 200);
            case SMA_200_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.SMA_50_RAW), candleId, 200);
            case SMA_200_CLOSE_DIFF:
                return difference(indicators.get(IndicatorValue.SMA_200_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);


            // MACD
            case MACD_RAW:
                return indicators.get(IndicatorValue.MACD_RAW).getValue(candleId).toDouble();
            case MACD_IS_DIV_POSITIVE:
                return indicators.get(IndicatorValue.MACD_RAW).getValue(candleId).isGreaterThan(Decimal.ZERO) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case MACD_IS_DIV_NEGATIVE:
                return indicators.get(IndicatorValue.MACD_RAW).getValue(candleId).isLessThan(Decimal.ZERO) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();

            //RSI
            case RSI_RAW:
                return  indicators.get(IndicatorValue.RSI_RAW).getValue(candleId).toDouble();
            case RSI_IS_OVER_BROUGHT:
                return isOverBrought(indicators.get(IndicatorValue.RSI_RAW), candleId, 80);
            case RSI_IS_OVER_SOLD:
                return isOverSold(indicators.get(IndicatorValue.RSI_RAW), candleId, 20);
            case RSI_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.RSI_RAW), candleId, 5);
            case RSI_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.RSI_RAW), candleId, 5);

            // BOLLING BAND
            case BOLLINGER_BAND_IS_EXPANDING:
                boolean isUpperUpwardMoving = isUpwardSloping(indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean isLowerDownwardMoving = isDownwardSloping(indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                return isUpperUpwardMoving && isLowerDownwardMoving ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BOLLINGER_BAND_IS_CONTRACTING:
                boolean isUpperDownwardMoving = isDownwardSloping(indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean isLowerUpwardMoving = isUpwardSloping(indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                return isUpperDownwardMoving && isLowerUpwardMoving ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BOLLINGER_BAND_UPPER_IS_CLOSE_ABOVE:
                return isClosedAbove(indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW), candleId);
            case BOLLINGER_BAND_UPPER_IS_CLOSE_BELOW:
                return isClosedBelow(indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW), candleId);
            case BOLLINGER_BAND_LOWER_IS_CLOSE_ABOVE:
                return isClosedAbove(indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW), candleId);
            case BOLLINGER_BAND_LOWER_IS_CLOSE_BELOW:
                return isClosedBelow(indicators.get(IndicatorValue.CLOSE_PRICE_RAW), indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW), candleId);
            case BOLLINGER_BAND_WIDTH_RAW:
                return indicators.get(IndicatorValue.BOLLINGER_BAND_WIDTH_RAW).getValue(candleId).toDouble();
            case BOLLINGER_BAND_LOWER_DIFF:
                return difference(indicators.get(IndicatorValue.BOLLINGER_BAND_LOWER_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);
            case BOLLINGER_BAND_UPPER_DIFF:
                return difference(indicators.get(IndicatorValue.BOLLINGER_BAND_UPPER_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);
            case BOLLINGER_BAND_MIDDLE_DIFF:
                return difference(indicators.get(IndicatorValue.BOLLINGER_BAND_MIDDLE_RAW), indicators.get(IndicatorValue.CLOSE_PRICE_RAW), candleId);

            // stochastic oscillator
            case STOCHASTIC_OSCILLATOR_K_ABOVE_D:
                return isClosedAbove(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW),
                        indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId);
            case STOCHASTIC_OSCILLATOR_K_BELOW_D:
                return isClosedBelow(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW),
                        indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId);
            case STOCHASTIC_OSCILLATOR_IS_KD_OVER_BROUGHT:
                boolean kOverBrought = isOverBrought(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 80) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean dOverBrought = isOverSold(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 20) == TrainingValue.INDICATOR_EXIST.getValue();
                if (kOverBrought && dOverBrought)
                    return TrainingValue.INDICATOR_EXIST.getValue();
                return TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case STOCHASTIC_OSCILLATOR_IS_KD_OVER_SOLD:
                boolean kOverSold = isOverBrought(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 80) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean dOverSold = isOverSold(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 20) == TrainingValue.INDICATOR_EXIST.getValue();
                if (kOverSold && dOverSold)
                    return TrainingValue.INDICATOR_EXIST.getValue();
                return TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case STOCHASTIC_OSCILLATOR_IS_K_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 5);
            case STOCHASTIC_OSCILLATOR_IS_K_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 5);
            case STOCHASTIC_OSCILLATOR_IS_D_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 3);
            case STOCHASTIC_OSCILLATOR_IS_D_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorValue.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 3);
            case VOLUME_RAW:
                return indicators.get(IndicatorValue.VOLUME_RAW).getValue(candleId).toDouble();
            default:
                log.info("fail to get indicator value : " + indicatorValue.getName());
                return null;
        }
    }

    /**
     * check if first indicator is close above second indicator
     *
     * @param firstIndicator
     * @param secondIndicator
     * @param candleId
     * @return
     */
    private Double isClosedAbove(Indicator<Decimal> firstIndicator, Indicator<Decimal> secondIndicator, int candleId) {
        if (firstIndicator.getValue(candleId).isGreaterThan(secondIndicator.getValue(candleId))) {
            return TrainingValue.INDICATOR_EXIST.getValue();
        }
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }

    /**
     * check if first indicator is close below second indicator
     *
     * @param firstIndicator
     * @param secondIndicator
     * @param candleId
     * @return
     */
    private Double isClosedBelow(Indicator<Decimal> firstIndicator, Indicator<Decimal> secondIndicator, int candleId) {
        if (firstIndicator.getValue(candleId).isLessThan(secondIndicator.getValue(candleId))) {
            return TrainingValue.INDICATOR_EXIST.getValue();
        }
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
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
     * calculate slope by least squares.
     *
     * @param indicator
     * @param candleId
     * @param timeFrame
     * @return
     */
    private Decimal calculateSlope(Indicator<Decimal> indicator, int candleId, int timeFrame) {
        int startIndex = Math.max(0, candleId - timeFrame + 1);
        int endIndex = candleId;
        if (candleId - startIndex + 1 < 2) {
            // Not enough candle to compute a regression line. i.e. only 1 candle
            log.info("Not enough candle to compute a regression line for candle " + candleId);
            return Decimal.NaN;
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
        return slope;
    }

    /**
     * check if the indicator is upward sloping.
     *
     * @param indicator
     * @param candleId
     * @param timeFrame
     * @return
     */
    private Double isUpwardSloping(Indicator<Decimal> indicator, int candleId, int timeFrame) {
        Decimal slope = calculateSlope(indicator, candleId, timeFrame);
        if (slope.isGreaterThan(Decimal.ZERO))
            return TrainingValue.INDICATOR_EXIST.getValue();
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }

    /**
     * check if the indicator is downward sloping.
     *
     * @param indicator
     * @param candleId
     * @param timeFrame
     * @return
     */
    private Double isDownwardSloping(Indicator<Decimal> indicator, int candleId, int timeFrame) {
        Decimal slope = calculateSlope(indicator, candleId, timeFrame);
        if (slope.isLessThan(Decimal.ZERO))
            return TrainingValue.INDICATOR_EXIST.getValue();
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }

    /**
     * check if the indicator is within over brought area.
     *
     * @param indicator
     * @param candleId
     * @param overBroughtValue
     * @return
     */
    private Double isOverBrought(Indicator<Decimal> indicator, int candleId, int overBroughtValue) {
        // indicator is in over brought area
        if (indicator.getValue(candleId).isGreaterThan(Decimal.valueOf(overBroughtValue))) {
            return TrainingValue.INDICATOR_EXIST.getValue();
        }
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }

    /**
     * check if the indicator is within over sold area.
     *
     * @param indicator
     * @param candleId
     * @param overSoldValue
     * @return
     */
    private Double isOverSold(Indicator<Decimal> indicator, int candleId, int overSoldValue) {
        // indicator is in over sold area
        if (indicator.getValue(candleId).isLessThan(Decimal.valueOf(overSoldValue))) {
            return TrainingValue.INDICATOR_EXIST.getValue();
        }
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }

}
