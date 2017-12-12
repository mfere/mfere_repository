package com.analyzer.enricher.rewardfunction;

import com.analyzer.constants.RewardFunctionValue;
import com.analyzer.model.repository.RawCandlestickRepository;

import static com.analyzer.constants.RewardFunctionValue.*;

public class RewardFunctionFactory {
    public static RewardFunctionBuilder getRewardFunction(
            RewardFunctionValue value,
            RawCandlestickRepository repository) {
        switch (value) {
            case BS_TAKE_PROFIT_001_24: return new FixedBuySellTakeProfit(repository, BS_TAKE_PROFIT_001_24.getName(),
                    24,0.001d);
            case BS_TAKE_PROFIT_005_24: return new FixedBuySellTakeProfit(repository, BS_TAKE_PROFIT_005_24.getName(),
                    24,0.005d);
            case BS_TAKE_PROFIT_005_60: return new FixedBuySellTakeProfit(repository, BS_TAKE_PROFIT_005_60.getName(),
                    60,0.005d);
            case B_TAKE_PROFIT_001_24: return new FixedBuyTakeProfit(repository, B_TAKE_PROFIT_001_24.getName(),
                    24,0.001d);
            case B_TAKE_PROFIT_005_24: return new FixedBuyTakeProfit(repository, B_TAKE_PROFIT_005_24.getName(),
                    24,0.005d);
            case B_TAKE_PROFIT_005_60: return new FixedBuyTakeProfit(repository, B_TAKE_PROFIT_005_60.getName(),
                    60,0.005d);
            case S_TAKE_PROFIT_001_24: return new FixedSellTakeProfit(repository, S_TAKE_PROFIT_001_24.getName(),
                    24,0.001d);
            case S_TAKE_PROFIT_005_24: return new FixedSellTakeProfit(repository, S_TAKE_PROFIT_005_24.getName(),
                    24,0.005d);
            case S_TAKE_PROFIT_005_60: return new FixedSellTakeProfit(repository, S_TAKE_PROFIT_005_60.getName(),
                    60,0.005d);
            default: return null;
        }
    }
}
