package com.analyzer.enricher.rewardfunction;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;
import com.analyzer.model.repository.RawCandlestickRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class FixedTakeProfit implements RewardFunctionBuilder {

    protected static final double PROBABILITY_THRESHOLD = 0.7;

    private final RawCandlestickRepository rawCandlestickRepository;
    private String name;
    private int interval;
    private double distance;

    public RewardFunction getRewardFunction(RawCandlestick closePriceCandlestick) {
        if (closePriceCandlestick.getNextDateTime() == null) {
            new RewardFunction(name, Action.NOTHING.getValue());
        }
        GranularityValue granularity = GranularityValue.valueOf(closePriceCandlestick.getRawCandlestickKey().getGranularity());
        InstrumentValue instrument = InstrumentValue.valueOf(closePriceCandlestick.getRawCandlestickKey().getInstrument());
        Double closeValue = closePriceCandlestick.getMidRawCandlestickData().getClose();
        double sellValue = closeValue - (closeValue * distance);
        double buyValue = closeValue + (closeValue * distance);
        Action selectedLabel = getLabel(closePriceCandlestick, granularity, interval, instrument, sellValue, buyValue);
        //log.info("found label: "+ selectedLabel);
        return new RewardFunction(name, selectedLabel.getValue());
    }

    private Action getLabel(RawCandlestick nextCandlestick, GranularityValue granularity, int interval,
                            InstrumentValue instrument, double sellValue, double buyValue) {
        Action selectedLabel = Action.NOTHING;
        for (int i = 0; i < interval; i++) {
            selectedLabel = chooseLabel(nextCandlestick, buyValue, sellValue);
            if (selectedLabel.equals(Action.BOTH)) {
                // Find lower granularity value and interval
                GranularityValue lowerGranularity;
                int lowerInterval;
                RawCandlestick prevCandlestick = nextCandlestick;
                do {
                    switch (granularity) {
                        case D:
                            lowerGranularity = GranularityValue.H12;
                            lowerInterval = 2;
                            break;
                        case H12:
                            lowerGranularity = GranularityValue.H4;
                            lowerInterval = 3;
                            break;
                        case H4:
                            lowerGranularity = GranularityValue.H1;
                            lowerInterval = 4;
                            break;
                        case H1:
                            lowerGranularity = GranularityValue.M30;
                            lowerInterval = 2;
                            break;
                        case M30:
                            lowerGranularity = GranularityValue.M1;
                            lowerInterval = 30;
                            break;
                        default:
                            return selectedLabel;
                    }
                    granularity = lowerGranularity;
                    nextCandlestick = rawCandlestickRepository.findOne(
                            prevCandlestick.getNextDateTime(),
                            granularity,
                            instrument);
                } while (nextCandlestick == null);
                selectedLabel = getLabel(nextCandlestick, lowerGranularity, lowerInterval, instrument, sellValue, buyValue);
            } else if (!selectedLabel.equals(Action.NOTHING)) {
                break;
            } else {
                nextCandlestick = rawCandlestickRepository.findOne(
                        nextCandlestick.getNextDateTime(),
                        granularity,
                        instrument);
                if (nextCandlestick == null) {
                    return selectedLabel;
                }
            }
        }
        return selectedLabel;
    }

    protected abstract Action chooseLabel(
            RawCandlestick nextCandlestick,
            double buyValue,
            double sellValue);

}
