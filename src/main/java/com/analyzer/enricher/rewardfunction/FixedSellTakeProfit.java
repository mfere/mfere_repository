package com.analyzer.enricher.rewardfunction;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedSellTakeProfit extends FixedTakeProfit {

    public FixedSellTakeProfit(RawCandlestickRepository rawCandlestickRepository, String name, int interval, double distance) {
        super(rawCandlestickRepository, name, interval, distance);
    }

    protected Action chooseLabel(RawCandlestick nextCandlestick,
                                 double buyValue,
                                 double sellValue){
        if (nextCandlestick.getMidRawCandlestickData().getLow() <= sellValue) {
            return Action.SELL;
        } else {
            return Action.NOTHING;
        }
    }

    @Override
    public Action getAction(INDArray prediction) {
        return prediction.getDouble(1) > PROBABILITY_THRESHOLD ? Action.SELL : Action.NOTHING;
    }
}
