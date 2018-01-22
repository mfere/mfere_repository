package com.analyzer.enricher.action;

import com.analyzer.constants.ActionType;
import com.analyzer.constants.InstrumentValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Action {
    private InstrumentValue instrument;
    private ActionType type;
    private int amount;
    private int takeProfitPips;
    private int stopLossPips;

    public static double getDistance(InstrumentValue instrument, int pips) {
        return instrument.getPipValue() * pips;
    }
}
