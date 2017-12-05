package com.fxanalyzer.enricher;

import com.fxanalyzer.model.RawCandlestick;
import com.fxanalyzer.model.repository.RawCandlestickRepository;

public class FixedSellTakeProfit extends FixedTakeProfit {

    public FixedSellTakeProfit(RawCandlestickRepository rawCandlestickRepository, String name, int interval, double distance) {
        super(rawCandlestickRepository, name, interval, distance);
    }

    protected Label chooseLabel(RawCandlestick nextCandlestick,
                                double buyValue,
                                double sellValue){
        if (nextCandlestick.getBidRawCandlestickData().getLow() <= sellValue) {
            return Label.SELL;
        } else {
            return Label.NOTHING;
        }
    }
}
