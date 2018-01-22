package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedBuyTakeProfit  extends FixedTakeProfit {

    public FixedBuyTakeProfit(String name, int interval, int pips) {
        super(name, interval, pips);
    }

    protected ActionType chooseActionType(RawCandlestick nextCandlestick,
                                          double buyValue,
                                          double sellValue){
        if (nextCandlestick.getMidRawCandlestickData().getHigh() >= buyValue) {
            return ActionType.BUY;
        } else {
            return ActionType.NOTHING;
        }
    }

    @Override
    public ActionType getPredictedActionType(INDArray prediction, double probabilityTreshold) {
        return prediction.getDouble(1) > probabilityTreshold ? ActionType.BUY : ActionType.NOTHING;
    }
}
