package com.analyzer.model;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.IndicatorType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.StrategyType;
import com.oanda.v20.instrument.Candlestick;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
public class RawCandlestick implements Persistable {

    @CreatedDate
    private Instant createdDate;

    @Id
    private RawCandlestickKey rawCandlestickKey;

    private Integer volume;
    private RawCandlestickData bidRawCandlestickData;
    private RawCandlestickData askRawCandlestickData;
    private RawCandlestickData midRawCandlestickData;
    private Instant nextDateTime;
    private Instant prevDateTime;
    private FxIndicator[] fxIndicators;
    private ActionStrategy[] actionStrategies;

    private RawCandlestick() {}

    public RawCandlestick(Candlestick candlestick,
                          GranularityType granularity,
                          InstrumentValue instrumentValue,
                          Candlestick prevCandlestick,
                          Candlestick nextCandlestick) throws ParseException {
        midRawCandlestickData = new RawCandlestickData(candlestick.getMid());
        askRawCandlestickData = new RawCandlestickData(candlestick.getAsk());
        bidRawCandlestickData = new RawCandlestickData(candlestick.getBid());
        this.volume = candlestick.getVolume();
        this.prevDateTime = prevCandlestick != null ? Instant.parse(prevCandlestick.getTime()) : null;
        this.nextDateTime = nextCandlestick != null ? Instant.parse(nextCandlestick.getTime()) : null;
        this.rawCandlestickKey = new RawCandlestickKey(
                Instant.parse(candlestick.getTime()),
                granularity.name(),
                instrumentValue.name());
    }

    public void addIndicator(FxIndicator indicator){
        if (fxIndicators == null) {
            fxIndicators = new FxIndicator[1];
            fxIndicators[0] = indicator;
        } else {
            List<FxIndicator> fxIndicatorList = new ArrayList<>();
            fxIndicatorList.addAll(Arrays.asList(fxIndicators));


            boolean alreadyAdded = false;
            for (FxIndicator oldIndicator: fxIndicatorList) {
                if (oldIndicator.getName().equals(indicator.getName())) {
                    oldIndicator.setValue(indicator.getValue());
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                fxIndicatorList.add(indicator);
            }
            fxIndicators = fxIndicatorList.toArray(new FxIndicator[fxIndicatorList.size()]);
        }
    }

    public void addActionStrategy(ActionStrategy actionStrategy){
        if (actionStrategies == null) {
            actionStrategies = new ActionStrategy[1];
            actionStrategies[0] = actionStrategy;
        } else {
            List<ActionStrategy> rewardList = new ArrayList<>();
            rewardList.addAll(Arrays.asList(actionStrategies));

            boolean alreadyAdded = false;
            for (ActionStrategy oldActionStrategy : rewardList) {
                if (oldActionStrategy.getStrategyTypeValue().equals(actionStrategy.getStrategyTypeValue())) {
                    oldActionStrategy.setActionTypeValue(actionStrategy.getActionTypeValue());
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                rewardList.add(actionStrategy);
            }
            actionStrategies = rewardList.toArray(new ActionStrategy[rewardList.size()]);
        }
    }

    // First column is always reward function
    public String toCsvLine(StrategyType strategyType,
                            List<IndicatorType> indicatorTypes,
                            boolean testConvergence) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        boolean found = false;

        //stringBuilder.append(getRawCandlestickKey().dateTime);
        //stringBuilder.append(",");

        if (actionStrategies != null) {
            for (ActionStrategy actionStrategy : actionStrategies) {
                if (actionStrategy.getStrategyTypeValue().equals(strategyType.name())) {
                    stringBuilder.append(actionStrategy.getActionTypeValue());
                    found = true;
                    if (testConvergence) {
                        stringBuilder.append(",");
                        stringBuilder.append(actionStrategy.getActionTypeValue());
                    }
                    break;
                }

            }
            if (!found) {
                throw new Exception("Reward function ("+ strategyType.name()+") " +
                        "value not found for instant " + getRawCandlestickKey().getDateTime().toString());
            }
        } else {
            throw new Exception("This candle has no reward functions: "
                    + getRawCandlestickKey().getDateTime().toString());

        }

        // Add mid high, low, start, end and volume as standard values
        /*stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getHigh());
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getLow());
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getOpen());
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getClose());*/
        //stringBuilder.append(",");
        //stringBuilder.append(volume);

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
                            + "value not found for instant " + getRawCandlestickKey().getDateTime().toString());
                }
            }
        } else {
            throw new Exception("This candle has no indicators: "
                    + getRawCandlestickKey().getDateTime().toString());

        }

        return stringBuilder.toString();
    }

    @Override
    public Serializable getId() {
        return rawCandlestickKey;
    }

    @Override
    public boolean isNew() {
        return createdDate == null;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @ToString
    public static class RawCandlestickKey implements Serializable {
        private Instant dateTime;
        private String granularity;
        private String instrument;

        private RawCandlestickKey(){}

    }
}
