package com.analyzer.enricher.rewardfunction;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;

public class FixedBuySellTakeProfit extends FixedTakeProfit {

    protected FixedBuySellTakeProfit(RawCandlestickRepository rawCandlestickRepository,
                                  String name,
                                  int interval,
                                  double distance) {
        super(rawCandlestickRepository, name, interval, distance);
    }

    protected Label chooseLabel(RawCandlestick nextCandlestick,
                                double buyValue,
                                double sellValue){
        if (nextCandlestick.getMidRawCandlestickData().getHigh() >= buyValue &&
                nextCandlestick.getMidRawCandlestickData().getLow() <= sellValue) {
            return Label.BOTH;
        } else if (nextCandlestick.getMidRawCandlestickData().getHigh() >= buyValue) {
            return Label.BUY;
        } else if (nextCandlestick.getMidRawCandlestickData().getLow() <= sellValue) {
            return Label.SELL;
        } else {
            return Label.NOTHING;
        }
    }

}
