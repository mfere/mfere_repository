package com.analyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RewardFunctionValue {
    BS_TAKE_PROFIT_001_24("BS_TAKE_PROFIT_001_24",4),
    BS_TAKE_PROFIT_005_24("BS_TAKE_PROFIT_005_24",4),
    BS_TAKE_PROFIT_005_60("BS_TAKE_PROFIT_005_60",4),
    B_TAKE_PROFIT_001_24("B_TAKE_PROFIT_001_24",2),
    B_TAKE_PROFIT_005_24("B_TAKE_PROFIT_005_24",2),
    B_TAKE_PROFIT_005_60("B_TAKE_PROFIT_005_60",2),
    S_TAKE_PROFIT_001_24("S_TAKE_PROFIT_001_24",2),
    S_TAKE_PROFIT_005_24("S_TAKE_PROFIT_005_24",2),
    S_TAKE_PROFIT_005_60("S_TAKE_PROFIT_005_60",2);

    String name;
    int labelNumber;

    /**
     * convert RewardFunction name to exact value
     *
     * @param name: name of RewardFunction
     * @return a RewardFunctionValue
     */
    public static RewardFunctionValue getRewardFunctionValue(String name) {
        for (RewardFunctionValue value : RewardFunctionValue.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
