package com.analyzer.enricher.rewardfunction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {

    NOTHING(0), // Take profit and Stop loss are not triggered in any case
    BOTH(1),    // Take profit or Stop loss is triggered (not sure who is first)
    BUY(2),    // Only buy take profit will trigger
    SELL(3);   // Only sell take profit will trigger

    private int value;
}
