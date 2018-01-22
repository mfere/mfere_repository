package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedSellTakeProfit extends FixedTakeProfit {

    public FixedSellTakeProfit(String name, int interval, int pips) {
        super(name, interval, pips);
    }

    protected ActionType chooseActionType(RawCandlestick nextCandlestick,
                                          double buyValue,
                                          double sellValue){
        if (nextCandlestick.getMidRawCandlestickData().getLow() <= sellValue) {
            return ActionType.SELL;
        } else {
            return ActionType.NOTHING;
        }
    }

    @Override
    public ActionType getPredictedActionType(INDArray prediction, double probabilityTreshold) {
        return prediction.getDouble(1) > probabilityTreshold ? ActionType.SELL : ActionType.NOTHING;
    }

}
