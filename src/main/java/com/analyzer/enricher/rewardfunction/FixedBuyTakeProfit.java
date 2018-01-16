package com.analyzer.enricher.rewardfunction;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedBuyTakeProfit  extends FixedTakeProfit {

    public FixedBuyTakeProfit(RawCandlestickRepository rawCandlestickRepository, String name, int interval, double distance) {
        super(rawCandlestickRepository, name, interval, distance);
    }

    protected Action chooseLabel(RawCandlestick nextCandlestick,
                                 double buyValue,
                                 double sellValue){
        if (nextCandlestick.getMidRawCandlestickData().getHigh() >= buyValue) {
            return Action.BUY;
        } else {
            return Action.NOTHING;
        }
    }

    @Override
    public Action getAction(INDArray prediction) {
        return prediction.getDouble(1) > PROBABILITY_THRESHOLD ? Action.BUY : Action.NOTHING;
    }
}
