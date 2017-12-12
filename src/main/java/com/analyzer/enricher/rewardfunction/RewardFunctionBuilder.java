package com.analyzer.enricher.rewardfunction;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;

public interface RewardFunctionBuilder {
    RewardFunction getRewardFunction(RawCandlestick rawCandlestick);
}
