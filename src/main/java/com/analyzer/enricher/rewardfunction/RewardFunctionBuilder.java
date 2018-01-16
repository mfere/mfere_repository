package com.analyzer.enricher.rewardfunction;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;
import org.nd4j.linalg.api.ndarray.INDArray;

public interface RewardFunctionBuilder {


    RewardFunction getRewardFunction(RawCandlestick rawCandlestick);
    Action getAction(INDArray prediction);
}
