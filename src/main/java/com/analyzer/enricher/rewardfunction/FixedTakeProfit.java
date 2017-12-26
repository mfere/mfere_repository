package com.analyzer.enricher.rewardfunction;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;
import com.analyzer.model.repository.RawCandlestickRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class FixedTakeProfit implements RewardFunctionBuilder {

    private final RawCandlestickRepository rawCandlestickRepository;
    private String name;
    private int interval;
    private double distance;

    protected Double maxValue;
    protected Double minValue;
    protected int lastCount = 0;
    protected RawCandlestick lastCheckedCandlestick;
    protected Double referenceCloseValue = null;

    FixedTakeProfit(RawCandlestickRepository rawCandlestickRepository, String name, int interval, double distance) {
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.name = name;
        this.interval = interval;
        this.distance = distance;
    }

    public RewardFunction getRewardFunction(RawCandlestick rawCandlestick) {
        GranularityValue granularity = GranularityValue.getGranularityValue(rawCandlestick.getRawCandlestickKey().getGranularity());
        InstrumentValue instrument = InstrumentValue.getInstrumentValue(rawCandlestick.getRawCandlestickKey().getInstrument());
        Double closeValue = rawCandlestick.getMidRawCandlestickData().getClose();
        double sellValue = closeValue - distance;
        double buyValue = closeValue + distance;
        if (lastCount > 0) {
            lastCount --;
        }

        // First check if I can continue from previously calculated candle to search for label
        // Search only if
        // next candle sell and buy is between min and max
        // next candle close increased compared to previous, but sell value is still after min
        // next candle close decreased compared to previous, but buy value is still below max
        Label selectedLabel;
        if (lastCount == 0 || !(lastCount < interval && lastCheckedCandlestick != null &&
 //               ((sellValue > minValue && buyValue < maxValue) ||
 //               ((closeValue > referenceCloseValue && sellValue > minValue) ||
 //                       (closeValue < referenceCloseValue && buyValue < maxValue)))) {
                (sellValue > minValue && buyValue < maxValue))) {
            lastCount = 0;
            lastCheckedCandlestick = rawCandlestick;
            referenceCloseValue = closeValue;
            minValue = null;
            maxValue = null;
        }

        //lastCount = 0;
        //lastCheckedCandlestick = rawCandlestick;
        //System.out.println("start from "+lastCount + " closevalue: "+closeValue
         //       + " sellValue: "+sellValue + " buyValue: "+buyValue
        //        + " minValue: "+sellValue + " maxValue: "+buyValue);

        selectedLabel = getLabel(lastCheckedCandlestick, granularity, instrument, sellValue, buyValue);
        //System.out.println(selectedLabel);

        return new RewardFunction(name, selectedLabel.getValue());
    }

    private Label getLabel(RawCandlestick rawCandlestick, GranularityValue granularity,
                           InstrumentValue instrument, double sellValue, double buyValue) {
        Label selectedLabel = Label.NOTHING;
        for (int i = lastCount; i < interval && rawCandlestick.getNextDateTime() != null; i++) {
            RawCandlestick nextCandlestick = rawCandlestickRepository.findOne(
                    rawCandlestick.getNextDateTime(),
                    granularity,
                    instrument);
            // Start calculating max value from one candlestick ahead than currently calculated one
            if (i > 0) {
                if (minValue == null || minValue >= rawCandlestick.getMidRawCandlestickData().getLow()) {
                    minValue = rawCandlestick.getMidRawCandlestickData().getLow();
                }
                if (maxValue == null || maxValue > rawCandlestick.getMidRawCandlestickData().getHigh()) {
                    maxValue = rawCandlestick.getMidRawCandlestickData().getHigh();
                }
                lastCheckedCandlestick = nextCandlestick;
                lastCount = i;
            } else {
                lastCheckedCandlestick = null;
            }
            selectedLabel = chooseLabel(nextCandlestick, buyValue, sellValue);
            if (!selectedLabel.equals(Label.NOTHING)) {
                //System.out.println("Found "+selectedLabel+ " at "+i + " - lastCount: "+lastCount);
                //System.out.println("Found "+selectedLabel+ " at "+i);
                break;
            } else {
                rawCandlestick = nextCandlestick;
            }
        }
        return selectedLabel;
    }

    protected abstract Label chooseLabel(
            RawCandlestick nextCandlestick,
            double buyValue,
            double sellValue);

    @AllArgsConstructor
    @Getter
    public enum Label {

        NOTHING(0), // Take profit and Stop loss are not triggered in any case
        BOTH(1),    // Take profit or Stop loss is triggered (not sure who is first)
        BUY(2),    // Only buy take profit will trigger
        SELL(3);   // Only sell take profit will trigger

        private int value;
    }
}
