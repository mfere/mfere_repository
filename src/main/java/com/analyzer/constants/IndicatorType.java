package com.analyzer.constants;

import lombok.AllArgsConstructor;

import static com.analyzer.constants.IndicatorOperation.DIFFERENCE_LATEST_CLOSE_PRICE;
import static com.analyzer.constants.IndicatorOperation.DIFFERENCE_PREVIOUS;
import static com.analyzer.constants.IndicatorOperation.NO_OPERATION;
@AllArgsConstructor
public enum IndicatorType {
    CLOSE_PRICE_RAW("close_price_raw", NO_OPERATION, null),
    CLOSE_PRICE_LATEST_CLOSE_PRICE_DIFF("close_price_latest_close_diff", DIFFERENCE_LATEST_CLOSE_PRICE, CLOSE_PRICE_RAW),
    CLOSE_PRICE_DIFF_WITH_PREVIOUS("close_price_diff_previous", DIFFERENCE_PREVIOUS, CLOSE_PRICE_RAW),
    OPEN_PRICE_RAW("open_price_raw", NO_OPERATION, null),
    VOLUME_RAW("volume", NO_OPERATION, null),

    BEARISH_ENGULFING_CANDLE("bearish_engulfing_candle", NO_OPERATION, null),
    BULLISH_ENGULFING_CANDLE("bullish_engulfing_candle", NO_OPERATION, null),

    BEARISH_HARAM_CANDLE("bearish_bullish_haram_candle", NO_OPERATION, null),
    BULLISH_HARAM_CANDLE("bullish_haram_candle", NO_OPERATION, null),

    // Simple moving average
    SMA_5_RAW("sma_5_raw", NO_OPERATION, null),
    SMA_5_CLOSE_ABOVE_OR_BELOW("sma_5_close_above_or_below", NO_OPERATION, null),
    SMA_5_UPWARD_OR_DOWNWARD("sma_5_upward_or_downward", NO_OPERATION, null),

    SMA_5_IS_CLOSE_ABOVE("sma_5_is_close_above", NO_OPERATION, null),
    SMA_5_IS_CLOSE_BELOW("sma_5_is_close_below", NO_OPERATION, null),
    SMA_5_IS_UPWARD_SLOPING("sma_5_is_upward_sloping", NO_OPERATION, null),
    SMA_5_IS_DOWNWARD_SLOPING("sma_5_is_downward_sloping", NO_OPERATION, null),
    SMA_5_CLOSE_DIFF("sma_5_close_diff", NO_OPERATION, null),
    SMA_5_LATEST_CLOSE_DIFF("sma_5_latest_close_diff", DIFFERENCE_LATEST_CLOSE_PRICE, SMA_5_RAW),
    SMA_5_DIFF_WITH_PREVIOUS("sma_5_diff_with_yesterday", DIFFERENCE_PREVIOUS, SMA_5_RAW),


    SMA_10_RAW("sma_10_raw", NO_OPERATION, null),
    SMA_10_CLOSE_ABOVE_OR_BELOW("sma_50_close_above_or_below", NO_OPERATION, null),
    SMA_10_UPWARD_OR_DOWNWARD("sma_50_upward_or_downward", NO_OPERATION, null),

    SMA_10_IS_CLOSE_ABOVE("sma_10_is_close_above", NO_OPERATION, null),
    SMA_10_IS_CLOSE_BELOW("sma_10_is_close_below", NO_OPERATION, null),
    SMA_10_IS_UPWARD_SLOPING("sma_10_is_upward_sloping", NO_OPERATION, null),
    SMA_10_IS_DOWNWARD_SLOPING("sma_10_is_downward_sloping", NO_OPERATION, null),
    SMA_10_CLOSE_DIFF("sma_10_close_diff", NO_OPERATION, null),
    SMA_10_LATEST_CLOSE_DIFF("sma_10_latest_close_diff", DIFFERENCE_LATEST_CLOSE_PRICE, SMA_5_RAW),
    SMA_10_DIFF_WITH_PREVIOUS("sma_10_diff_with_yesterday", DIFFERENCE_PREVIOUS, SMA_10_RAW),
    
    SMA_50_RAW("sma_50_raw", NO_OPERATION, null),
    SMA_50_CLOSE_ABOVE_OR_BELOW("sma_50_close_above_or_below", NO_OPERATION, null),
    SMA_50_UPWARD_OR_DOWNWARD("sma_50_upward_or_downward", NO_OPERATION, null),

    SMA_50_IS_CLOSE_ABOVE("sma_50_is_close_above", NO_OPERATION, null),
    SMA_50_IS_CLOSE_BELOW("sma_50_is_close_below", NO_OPERATION, null),
    SMA_50_IS_UPWARD_SLOPING("sma_50_is_upward_sloping", NO_OPERATION, null),
    SMA_50_IS_DOWNWARD_SLOPING("sma_50_is_downward_sloping", NO_OPERATION, null),
    SMA_50_CLOSE_DIFF("sma_50_close_diff", NO_OPERATION, null),
    SMA_50_LATEST_CLOSE_DIFF("sma_50_latest_close_diff", DIFFERENCE_LATEST_CLOSE_PRICE, SMA_5_RAW),
    SMA_50_DIFF_WITH_PREVIOUS("sma_50_diff_with_yesterday", DIFFERENCE_PREVIOUS, SMA_50_RAW),
    
