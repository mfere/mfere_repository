package com.analyzer.enricher.strategy;

import com.analyzer.constants.ActionType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.enricher.action.Action;
import com.analyzer.model.ActionStrategy;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.oanda.v20.primitives.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nd4j.linalg.api.ndarray.INDArray;

import javax.sound.midi.Instrument;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public abstract class Strategy implements Serializable {

    protected String name;
    protected int interval;
    protected int takeProfitPipNumber;
    protected int stopLossPipNumber;

    public abstract ActionStrategy getCorrectActionStrategy(
            RawCandlestickRepository rawCandlestickRepository, RawCandlestick rawCandlestick);
    public Action getPredictedAction(
            InstrumentValue instrument,
            INDArray prediction,
            int amount,
            double probabilityTreshold){
        return new Action(
                instrument,
                getPredictedActionType(prediction, probabilityTreshold),
                amount,
                takeProfitPipNumber,
                stopLossPipNumber
        );
    }

    protected abstract ActionType getPredictedActionType(INDArray prediction, double probabilityTreshold);
}
