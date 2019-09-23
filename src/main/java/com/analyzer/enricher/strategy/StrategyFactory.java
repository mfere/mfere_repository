package com.analyzer.enricher.strategy;

import com.analyzer.constants.StrategyType;

import static com.analyzer.constants.StrategyType.*;

public class StrategyFactory {

    public static Strategy getStrategy(
            StrategyType strategy) throws Exception {
        switch (strategy) {
            case BS_TAKE_PROFIT_001_24: return new FixedBuySellTakeProfit(BS_TAKE_PROFIT_001_24.name(),
                    24,10);
            case BS_TAKE_PROFIT_001_120: return new FixedBuySellTakeProfit(BS_TAKE_PROFIT_001_120.name(),
                    120,10);
            case BS_TAKE_PROFIT_004_24: return new FixedBuySellTakeProfit(BS_TAKE_PROFIT_004_24.name(),
                    24,40);
            case BS_TAKE_PROFIT_005_24: return new FixedBuySellTakeProfit(BS_TAKE_PROFIT_005_24.name(),
                    24,50);
            case B_TAKE_PROFIT_001_24: return new FixedBuyTakeProfit(B_TAKE_PROFIT_001_24.name(),
                    24,10,10);
            case B_TAKE_PROFIT_005_24: return new FixedBuyTakeProfit(B_TAKE_PROFIT_005_24.name(),
                    24,50, 50);
            case S_TAKE_PROFIT_001_24: return new FixedSellTakeProfit(S_TAKE_PROFIT_001_24.name(),
                    24,10, 10);
            case S_TAKE_PROFIT_005_24: return new FixedSellTakeProfit(S_TAKE_PROFIT_005_24.name(),
                    24,50, 50);
            default: throw new Exception("Invalid strategy name");
        }
    }
}
