package com.analyzer.model;

import com.analyzer.constants.IndicatorType;
import com.analyzer.constants.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class FxLearnData {
    RawCandlestick actionCandlestick;
    List<RawCandlestick> watchCandlesticks;
    StrategyType strategyType;
    List<IndicatorType> indicatorTypes;

    // First column is always reward function
    public String toCsvLine() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        boolean found = false;

        ActionStrategy[] actionStrategies = actionCandlestick.getActionStrategies();
        if (actionStrategies != null) {
            for (ActionStrategy actionStrategy : actionStrategies) {
                if (actionStrategy.getStrategyTypeValue().equals(strategyType.name())) {
                    if (strategyType.getLabelNumber() > 2) {
                        stringBuilder.append(actionStrategy.getActionTypeValue());
                    } else {
                        stringBuilder.append(actionStrategy.getActionTypeValue() > 0 ? 1 : 0);
                    }
                    found = true;
                    break;
                }

            }
            if (!found) {
                throw new Exception("Reward function ("+ strategyType.name()+") " +
                        "value not found for instant " +
                        actionCandlestick.getRawCandlestickKey().getDateTime().toString());
            }
        } else {
            throw new Exception("This candle has no reward functions: "
                    + actionCandlestick.getRawCandlestickKey().getDateTime().toString());

        }
        if (watchCandlesticks == null || watchCandlesticks.size() == 0) {
            throw new Exception("No candlesticks to watch are selected");
        }

        for (RawCandlestick watchCandlestick : watchCandlesticks) {
            addIndicatorsForCandlesticks(watchCandlestick, stringBuilder);
        }
        return stringBuilder.toString();
    }

    private void addIndicatorsForCandlesticks(RawCandlestick actionCandlestick,
                                             StringBuilder stringBuilder) throws Exception {
        boolean found;
        FxIndicator[] fxIndicators = actionCandlestick.getFxIndicators();
        if (fxIndicators != null) {
            for (IndicatorType indicatorType : indicatorTypes) {
                found = false;
                for (FxIndicator indicator : fxIndicators) {
                    if (indicator.getName().equalsIgnoreCase(indicatorType.name())) {
                        stringBuilder.append(",");
                        stringBuilder.append(indicator.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new Exception("Indicator ("+ indicatorType.name()+") "
                            + "value not found for instant " + actionCandlestick.getRawCandlestickKey().getDateTime().toString());
                }
            }
        } else {
            throw new Exception("This candle has no indicators: "
                    + actionCandlestick.getRawCandlestickKey().getDateTime().toString());

        }
    }
}
