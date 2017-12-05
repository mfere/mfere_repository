package com.fxanalyzer.enricher;

import com.fxanalyzer.constants.RewardFunctionValue;
import com.fxanalyzer.model.repository.RawCandlestickRepository;

import static com.fxanalyzer.constants.RewardFunctionValue.*;

public class RewardFunctionFactory {
    static RewardFunctionBuilder getRewardFunction(
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
