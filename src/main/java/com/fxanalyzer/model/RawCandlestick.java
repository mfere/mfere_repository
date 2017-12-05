package com.fxanalyzer.model;

import com.fxanalyzer.constants.GranularityValue;
import com.fxanalyzer.constants.IndicatorValue;
import com.fxanalyzer.constants.InstrumentValue;
import com.fxanalyzer.constants.RewardFunctionValue;
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

import static java.util.Arrays.asList;

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
    private RewardFunction[] strategies;

    private RawCandlestick() {}

    public RawCandlestick(Candlestick candlestick,
                          GranularityValue granularity,
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
                granularity.getName(),
                instrumentValue.getName());
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

    public void addRewardFunction(RewardFunction rewardFunction){
        if (strategies == null) {
            strategies = new RewardFunction[1];
            strategies[0] = rewardFunction;
        } else {
            List<RewardFunction> rewardList = new ArrayList<>();
            rewardList.addAll(Arrays.asList(strategies));

            rewardList.add(rewardFunction);
            boolean alreadyAdded = false;
            for (RewardFunction oldRewardFunction: rewardList) {
                if (oldRewardFunction.getName().equals(rewardFunction.getName())) {
                    oldRewardFunction.setValue(rewardFunction.getValue());
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                rewardList.add(rewardFunction);
            }
            strategies = rewardList.toArray(new RewardFunction[rewardList.size()]);
        }
    }

    // First column is always reward function
    public String toCsvLine(RewardFunctionValue rewardFunctionValue,
                            List<IndicatorValue> indicatorValues) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        boolean found = false;

        //stringBuilder.append(getRawCandlestickKey().dateTime);
        //stringBuilder.append(",");

        if (strategies != null) {
            for (RewardFunction rewardFunction : strategies) {
                if (rewardFunction.getName().equals(rewardFunctionValue.getName())) {
                    stringBuilder.append(rewardFunction.getValue());
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new Exception("Reward function ("+rewardFunctionValue.getName()+") " +
                        "value not found for instant " + getRawCandlestickKey().getDateTime().toString());
            }
        } else {
            throw new Exception("This candle has no reward functions: "
                    + getRawCandlestickKey().getDateTime().toString());

        }

        // Add mid high, low, start, end and volume as standard values
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getHigh());
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getLow());
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getOpen());
        stringBuilder.append(",");
        stringBuilder.append(midRawCandlestickData.getClose());
        stringBuilder.append(",");
        stringBuilder.append(volume);

        if (fxIndicators != null) {
            for (IndicatorValue indicatorValue : indicatorValues) {
                found = false;
                for (FxIndicator indicator : fxIndicators) {
                    if (indicator.getName().equals(indicatorValue.getName())) {
                        stringBuilder.append(",");
                        stringBuilder.append(indicator.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new Exception("Indicator ("+indicatorValue.getName()+") "
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