    SMA_100_RAW("sma_100_raw", NO_OPERATION, null),
    SMA_100_CLOSE_ABOVE_OR_BELOW("sma_100_close_above_or_below", NO_OPERATION, null),
    SMA_100_UPWARD_OR_DOWNWARD("sma_100_upward_or_downward", NO_OPERATION, null),

    SMA_100_IS_CLOSE_ABOVE("sma_100_is_close_above", NO_OPERATION, null),
    SMA_100_IS_CLOSE_BELOW("sma_100_is_close_below", NO_OPERATION, null),
    SMA_100_IS_UPWARD_SLOPING("sma_100_is_upward_sloping", NO_OPERATION, null),
    SMA_100_IS_DOWNWARD_SLOPING("sma_100_is_downward_sloping", NO_OPERATION, null),
    SMA_100_CLOSE_DIFF("sma_100_close_diff", NO_OPERATION, null),
    SMA_100_LATEST_CLOSE_DIFF("sma_100_latest_close_diff", DIFFERENCE_LATEST_CLOSE_PRICE, SMA_100_RAW),
    SMA_100_DIFF_WITH_PREVIOUS("sma_100_diff_with_yesterday", DIFFERENCE_PREVIOUS, SMA_100_RAW),

    SMA_200_RAW("sma_200_raw", NO_OPERATION, null),
    SMA_200_CLOSE_ABOVE_OR_BELOW("sma_200_close_above_or_below", NO_OPERATION, null),
    SMA_200_UPWARD_OR_DOWNWARD("sma_200_upward_or_downward", NO_OPERATION, null),

    SMA_200_IS_CLOSE_ABOVE("sma_200_is_close_above", NO_OPERATION, null),
    SMA_200_IS_CLOSE_BELOW("sma_200_is_close_below", NO_OPERATION, null),
    SMA_200_IS_UPWARD_SLOPING("sma_200_is_upward_sloping", NO_OPERATION, null),
    SMA_200_IS_DOWNWARD_SLOPING("sma_200_is_downward_sloping", NO_OPERATION, null),
    SMA_200_CLOSE_DIFF("sma_200_close_diff", NO_OPERATION, null),
    SMA_200_LATEST_CLOSE_DIFF("sma_200_latest_close_diff", DIFFERENCE_LATEST_CLOSE_PRICE, SMA_200_RAW),
    SMA_200_DIFF_WITH_PREVIOUS("sma_200_diff_with_yesterday", DIFFERENCE_PREVIOUS, SMA_200_RAW),

    // MACD
    MACD_RAW("macd_raw", NO_OPERATION, null),
    MACD_div_positive_or_negative("macd_div_positive_or_negative", NO_OPERATION, null),

    MACD_IS_DIV_POSITIVE("macd_is_div_positive", NO_OPERATION, null),
    MACD_IS_DIV_NEGATIVE("macd_is_div_negative", NO_OPERATION, null),

    MACD_DIFF_WITH_PREVIOUS("MACD_diff_with_yesterday", DIFFERENCE_PREVIOUS, MACD_RAW),

    //RSI
    RSI_RAW("rsi_raw", NO_OPERATION, null),
    RSI_OVER_BROUGHT_OR_SOLD("rsi_over_brought_or_sold", NO_OPERATION, null),
    RSI_UPWARD_OR_DOWNWARD_SLOPING("rsi_5_upward_or_downward_sloping", NO_OPERATION, null),

    RSI_IS_OVER_BROUGHT("rsi_is_over_brought", NO_OPERATION, null),
    RSI_IS_OVER_SOLD("rsi_is_over_sold", NO_OPERATION, null),
    RSI_IS_UPWARD_SLOPING("rsi_is_upward_sloping", NO_OPERATION, null),
    RSI_IS_DOWNWARD_SLOPING("rsi_is_downward_sloping", NO_OPERATION, null),

    RSI_DIFF_WITH_PREVIOUS("RSI_diff_with_yesterday", DIFFERENCE_PREVIOUS, RSI_RAW),

    // Bollinger Band
    BOLLINGER_BAND_LOWER_RAW("bollinger_band_lower_raw", NO_OPERATION, null),
    BOLLINGER_BAND_UPPER_RAW("bollinger_band_upper_raw", NO_OPERATION, null),
    BOLLINGER_BAND_MIDDLE_RAW("bollinger_band_middle_raw", NO_OPERATION, null),
    BOLLINGER_BAND_WIDTH_RAW("bollinger_band_width_raw", NO_OPERATION, null),
    BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW("bollinger_band_upper_close_above_or_below", NO_OPERATION, null),
    BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW("bollinger_band_upper_lower_above_or_below", NO_OPERATION, null),
    BOLLINGER_BAND_EXPANDING_OR_CONTRACTING("bollinger_band_expanding_or_contracting", NO_OPERATION, null),

