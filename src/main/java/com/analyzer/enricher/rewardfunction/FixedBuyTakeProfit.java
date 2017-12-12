package com.analyzer.enricher.rewardfunction;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;

public class FixedBuyTakeProfit  extends FixedTakeProfit {

    public FixedBuyTakeProfit(RawCandlestickRepository rawCandlestickRepository, String name, int interval, double distance) {
        super(rawCandlestickRepository, name, interval, distance);
    }

    protected Label chooseLabel(RawCandlestick nextCandlestick,
                                double buyValue,
                                double sellValue){
        if (nextCandlestick.getBidRawCandlestickData().getHigh() >= buyValue) {
            return Label.BUY;
        } else {
            return Label.NOTHING;
        }
    }
}
