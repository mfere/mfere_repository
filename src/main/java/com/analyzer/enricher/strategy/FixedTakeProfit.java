package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.ActionStrategy;
import com.analyzer.model.repository.RawCandlestickRepository;

public abstract class FixedTakeProfit extends Strategy {

    FixedTakeProfit(String name, int interval, int pipNumber) {
        super(name, interval, pipNumber, pipNumber);
    }

    public ActionStrategy getCorrectActionStrategy(RawCandlestickRepository rawCandlestickRepository,
                                                   RawCandlestick closePriceCandlestick) {
        if (closePriceCandlestick.getNextDateTime() == null) {
            new ActionStrategy(name, ActionType.NOTHING.getValue());
        }
        GranularityType granularity = GranularityType.valueOf(closePriceCandlestick.getRawCandlestickKey().getGranularity());
        InstrumentValue instrument = InstrumentValue.valueOf(closePriceCandlestick.getRawCandlestickKey().getInstrument());
        Double closeValue = closePriceCandlestick.getMidRawCandlestickData().getClose();
        double sellValue = closeValue - instrument.getDistance(takeProfitPipNumber);
        double buyValue = closeValue + instrument.getDistance(takeProfitPipNumber);
        ActionType selectedLabel = getCorrectActionType(rawCandlestickRepository,closePriceCandlestick, granularity, interval, instrument, sellValue, buyValue);
        return new ActionStrategy(name, selectedLabel.getValue());
    }

    private ActionType getCorrectActionType(RawCandlestickRepository rawCandlestickRepository,
                                            RawCandlestick nextCandlestick,
                                            GranularityType granularity, int interval,
                                            InstrumentValue instrument, double sellValue, double buyValue) {
        ActionType selectedLabel = ActionType.NOTHING;
        for (int i = 0; i < interval; i++) {
            selectedLabel = chooseActionType(nextCandlestick, buyValue, sellValue);
            if (selectedLabel.equals(ActionType.BOTH)) {
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
                            return selectedLabel;
                    }
                    granularity = lowerGranularity;
                    nextCandlestick = rawCandlestickRepository.findOne(
                            prevCandlestick.getNextDateTime(),
                            granularity,
                            instrument);
                } while (nextCandlestick == null);
                selectedLabel = getCorrectActionType(rawCandlestickRepository,
                        nextCandlestick, lowerGranularity, lowerInterval, instrument, sellValue, buyValue);
            } else if (!selectedLabel.equals(ActionType.NOTHING)) {
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

    protected abstract ActionType chooseActionType(
            RawCandlestick nextCandlestick,
            double buyValue,
            double sellValue);

}
