package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;

@AllArgsConstructor
public enum IndicatorType {
    CLOSE_PRICE_RAW("close_price_raw"),
    VOLUME_RAW("volume"),

    BEARISH_ENGULFING_CANDLE("bearish_engulfing_candle"),
    BULLISH_ENGULFING_CANDLE("bullish_engulfing_candle"),

    BEARISH_HARAM_CANDLE("bearish_bullish_haram_candle"),
    BULLISH_HARAM_CANDLE("bullish_haram_candle"),


    // Simple moving average
    SMA_5_RAW("sma_5_raw"),
    SMA_5_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below"),
    SMA_5_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward"),

    SMA_5_IS_CLOSE_ABOVE("sma_5_is_close_above"),
    SMA_5_IS_CLOSE_BELOW("sma_5_is_close_below"),
    SMA_5_IS_UPWARD_SLOPING("sma_5_is_upward_sloping"),
    SMA_5_IS_DOWNWARD_SLOPING("sma_5_is_downward_sloping"),
    SMA_5_CLOSE_DIFF("sma_5_close_diff"),
    SMA_5_DIFF_WITH_YESTERDAY("sma_5_diff_with_yesterday"),


    SMA_10_RAW("sma_10_raw"),
    SMA_10_CLOSE_ABOVE_OR_BELOW("sma_50_close_above_or_below"),
    SMA_10_UPWARD_OR_DOWNWARD("sma_50_upward_or_downward"),

    SMA_10_IS_CLOSE_ABOVE("sma_10_is_close_above"),
    SMA_10_IS_CLOSE_BELOW("sma_10_is_close_below"),
    SMA_10_IS_UPWARD_SLOPING("sma_10_is_upward_sloping"),
    SMA_10_IS_DOWNWARD_SLOPING("sma_10_is_downward_sloping"),
    SMA_10_CLOSE_DIFF("sma_10_close_diff"),
    SMA_10_DIFF_WITH_YESTERDAY("sma_10_diff_with_yesterday"),


    SMA_50_RAW("sma_50_raw"),
    SMA_50_CLOSE_ABOVE_OR_BELOW("sma_50_close_above_or_below"),
    SMA_50_UPWARD_OR_DOWNWARD("sma_50_upward_or_downward"),

    SMA_50_IS_CLOSE_ABOVE("sma_50_is_close_above"),
    SMA_50_IS_CLOSE_BELOW("sma_50_is_close_below"),
    SMA_50_IS_UPWARD_SLOPING("sma_50_is_upward_sloping"),
    SMA_50_IS_DOWNWARD_SLOPING("sma_50_is_downward_sloping"),
    SMA_50_CLOSE_DIFF("sma_50_close_diff"),
    SMA_50_DIFF_WITH_YESTERDAY("sma_50_diff_with_yesterday"),


    SMA_100_RAW("sma_100_raw"),
    SMA_100_CLOSE_ABOVE_OR_BELOW("sma_100_close_above_or_below"),
    SMA_100_UPWARD_OR_DOWNWARD("sma_100_upward_or_downward"),

    SMA_100_IS_CLOSE_ABOVE("sma_100_is_close_above"),
    SMA_100_IS_CLOSE_BELOW("sma_100_is_close_below"),
    SMA_100_IS_UPWARD_SLOPING("sma_100_is_upward_sloping"),
    SMA_100_IS_DOWNWARD_SLOPING("sma_100_is_downward_sloping"),
    SMA_100_CLOSE_DIFF("sma_100_close_diff"),
    SMA_100_DIFF_WITH_YESTERDAY("sma_100_diff_with_yesterday"),

    SMA_200_RAW("sma_200_raw"),
    SMA_200_CLOSE_ABOVE_OR_BELOW("sma_200_close_above_or_below"),
    SMA_200_UPWARD_OR_DOWNWARD("sma_200_upward_or_downward"),

    SMA_200_IS_CLOSE_ABOVE("sma_200_is_close_above"),
    SMA_200_IS_CLOSE_BELOW("sma_200_is_close_below"),
    SMA_200_IS_UPWARD_SLOPING("sma_200_is_upward_sloping"),
    SMA_200_IS_DOWNWARD_SLOPING("sma_200_is_downward_sloping"),
    SMA_200_CLOSE_DIFF("sma_200_close_diff"),
    SMA_200_DIFF_WITH_YESTERDAY("sma_200_diff_with_yesterday"),

    // MACD
    MACD_RAW("macd_raw"),
    MACD_div_positive_or_negative("macd_div_positive_or_negative"),

    MACD_IS_DIV_POSITIVE("macd_is_div_positive"),
    MACD_IS_DIV_NEGATIVE("macd_is_div_negative"),

    MACD_DIFF_WITH_YESTERDAY("MACD_diff_with_yesterday"),

    //RSI
    RSI_RAW("rsi_raw"),
    RSI_OVER_BROUGHT_OR_SOLD("rsi_over_brought_or_sold"),
    RSI_UPWARD_OR_DOWNWARD_SLOPING("rsi_5_upward_or_downward_sloping"),

