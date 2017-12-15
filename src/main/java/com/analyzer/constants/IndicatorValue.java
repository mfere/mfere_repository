package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;

@AllArgsConstructor
@Getter
public enum IndicatorValue {
    CLOSE_PRICE_RAW("close_price_raw"),

    // Simple moving average
    SMA_5_RAW("sma_5_raw"),
    SMA_5_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_5_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    SMA_10("sma_10_raw"),
    SMA_10_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_10_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    SMA_50("sma_50_raw"),
    SMA_50_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_50_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    SMA_100("sma_100_raw"),
    SMA_100_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_100_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    SMA_200("sma_200_raw"),
    SMA_200_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_200_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    // MACD
    MACD_RAW("macd_raw"),
    MACD_div_positive_or_negative("macd_div_positive_or_negative"),

    //RSI
    RSI_RAW("rsi_raw"),
    RIS_OVER_BROUGHT_OR_SOLD("rsi_over_brought_or_sold"),
    RIS_UPWARD_OR_DOWNWARD_SLOPING("rsi_5_upward_or_downward_sloping"),

    // Bollinger Band
    BOLLINGER_BAND_RAW("bollinger_band_raw"),
    BOLLINGER_BAND_LOWER_RAW("bollinger_band_lower_raw"),
    BOLLINGER_BAND_UPPER_RAW("bollinger_band_upper_raw"),
    BOLLINGER_BAND_MIDDLE_RAW("bollinger_band_middle_raw"),
    BOLLINGER_BAND_WIDTH_RAW("bollinger_band_width_raw"),
    BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOWE("bollinger_band_upper_close_above_or_below"),
    BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOWE("bollinger_band_upper_lower_above_or_below"),
    BOLLINGER_BAND_EXPANDING_OR_CONTRACTING("bollinger_band_expanding_or_contracting"),

    //Stochastic Oscillator
    STOCHASTIC_OSCILLATOR_RAW("stochastic_oscillator_raw"),
    STOCHASTIC_OSCILLATOR_K_RAW("stochastic_oscillator_K_raw"),
    STOCHASTIC_OSCILLATOR_D_RAW("stochastic_oscillator_D_raw"),
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
