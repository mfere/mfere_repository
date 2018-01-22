package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StrategyType {
    BS_TAKE_PROFIT_001_24(4),
    BS_TAKE_PROFIT_004_24(4),
    BS_TAKE_PROFIT_005_24(4),
    B_TAKE_PROFIT_001_24(2),
    B_TAKE_PROFIT_005_24(2),
    S_TAKE_PROFIT_001_24(2),
    S_TAKE_PROFIT_005_24(2);

    int labelNumber;
}
