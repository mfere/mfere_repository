package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;

@AllArgsConstructor
@Getter
public enum IndicatorValue {
    CLOSE_PRICE_RAW("close_price_raw"),

    BEARISH_ENGULFING_CANDLE("bearish_engulfing_candle"),
    BULLISH_ENGULFING_CANDLE("bullish_engulfing_candle"),

    BEARISH_HARAM_CANDLE("bearish_bullish_haram_candle"),
    BULLISH_HARAM_CANDLE("bullish_haram_candle"),


    // Simple moving average
    SMA_5_RAW("sma_5_raw"),
    SMA_5_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_5_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    SMA_10_RAW("sma_10_raw"),
    SMA_10_CLOSE_ABOVE_OR_BELOW("sma_10_close_above_or_below"),
    SMA_10_UPWARD_OR_DOWNWARD("sma_10_upward_or_downward"),

    SMA_50_RAW("sma_50_raw"),
    SMA_50_CLOSE_ABOVE_OR_BELOW("sma_50_close_above_or_below"),
    SMA_50_UPWARD_OR_DOWNWARD("sma_50_upward_or_downward"),

    SMA_100_RAW("sma_100_raw"),
    SMA_100_CLOSE_ABOVE_OR_BELOW("sma_100_close_above_or_below"),
    SMA_100_UPWARD_OR_DOWNWARD("sma_100_upward_or_downward"),

    SMA_200_RAW("sma_200_raw"),
    SMA_200_CLOSE_ABOVE_OR_BELOW("sma_200_close_above_or_below"),
    SMA_200_UPWARD_OR_DOWNWARD("sma_200_upward_or_downward"),

    // MACD
    MACD_RAW("macd_raw"),
    MACD_div_positive_or_negative("macd_div_positive_or_negative"),

    //RSI
    RSI_RAW("rsi_raw"),
    RSI_OVER_BROUGHT_OR_SOLD("rsi_over_brought_or_sold"),
    RSI_UPWARD_OR_DOWNWARD_SLOPING("rsi_5_upward_or_downward_sloping"),

    // Bollinger Band
    BOLLINGER_BAND_LOWER_RAW("bollinger_band_lower_raw"),
    BOLLINGER_BAND_UPPER_RAW("bollinger_band_upper_raw"),
    BOLLINGER_BAND_MIDDLE_RAW("bollinger_band_middle_raw"),
    BOLLINGER_BAND_WIDTH_RAW("bollinger_band_width_raw"),
    BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW("bollinger_band_upper_close_above_or_below"),
    BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW("bollinger_band_upper_lower_above_or_below"),
    BOLLINGER_BAND_EXPANDING_OR_CONTRACTING("bollinger_band_expanding_or_contracting"),

    //Stochastic Oscillator
    STOCHASTIC_OSCILLATOR_RAW("stochastic_oscillator_raw"),
    STOCHASTIC_OSCILLATOR_K_RAW("stochastic_oscillator_K_raw"),
    STOCHASTIC_OSCILLATOR_D_RAW("stochastic_oscillator_D_raw"),
    STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D("stochastic_oscillator_K_above_or_below_D"),
    STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD("stochastic_oscillator_KD_over_brought_or_sold"),
    STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING("stochastic_oscillator_K_upward_or_downward_sloping"),
    STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING("stochastic_oscillator_D_upward_or_downward_sloping");




    String name;

    /**
     * convert Indicator name to exact value
     *
     * @param name: name of Indicator
     * @return a IndicatorValue
     */
    public static IndicatorValue getIndicatorValue(String name) {
        for (IndicatorValue value : IndicatorValue.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
