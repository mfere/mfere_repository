package com.analyzer.enricher;

import com.analyzer.constants.IndicatorType;
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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndicatorFactory {

    private static final Logger log = LoggerFactory.getLogger(IndicatorFactory.class);

    private Map<IndicatorType, Indicator<Decimal>> indicators = new HashMap<>();
    private Map<IndicatorType, Indicator<Boolean>> candlesIndicator = new HashMap<>();
    private List<RawCandlestick> rawCandlestickList;

    public IndicatorFactory(List<RawCandlestick> rawCandlestickList) {
        this.rawCandlestickList = rawCandlestickList;
        TimeSeries timeSeries = TimeSeriesLoader.loadTimeSeries(rawCandlestickList);

        // put all indicators we will use
        // candle stick
        candlesIndicator.put(IndicatorType.BULLISH_ENGULFING_CANDLE, new BullishEngulfingIndicator(timeSeries));
        candlesIndicator.put(IndicatorType.BEARISH_ENGULFING_CANDLE, new BearishEngulfingIndicator(timeSeries));
        candlesIndicator.put(IndicatorType.BULLISH_HARAM_CANDLE, new BullishHaramiIndicator(timeSeries));
        candlesIndicator.put(IndicatorType.BEARISH_HARAM_CANDLE, new BearishHaramiIndicator(timeSeries));

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
        StandardDeviationIndicator sdIndicator = new StandardDeviationIndicator(closePriceIndicator,20);
        indicators.put(IndicatorType.CLOSE_PRICE_RAW, closePriceIndicator);
        indicators.put(IndicatorType.VOLUME_RAW, new VolumeIndicator(timeSeries));

        // RSI, time frame default as 5
        indicators.put(IndicatorType.RSI_RAW, new SMAIndicator(closePriceIndicator, 5));

        // SMA
        indicators.put(IndicatorType.SMA_5_RAW, new SMAIndicator(closePriceIndicator, 5));
        indicators.put(IndicatorType.SMA_10_RAW, new SMAIndicator(closePriceIndicator, 10));
        indicators.put(IndicatorType.SMA_50_RAW, new SMAIndicator(closePriceIndicator, 50));
        indicators.put(IndicatorType.SMA_100_RAW, new SMAIndicator(closePriceIndicator, 100));
        indicators.put(IndicatorType.SMA_200_RAW, new SMAIndicator(closePriceIndicator, 200));

        //MACD, using standard time frame
        indicators.put(IndicatorType.MACD_RAW, new MACDIndicator(closePriceIndicator, 12, 26));

        // bollinger band, using standard time frame
        BollingerBandsMiddleIndicator BBMiddle = new BollingerBandsMiddleIndicator(new SMAIndicator(closePriceIndicator, 20));
        BollingerBandsUpperIndicator BBUpper = new BollingerBandsUpperIndicator(BBMiddle, sdIndicator);
        BollingerBandsLowerIndicator BBLower = new BollingerBandsLowerIndicator(BBMiddle, sdIndicator);
        indicators.put(IndicatorType.BOLLINGER_BAND_MIDDLE_RAW, BBMiddle);
        indicators.put(IndicatorType.BOLLINGER_BAND_UPPER_RAW, BBUpper);
        indicators.put(IndicatorType.BOLLINGER_BAND_LOWER_RAW, BBLower);
        indicators.put(IndicatorType.BOLLINGER_BAND_WIDTH_RAW, new BollingerBandWidthIndicator(BBUpper, BBMiddle, BBLower));

        //KD, using standard time frame
        StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(timeSeries,5);
        indicators.put(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW, kIndicator);
        // D indicator default time frame is 3
        indicators.put(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW, new StochasticOscillatorDIndicator(kIndicator));
    }

    /**
     * return value of a candle for training based on the strategy
     * @param indicatorType
     * @param candleId
     * @return
     */
    public Double getIndicatorValue(IndicatorType indicatorType, int candleId) {

        switch (indicatorType) {
            case BULLISH_ENGULFING_CANDLE:
                return candlesIndicator.get(IndicatorType.BULLISH_ENGULFING_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BEARISH_ENGULFING_CANDLE:
                return candlesIndicator.get(IndicatorType.BEARISH_ENGULFING_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BULLISH_HARAM_CANDLE:
                return candlesIndicator.get(IndicatorType.BULLISH_HARAM_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BEARISH_HARAM_CANDLE:
                return candlesIndicator.get(IndicatorType.BEARISH_HARAM_CANDLE).getValue(candleId) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();

            // SMA 5
            case SMA_5_RAW:
                return indicators.get(IndicatorType.SMA_5_RAW).getValue(candleId).toDouble();
            case SMA_5_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_5_RAW), candleId);
            case SMA_5_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.SMA_5_RAW), candleId, 5);
            case SMA_5_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_5_RAW), candleId);
            case SMA_5_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_5_RAW), candleId);
            case SMA_5_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.SMA_5_RAW), candleId, 5);
            case SMA_5_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.SMA_5_RAW), candleId, 5);
            case SMA_5_CLOSE_DIFF:
                return difference(indicators.get(IndicatorType.SMA_5_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);

            // SMA 10
            case SMA_10_RAW:
                return indicators.get(IndicatorType.SMA_10_RAW).getValue(candleId).toDouble();
            case SMA_10_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_10_RAW), candleId);
            case SMA_10_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.SMA_10_RAW), candleId, 10);
            case SMA_10_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_10_RAW), candleId);
            case SMA_10_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_10_RAW), candleId);
            case SMA_10_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.SMA_10_RAW), candleId, 10);
            case SMA_10_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.SMA_10_RAW), candleId, 10);
            case SMA_10_CLOSE_DIFF:
                return difference(indicators.get(IndicatorType.SMA_10_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);


            // SMA 50
            case SMA_50_RAW:
                return indicators.get(IndicatorType.SMA_50_RAW).getValue(candleId).toDouble();
            case SMA_50_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_50_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 50);
            case SMA_50_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_50_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_50_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 50);
            case SMA_50_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 50);
            case SMA_50_CLOSE_DIFF:
                return difference(indicators.get(IndicatorType.SMA_50_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);


            // SMA 100
            case SMA_100_RAW:
                return indicators.get(IndicatorType.SMA_100_RAW).getValue(candleId).toDouble();
            case SMA_100_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_100_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 100);
            case SMA_100_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_100_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_100_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 100);
            case SMA_100_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 100);
            case SMA_100_CLOSE_DIFF:
                return difference(indicators.get(IndicatorType.SMA_100_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);

            // SMA 200
            case SMA_200_RAW:
                return indicators.get(IndicatorType.SMA_200_RAW).getValue(candleId).toDouble();
            case SMA_200_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_200_UPWARD_OR_DOWNWARD:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 200);
            case SMA_200_IS_CLOSE_ABOVE:
                return isClosedAbove(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_200_IS_CLOSE_BELOW:
                return isClosedBelow(
                        indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.SMA_50_RAW), candleId);
            case SMA_200_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 200);
            case SMA_200_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.SMA_50_RAW), candleId, 200);
            case SMA_200_CLOSE_DIFF:
                return difference(indicators.get(IndicatorType.SMA_200_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);


            // MACD
            case MACD_RAW:
                return indicators.get(IndicatorType.MACD_RAW).getValue(candleId).toDouble();
            case MACD_div_positive_or_negative:
                return indicators.get(IndicatorType.MACD_RAW).getValue(candleId).isGreaterThanOrEqual(Decimal.ZERO) ?
                        TrainingValue.POSITIVE.getValue() : TrainingValue.NEGATIVE.getValue();
            case MACD_IS_DIV_POSITIVE:
                return indicators.get(IndicatorType.MACD_RAW).getValue(candleId).isGreaterThan(Decimal.ZERO) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case MACD_IS_DIV_NEGATIVE:
                return indicators.get(IndicatorType.MACD_RAW).getValue(candleId).isLessThan(Decimal.ZERO) ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();

            //RSI
            case RSI_RAW:
                return  indicators.get(IndicatorType.RSI_RAW).getValue(candleId).toDouble();
            case RSI_OVER_BROUGHT_OR_SOLD:
                return overBroughtOrSold(indicators.get(IndicatorType.RSI_RAW), candleId, 80, 20);
            case RSI_UPWARD_OR_DOWNWARD_SLOPING:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.RSI_RAW), candleId, 14);
            case RSI_IS_OVER_BROUGHT:
                return isOverBrought(indicators.get(IndicatorType.RSI_RAW), candleId, 80);
            case RSI_IS_OVER_SOLD:
                return isOverSold(indicators.get(IndicatorType.RSI_RAW), candleId, 20);
            case RSI_IS_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.RSI_RAW), candleId, 5);
            case RSI_IS_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.RSI_RAW), candleId, 5);

            // BOLLING BAND
            case BOLLINGER_BAND_EXPANDING_OR_CONTRACTING:
                boolean upperUpwardMoving = upwardOrDownwardSloping(indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW),
                        candleId, 5) == TrainingValue.UPWARD.getValue();
                boolean lowerUpwardMoving = upwardOrDownwardSloping(indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW),
                        candleId, 5) == TrainingValue.DOWNWARD.getValue();
                return upperUpwardMoving && lowerUpwardMoving ? TrainingValue.EXPANDING.getValue() : TrainingValue.CONTRACTING.getValue();
            case BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW), candleId);
            case BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW:
                return closeAboveOrBelow(indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW), candleId);
            case BOLLINGER_BAND_IS_EXPANDING:
                boolean isUpperUpwardMoving = isUpwardSloping(indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean isLowerDownwardMoving = isDownwardSloping(indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                return isUpperUpwardMoving && isLowerDownwardMoving ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BOLLINGER_BAND_IS_CONTRACTING:
                boolean isUpperDownwardMoving = isDownwardSloping(indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean isLowerUpwardMoving = isUpwardSloping(indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW),
                        candleId, 5) == TrainingValue.INDICATOR_EXIST.getValue();
                return isUpperDownwardMoving && isLowerUpwardMoving ?
                        TrainingValue.INDICATOR_EXIST.getValue() : TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case BOLLINGER_BAND_UPPER_IS_CLOSE_ABOVE:
                return isClosedAbove(indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW), candleId);
            case BOLLINGER_BAND_UPPER_IS_CLOSE_BELOW:
                return isClosedBelow(indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW), candleId);
            case BOLLINGER_BAND_LOWER_IS_CLOSE_ABOVE:
                return isClosedAbove(indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW), candleId);
            case BOLLINGER_BAND_LOWER_IS_CLOSE_BELOW:
                return isClosedBelow(indicators.get(IndicatorType.CLOSE_PRICE_RAW), indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW), candleId);
            case BOLLINGER_BAND_WIDTH_RAW:
                return indicators.get(IndicatorType.BOLLINGER_BAND_WIDTH_RAW).getValue(candleId).toDouble();
            case BOLLINGER_BAND_LOWER_DIFF:
                return difference(indicators.get(IndicatorType.BOLLINGER_BAND_LOWER_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);
            case BOLLINGER_BAND_UPPER_DIFF:
                return difference(indicators.get(IndicatorType.BOLLINGER_BAND_UPPER_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);
            case BOLLINGER_BAND_MIDDLE_DIFF:
                return difference(indicators.get(IndicatorType.BOLLINGER_BAND_MIDDLE_RAW), indicators.get(IndicatorType.CLOSE_PRICE_RAW), candleId);

            // stochastic oscillator
            case STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D:
                return closeAboveOrBelow(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW),
                        indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId);
            case STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD:
                Double kOver = overBroughtOrSold(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 80, 20);
                Double dOver = overBroughtOrSold(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 80, 20);
                if (TrainingValue.OVER_BROUGHT.getValue() == kOver && TrainingValue.OVER_BROUGHT.getValue() == dOver)
                    return TrainingValue.OVER_BROUGHT.getValue();
                if (TrainingValue.OVER_SOLD.getValue() == kOver && TrainingValue.OVER_SOLD.getValue() == dOver)
                    return TrainingValue.OVER_SOLD.getValue();
                return TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 5);
            case STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING:
                return upwardOrDownwardSloping(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 3);
            case STOCHASTIC_OSCILLATOR_K_ABOVE_D:
                return isClosedAbove(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW),
                        indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId);
            case STOCHASTIC_OSCILLATOR_K_BELOW_D:
                return isClosedBelow(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW),
                        indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId);
            case STOCHASTIC_OSCILLATOR_IS_KD_OVER_BROUGHT:
                boolean kOverBrought = isOverBrought(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 80) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean dOverBrought = isOverSold(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 20) == TrainingValue.INDICATOR_EXIST.getValue();
                if (kOverBrought && dOverBrought)
                    return TrainingValue.INDICATOR_EXIST.getValue();
                return TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case STOCHASTIC_OSCILLATOR_IS_KD_OVER_SOLD:
                boolean kOverSold = isOverBrought(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 80) == TrainingValue.INDICATOR_EXIST.getValue();
                boolean dOverSold = isOverSold(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 20) == TrainingValue.INDICATOR_EXIST.getValue();
                if (kOverSold && dOverSold)
                    return TrainingValue.INDICATOR_EXIST.getValue();
                return TrainingValue.INDICATOR_NOT_EXIST.getValue();
            case STOCHASTIC_OSCILLATOR_IS_K_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 5);
            case STOCHASTIC_OSCILLATOR_IS_K_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW), candleId, 5);
            case STOCHASTIC_OSCILLATOR_IS_D_UPWARD_SLOPING:
                return isUpwardSloping(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 3);
            case STOCHASTIC_OSCILLATOR_IS_D_DOWNWARD_SLOPING:
                return isDownwardSloping(indicators.get(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW), candleId, 3);

            // others
            case VOLUME_RAW:
                return indicators.get(IndicatorType.VOLUME_RAW).getValue(candleId).toDouble();
            case IS_TOMORROW_HOLIDAY:
                return isTomorrowHoliday(candleId);
            case IS_YESTERDAY_HOLIDAY:
                return isYesterdayHoliday(candleId);
            default:
                log.info("fail to get indicator value : " + indicatorType.name());
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
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
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

    /**
     * check is yesterday holiday.
     *
     * @param candleId
     * @return
     */
    private Double isYesterdayHoliday(int candleId) {
        RawCandlestick currentCandle = rawCandlestickList.get(candleId);
        LocalDate today = currentCandle.getRawCandlestickKey().getDateTime().atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate prevDateCandle = today;
        // get the next date time
        for (int i = candleId ;i >= 0 ; i--) {
            prevDateCandle = rawCandlestickList.get(i).getPrevDateTime().atOffset(ZoneOffset.UTC).toLocalDate();
            if (!today.isEqual(prevDateCandle)) {
                break;
            }
        }

        if (!today.minusDays(1).isEqual(prevDateCandle)) {
            return TrainingValue.INDICATOR_EXIST.getValue();

        }
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }

    /**
     * check is tomorrow holiday.
     *
     * @param candleId
     * @return
     */
    private Double isTomorrowHoliday(int candleId) {
        RawCandlestick currentCandle = rawCandlestickList.get(candleId);
        LocalDate today = currentCandle.getRawCandlestickKey().getDateTime().atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate nextDateCandle = today;
        // get the next date time
        for (int i = candleId ;i <= rawCandlestickList.size() ; i++) {
            nextDateCandle = rawCandlestickList.get(i).getNextDateTime().atOffset(ZoneOffset.UTC).toLocalDate();
            if (!today.isEqual(nextDateCandle)) {
                break;
            }
        }

        if (!today.plusDays(1).isEqual(nextDateCandle)) {
            return TrainingValue.INDICATOR_EXIST.getValue();

        }
        return TrainingValue.INDICATOR_NOT_EXIST.getValue();
    }
}