    BOLLINGER_BAND_UPPER_IS_CLOSE_ABOVE("bollinger_band_upper_is_close_above", NO_OPERATION, null),
    BOLLINGER_BAND_UPPER_IS_CLOSE_BELOW("bollinger_band_upper_is_close_below", NO_OPERATION, null),
    BOLLINGER_BAND_LOWER_IS_CLOSE_ABOVE("bollinger_band_upper_lower_is_close_above", NO_OPERATION, null),
    BOLLINGER_BAND_LOWER_IS_CLOSE_BELOW("bollinger_band_upper_lower_is_close_below", NO_OPERATION, null),
    BOLLINGER_BAND_IS_EXPANDING("bollinger_band_is_expanding", NO_OPERATION, null),
    BOLLINGER_BAND_IS_CONTRACTING("bollinger_band_is_contracting", NO_OPERATION, null),
    BOLLINGER_BAND_LOWER_DIFF("bollinger_band_lower_diff", NO_OPERATION, null),
    BOLLINGER_BAND_UPPER_DIFF("bollinger_band_upper_diff", NO_OPERATION, null),
    BOLLINGER_BAND_MIDDLE_DIFF("bollinger_band_middle_diff", NO_OPERATION, null),
    BOLLINGER_BAND_LOWER_CLOSE_DIFF("bollinger_band_lower_close_diff", NO_OPERATION, null),
    BOLLINGER_BAND_UPPER_CLOSE_DIFF("bollinger_band_upper_close_diff", NO_OPERATION, null),
    BOLLINGER_BAND_MIDDLE_CLOSE_DIFF("bollinger_band_middle_close_diff", NO_OPERATION, null),
    BOLLINGER_BAND_WIDTH_CLOSE_DIFF("bollinger_band_width_close_diff", NO_OPERATION, null),

    BOLLINGER_BAND_LOWER_DIFF_WITH_PREVIOUS("bollinger_band_lower_diff_with_yesterday", DIFFERENCE_PREVIOUS, BOLLINGER_BAND_LOWER_RAW),
    BOLLINGER_BAND_UPPER_DIFF_WITH_PREVIOUS("bollinger_band_upper_diff_with_yesterday", DIFFERENCE_PREVIOUS, BOLLINGER_BAND_UPPER_RAW),
    BOLLINGER_BAND_MIDDLE_DIFF_WITH_PREVIOUS("bollinger_band_middle_diff_with_yesterday", DIFFERENCE_PREVIOUS, BOLLINGER_BAND_MIDDLE_RAW),
    BOLLINGER_BAND_WIDTH_DIFF_WITH_PREVIOUS("bollinger_band_width_diff_with_yesterday", DIFFERENCE_PREVIOUS, BOLLINGER_BAND_WIDTH_RAW),


    // Stochastic Oscillator
    STOCHASTIC_OSCILLATOR_K_RAW("stochastic_oscillator_K_raw", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_D_RAW("stochastic_oscillator_D_raw", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D("stochastic_oscillator_K_above_or_below_D", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD("stochastic_oscillator_KD_over_brought_or_sold", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING("stochastic_oscillator_K_upward_or_downward_sloping", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING("stochastic_oscillator_D_upward_or_downward_sloping", NO_OPERATION, null),

    STOCHASTIC_OSCILLATOR_K_ABOVE_D("stochastic_oscillator_is_K_above_D", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_K_BELOW_D("stochastic_oscillator_is_K_below_D", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_IS_KD_OVER_BROUGHT("stochastic_oscillator_is_KD_over_brought", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_IS_KD_OVER_SOLD("stochastic_oscillator_is_KD_over_sold", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_IS_K_UPWARD_SLOPING("stochastic_oscillator_is_K_upward_sloping", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_IS_K_DOWNWARD_SLOPING("stochastic_oscillator_is_K_downward_sloping", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_IS_D_UPWARD_SLOPING("stochastic_oscillator_D_upward_sloping", NO_OPERATION, null),
    STOCHASTIC_OSCILLATOR_IS_D_DOWNWARD_SLOPING("stochastic_oscillator_D_downward_sloping", NO_OPERATION, null),

    STOCHASTIC_OSCILLATOR_K_DIFF_WITH_PREVIOUS("stochastic_oscillator_K_diff_with_yesterday", DIFFERENCE_PREVIOUS, STOCHASTIC_OSCILLATOR_K_RAW),
    STOCHASTIC_OSCILLATOR_D_DIFF_WITH_PREVIOUS("stochastic_oscillator_D_diff_with_yesterday", DIFFERENCE_PREVIOUS, STOCHASTIC_OSCILLATOR_D_RAW),

    // holiday
    IS_YESTERDAY_HOLIDAY("is_yesterday_holiday", NO_OPERATION, null),
    IS_TOMORROW_HOLIDAY("is_tomorrow_holiday", NO_OPERATION, null);

    String name;
    // TODO 1. For now only support few operations, need to complete with other unary operations
    // TODO 2. Support multiple argument operations
    public IndicatorOperation operation;
    public IndicatorType operationIndicator;

}
