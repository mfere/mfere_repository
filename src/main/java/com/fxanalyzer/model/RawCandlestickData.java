package com.fxanalyzer.model;

import com.oanda.v20.instrument.CandlestickData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class RawCandlestickData {
    private Double open;
    private Double high;
    private Double low;
    private Double close;

    private RawCandlestickData() {}

    public RawCandlestickData(CandlestickData candlestickData) {
        open = candlestickData.getO().doubleValue();
        high = candlestickData.getH().doubleValue();
        low = candlestickData.getL().doubleValue();
        close = candlestickData.getC().doubleValue();
    }
}
