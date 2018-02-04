package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.nd4j.linalg.api.ndarray.INDArray;

public class FixedSellTakeProfit extends FixedTakeProfit {

    public FixedSellTakeProfit(String name, int interval, int takeProfitPipNumber, int stopLossPipNumber) {
        super(name, interval, takeProfitPipNumber, stopLossPipNumber);
    }

    private double getTakeProfitValue(Double closeValue, InstrumentValue instrumentValue) {
        return closeValue - instrumentValue.getDistance(takeProfitPipNumber);
    }

    private double getStopLossValue(Double closeValue, InstrumentValue instrumentValue) {
        return closeValue + instrumentValue.getDistance(stopLossPipNumber);
    }

    public ActionType chooseActionType(RawCandlestick nextCandlestick,
                                          Double closeValue, InstrumentValue instrumentValue){
        if (nextCandlestick.getMidRawCandlestickData().getLow() <= getTakeProfitValue(closeValue, instrumentValue) &&
                nextCandlestick.getMidRawCandlestickData().getHigh() >= getStopLossValue(closeValue, instrumentValue)) {
            return ActionType.BOTH;
        } else if (nextCandlestick.getMidRawCandlestickData().getLow() <= getTakeProfitValue(closeValue, instrumentValue)) {
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
