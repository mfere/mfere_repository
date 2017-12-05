package com.analyzer.enricher;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;
import com.analyzer.model.repository.RawCandlestickRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class FixedTakeProfit implements RewardFunctionBuilder {

    private final RawCandlestickRepository rawCandlestickRepository;
    private String name;
    private int interval;
    private double distance;

    public RewardFunction getRewardFunction(RawCandlestick rawCandlestick) {
        GranularityValue granularity = GranularityValue.getGranularityValue(rawCandlestick.getRawCandlestickKey().getGranularity());
        InstrumentValue instrument = InstrumentValue.getInstrumentValue(rawCandlestick.getRawCandlestickKey().getInstrument());
        double sellValue = rawCandlestick.getMidRawCandlestickData().getClose() - distance;
        double buyValue = rawCandlestick.getMidRawCandlestickData().getClose() + distance;

        Label selectedLabel = Label.NOTHING;
        for (int i = 0; i < interval && rawCandlestick.getNextDateTime() != null; i++) {
            RawCandlestick nextCandlestick = rawCandlestickRepository.findOne(
                    rawCandlestick.getNextDateTime(),
                    granularity,
                    instrument);
            selectedLabel = chooseLabel(nextCandlestick, buyValue, sellValue);
            if (selectedLabel != Label.NOTHING) {
                break;
            }
        }
        return new RewardFunction(name, selectedLabel.getValue());
    }

    protected abstract Label chooseLabel(
            RawCandlestick nextCandlestick,
            double buyValue,
            double sellValue);

    @AllArgsConstructor
    @Getter
    public enum Label {

        NOTHING(0), // Take profit and Stop loss are not triggered in any case
        BOTH(1),    // Take profit or Stop loss is triggered (not sure who is first)
        BUY(2),    // Only buy take profit will trigger
        SELL(3);   // Only sell take profit will trigger

        private int value;
    }
}
