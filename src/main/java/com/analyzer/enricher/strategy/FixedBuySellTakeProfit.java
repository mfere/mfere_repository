package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedBuySellTakeProfit extends FixedTakeProfit {

    protected FixedBuySellTakeProfit(String name,
                                  int interval,
                                  int pips) {
        super(name, interval, pips);
    }

    protected ActionType chooseActionType(RawCandlestick nextCandlestick,
                                          double buyValue,
                                          double sellValue){
        if (nextCandlestick.getMidRawCandlestickData().getHigh() >= buyValue &&
                nextCandlestick.getMidRawCandlestickData().getLow() <= sellValue) {
            return ActionType.BOTH;
        } else if (nextCandlestick.getMidRawCandlestickData().getHigh() >= buyValue) {
            return ActionType.BUY;
        } else if (nextCandlestick.getMidRawCandlestickData().getLow() <= sellValue) {
            return ActionType.SELL;
        } else {
            return ActionType.NOTHING;
        }
    }

    @Override
    public ActionType getPredictedActionType(INDArray prediction, double probabilityTreshold) {
        return prediction.getDouble(2) > probabilityTreshold ? ActionType.SELL :
                prediction.getDouble(3) > probabilityTreshold ? ActionType.BUY : ActionType.NOTHING;
    }

}
