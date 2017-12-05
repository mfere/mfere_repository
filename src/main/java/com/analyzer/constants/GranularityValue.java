package com.analyzer.constants;

import com.oanda.v20.instrument.CandlestickGranularity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GranularityValue {

    /**
     * 5 second candlesticks, minute alignment
     */
    S5("S5", CandlestickGranularity.S5),

    /**
     * 10 second candlesticks, minute alignment
     */
    S10("S10", CandlestickGranularity.S10),

    /**
     * 15 second candlesticks, minute alignment
     */
    S15("S15", CandlestickGranularity.S15),

    /**
     * 30 second candlesticks, minute alignment
     */
    S30("S30", CandlestickGranularity.S30),

    /**
     * 1 minute candlesticks, minute alignment
     */
    M1("M1", CandlestickGranularity.M1),

    /**
     * 2 minute candlesticks, hour alignment
     */
    M2("M2", CandlestickGranularity.M2),

    /**
     * 4 minute candlesticks, hour alignment
     */
    M4("M4", CandlestickGranularity.M4),

    /**
     * 5 minute candlesticks, hour alignment
     */
    M5("M5", CandlestickGranularity.M5),

    /**
     * 10 minute candlesticks, hour alignment
     */
    M10("M10", CandlestickGranularity.M10),

    /**
     * 15 minute candlesticks, hour alignment
     */
    M15("M15", CandlestickGranularity.M15),

    /**
     * 30 minute candlesticks, hour alignment
     */
    M30("M30", CandlestickGranularity.M30),

    /**
     * 1 hour candlesticks, hour alignment
     */
    H1("H1", CandlestickGranularity.H1),

    /**
     * 2 hour candlesticks, day alignment
     */
    H2("H2", CandlestickGranularity.H2),

    /**
     * 3 hour candlesticks, day alignment
     */
    H3("H3", CandlestickGranularity.H3),

    /**
     * 4 hour candlesticks, day alignment
     */
    H4("H4", CandlestickGranularity.H4),

    /**
     * 6 hour candlesticks, day alignment
     */
    H6("H6", CandlestickGranularity.H6),

    /**
     * 8 hour candlesticks, day alignment
     */
    H8("H8", CandlestickGranularity.H8),

    /**
     * 12 hour candlesticks, day alignment
     */
    H12("H12", CandlestickGranularity.H12),

    /**
     * 1 day candlesticks, day alignment
     */
    D("D", CandlestickGranularity.D),

    /**
     * 1 week candlesticks, aligned to start of week
     */
    W("W", CandlestickGranularity.W),

    /**
     * 1 month candlesticks, aligned to first day of the month
     */
    M("M", CandlestickGranularity.M);

    private String name;
    private CandlestickGranularity granularity;

    /**
     * convert Granularity name to exact value
     *
     * @param name: name of Granularity
     * @return a GranularityValue
     */
    public static GranularityValue getGranularityValue(String name) {
        for (GranularityValue value : GranularityValue.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }

}
