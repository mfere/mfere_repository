package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedBuySellTakeProfit extends FixedTakeProfit {

    protected FixedBuySellTakeProfit(String name,
                                  int interval,
                                  int pips) {
        super(name, interval, pips, pips);
    }

    private double getBuyTakeProfitValue(Double closeValue, InstrumentValue instrumentValue) {
        return closeValue + instrumentValue.getDistance(takeProfitPipNumber);
    }

    private double getSellTakeProfitValue(Double closeValue, InstrumentValue instrumentValue) {
        return  closeValue - instrumentValue.getDistance(takeProfitPipNumber);
    }

    public ActionType chooseActionType(RawCandlestick nextCandlestick,
                                          Double closeValue, InstrumentValue instrumentValue){
        if (nextCandlestick.getMidRawCandlestickData().getHigh() >= getBuyTakeProfitValue(closeValue, instrumentValue) &&
                nextCandlestick.getMidRawCandlestickData().getLow() <= getSellTakeProfitValue(closeValue,instrumentValue)) {
            return ActionType.BOTH;
        } else if (nextCandlestick.getMidRawCandlestickData().getHigh() >= getBuyTakeProfitValue(closeValue, instrumentValue)) {
            return ActionType.BUY;
        } else if (nextCandlestick.getMidRawCandlestickData().getLow() <= getSellTakeProfitValue(closeValue,instrumentValue)) {
            return ActionType.SELL;
        } else {
            return null;
        }
    }

    @Override
    public ActionType getPredictedActionType(INDArray prediction, double probabilityTreshold) {
        return prediction.getDouble(2) > probabilityTreshold ? ActionType.BUY :
                prediction.getDouble(3) > probabilityTreshold ? ActionType.SELL : ActionType.NOTHING;
    }

}