    RSI_IS_OVER_BROUGHT("rsi_is_over_brought"),
    RSI_IS_OVER_SOLD("rsi_is_over_sold"),
    RSI_IS_UPWARD_SLOPING("rsi_is_upward_sloping"),
    RSI_IS_DOWNWARD_SLOPING("rsi_is_downward_sloping"),

    RSI_DIFF_WITH_YESTERDAY("RSI_diff_with_yesterday"),

    // Bollinger Band
    BOLLINGER_BAND_LOWER_RAW("bollinger_band_lower_raw"),
    BOLLINGER_BAND_UPPER_RAW("bollinger_band_upper_raw"),
    BOLLINGER_BAND_MIDDLE_RAW("bollinger_band_middle_raw"),
    BOLLINGER_BAND_WIDTH_RAW("bollinger_band_width_raw"),
    BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW("bollinger_band_upper_close_above_or_below"),
    BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW("bollinger_band_upper_lower_above_or_below"),
    BOLLINGER_BAND_EXPANDING_OR_CONTRACTING("bollinger_band_expanding_or_contracting"),

    BOLLINGER_BAND_UPPER_IS_CLOSE_ABOVE("bollinger_band_upper_is_close_above"),
    BOLLINGER_BAND_UPPER_IS_CLOSE_BELOW("bollinger_band_upper_is_close_below"),
    BOLLINGER_BAND_LOWER_IS_CLOSE_ABOVE("bollinger_band_upper_lower_is_close_above"),
    BOLLINGER_BAND_LOWER_IS_CLOSE_BELOW("bollinger_band_upper_lower_is_close_below"),
    BOLLINGER_BAND_IS_EXPANDING("bollinger_band_is_expanding"),
    BOLLINGER_BAND_IS_CONTRACTING("bollinger_band_is_contracting"),
    BOLLINGER_BAND_LOWER_DIFF("bollinger_band_lower_diff"),
    BOLLINGER_BAND_UPPER_DIFF("bollinger_band_upper_diff"),
    BOLLINGER_BAND_MIDDLE_DIFF("bollinger_band_middle_diff"),

    BOLLINGER_BAND_LOWER_DIFF_WITH_YESTERDAY("bollinger_band_lower_diff_with_yesterday"),
    BOLLINGER_BAND_UPPER_DIFF_WITH_YESTERDAY("bollinger_band_upper_diff_with_yesterday"),
    BOLLINGER_BAND_MIDDLE_DIFF_WITH_YESTERDAY("bollinger_band_middle_diff_with_yesterday"),
    BOLLINGER_BAND_WIDTH_DIFF_WITH_YESTERDAY("bollinger_band_width_diff_with_yesterday"),


    // Stochastic Oscillator
    STOCHASTIC_OSCILLATOR_K_RAW("stochastic_oscillator_K_raw"),
    STOCHASTIC_OSCILLATOR_D_RAW("stochastic_oscillator_D_raw"),
    STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D("stochastic_oscillator_K_above_or_below_D"),
    STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD("stochastic_oscillator_KD_over_brought_or_sold"),
    STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING("stochastic_oscillator_K_upward_or_downward_sloping"),
    STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING("stochastic_oscillator_D_upward_or_downward_sloping"),

    STOCHASTIC_OSCILLATOR_K_ABOVE_D("stochastic_oscillator_is_K_above_D"),
    STOCHASTIC_OSCILLATOR_K_BELOW_D("stochastic_oscillator_is_K_below_D"),
    STOCHASTIC_OSCILLATOR_IS_KD_OVER_BROUGHT("stochastic_oscillator_is_KD_over_brought"),
    STOCHASTIC_OSCILLATOR_IS_KD_OVER_SOLD("stochastic_oscillator_is_KD_over_sold"),
    STOCHASTIC_OSCILLATOR_IS_K_UPWARD_SLOPING("stochastic_oscillator_is_K_upward_sloping"),
    STOCHASTIC_OSCILLATOR_IS_K_DOWNWARD_SLOPING("stochastic_oscillator_is_K_downward_sloping"),
    STOCHASTIC_OSCILLATOR_IS_D_UPWARD_SLOPING("stochastic_oscillator_D_upward_sloping"),
    STOCHASTIC_OSCILLATOR_IS_D_DOWNWARD_SLOPING("stochastic_oscillator_D_downward_sloping"),

    STOCHASTIC_OSCILLATOR_K_DIFF_WITH_YESTERDAY("stochastic_oscillator_K_diff_with_yesterday"),
    STOCHASTIC_OSCILLATOR_D_DIFF_WITH_YESTERDAY("stochastic_oscillator_D_diff_with_yesterday"),

    // holiday
    IS_YESTERDAY_HOLIDAY("is_yesterday_holiday"),
    IS_TOMORROW_HOLIDAY("is_tomorrow_holiday");

    String name;

}
