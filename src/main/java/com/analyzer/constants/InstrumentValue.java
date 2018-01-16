package com.analyzer.constants;

import com.oanda.v20.primitives.InstrumentName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InstrumentValue {

    EUR_USD(new InstrumentName("EUR/USD")),
    GBP_USD(new InstrumentName("GBP/USD")),
    USD_CHF(new InstrumentName("USD/CHF")),
    USD_JPY(new InstrumentName("USD/JPY"));

    public InstrumentName instrumentName;

}
