package com.analyzer.enricher;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;

public interface RewardFunctionBuilder {
    public RewardFunction getRewardFunction(RawCandlestick rawCandlestick);
}
