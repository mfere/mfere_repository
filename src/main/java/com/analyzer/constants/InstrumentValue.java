package com.analyzer.constants;

import com.oanda.v20.primitives.InstrumentName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InstrumentValue {

    EUR_USD(new InstrumentName("EUR/USD"), 0.0001),
    GBP_USD(new InstrumentName("GBP/USD"), 0.0001),
    USD_CHF(new InstrumentName("USD/CHF"), 0.0001),
    USD_JPY(new InstrumentName("USD/JPY"), 0.01);

    public InstrumentName instrumentName;
    public double pipValue;

    public double getDistance(int pips) {
        return pipValue  * pips;
    }

}
