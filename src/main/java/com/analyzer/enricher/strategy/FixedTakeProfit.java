package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.ActionStrategy;
import com.analyzer.model.repository.RawCandlestickRepository;

public abstract class FixedTakeProfit extends Strategy {

    FixedTakeProfit(String name, int interval, int takeProfitPipNumber, int stopLossPipNumber) {
        super(name, interval, takeProfitPipNumber, stopLossPipNumber);
    }

    public ActionStrategy getCorrectActionStrategy(RawCandlestickRepository rawCandlestickRepository,
                                                   RawCandlestick closePriceCandlestick) {
        GranularityType granularity = GranularityType.valueOf(closePriceCandlestick.getRawCandlestickKey().getGranularity());
        InstrumentValue instrument = InstrumentValue.valueOf(closePriceCandlestick.getRawCandlestickKey().getInstrument());
        Double closeValue = closePriceCandlestick.getMidRawCandlestickData().getClose();
        RawCandlestick nextCandlestick = rawCandlestickRepository.findOne(
                closePriceCandlestick.getNextDateTime(),
                granularity,
                instrument);
        ActionType selectedLabel = getCorrectActionType(rawCandlestickRepository,
                nextCandlestick, granularity, interval, instrument, closeValue);
        return new ActionStrategy(name, selectedLabel.getValue());
    }

    private ActionType getCorrectActionType(RawCandlestickRepository rawCandlestickRepository,
                                            RawCandlestick nextCandlestick,
                                            GranularityType granularity, int interval,
                                            InstrumentValue instrument, double closeValue) {
        ActionType selectedLabel = null;
        for (int i = 0; i < interval; i++) {
            if (nextCandlestick == null) {
                return ActionType.NOTHING;
            }
            selectedLabel = chooseActionType(nextCandlestick, closeValue, instrument);
            if (ActionType.BOTH.equals(selectedLabel)) {
                // Find lower granularity value and interval
                GranularityType lowerGranularity;
                int lowerInterval;
                RawCandlestick prevCandlestick = nextCandlestick;
                do {
                    switch (granularity) {
                        case D:
                            lowerGranularity = GranularityType.H12;
                            lowerInterval = 2;
                            break;
                        case H12:
                            lowerGranularity = GranularityType.H4;
                            lowerInterval = 3;
                            break;
                        case H4:
                            lowerGranularity = GranularityType.H1;
                            lowerInterval = 4;
                            break;
                        case H1:
                            lowerGranularity = GranularityType.M30;
                            lowerInterval = 2;
                            break;
                        case M30:
                            lowerGranularity = GranularityType.M1;
                            lowerInterval = 30;
                            break;
                        default:
                            return ActionType.NOTHING;
                    }
                    granularity = lowerGranularity;
                    nextCandlestick = rawCandlestickRepository.findOne(
                            prevCandlestick.getNextDateTime(),
                            granularity,
                            instrument);
                } while (nextCandlestick == null);
                selectedLabel = getCorrectActionType(rawCandlestickRepository,
                        nextCandlestick, lowerGranularity, lowerInterval, instrument, closeValue);
            } else if (selectedLabel != null) {
                break;
            } else {
                nextCandlestick = rawCandlestickRepository.findOne(
                        nextCandlestick.getNextDateTime(),
                        granularity,
                        instrument);
                if (nextCandlestick == null) {
                    return ActionType.NOTHING;
                }
            }
        }
        return selectedLabel != null ? selectedLabel : ActionType.NOTHING;
    }

    public abstract ActionType chooseActionType(RawCandlestick nextCandlestick,
                     Double closeValue, InstrumentValue instrumentValue);

}
