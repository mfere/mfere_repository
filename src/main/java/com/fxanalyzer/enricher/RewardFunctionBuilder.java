package com.fxanalyzer.enricher;

import com.fxanalyzer.model.RawCandlestick;
import com.fxanalyzer.model.RewardFunction;

public interface RewardFunctionBuilder {
    public RewardFunction getRewardFunction(RawCandlestick rawCandlestick);
}
