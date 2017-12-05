package com.fxanalyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InstrumentValue {

    EUR_USD("EUR_USD"),
    GBP_USD("GBP_USD"),
    USD_CHF("USD_CHF"),
    USD_JPY("USD_JPY");

    private String name;

    /**
     * convert Instrument name to exact value
     *
     * @param name: name of Instrument
     * @return a InstrumentValue
     */
    public static InstrumentValue getInstrumentValue(String name) {
        for (InstrumentValue value : InstrumentValue.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }

}
