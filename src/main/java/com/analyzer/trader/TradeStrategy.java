package com.analyzer.trader;

import com.analyzer.client.OandaReaderClient;
import com.analyzer.client.SimulatorClient;
import com.analyzer.client.TradingClient;
import com.analyzer.constants.ActionType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.StrategyType;
import com.analyzer.enricher.action.Action;
import com.analyzer.enricher.strategy.Strategy;
import com.analyzer.enricher.strategy.StrategyFactory;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.AbstractDataSetNormalizer;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class TradeStrategy {
    private static final Logger log = LoggerFactory.getLogger(TradeStrategy.class);

    private final TradingClient tradingClient;
    private final PropertiesConfiguration config;
    private INDArray input;
    private List<String> predictionIndicators;
    private InstrumentValue instrument;
    private StrategyType strategy;

    public TradeStrategy(
            TradingClient tradingClient,
            PropertiesConfiguration config) throws Exception{
        this.tradingClient = tradingClient;
        this.config = config;
        instrument = InstrumentValue.valueOf(config.getString("trade.instrument"));
        strategy = StrategyType.valueOf(config.getString("trade.strategy"));
        // Read predictionIndicators to use
        String indicatorPath = config.getString("model.indicators.path");
        FileInputStream fis = new FileInputStream(indicatorPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        predictionIndicators = (List<String>) ois.readObject();
        ois.close();
    }

    public void setInput(INDArray input) {
        this.input = input;
    }

    public void calculateInput(RawCandlestick rawCandlestick) throws Exception{
        input = Nd4j.zeros(predictionIndicators.size());
        FxIndicator[] fxIndicators = rawCandlestick.getFxIndicators();
        int column = 0;
        for (String indicatorName : predictionIndicators) {
            for (FxIndicator fxIndicator : fxIndicators) {
                if (fxIndicator.getName().equals(indicatorName)) {
                    input.putScalar(0,column, fxIndicator.getValue());
                    column++;
                }
            }
        }
    }

    public Action checkForTrade() throws Exception {
        log.info("Checking trade for instrument: "+ instrument);

        // Create model from provided file
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(config.getString("model.path"));

        // Create normalizer if needed
        AbstractDataSetNormalizer normalizer = null;
        String normalizerPath = config.getString("model.normalizer.path",null);
        if (normalizerPath != null) {
            NormalizerSerializer serializer = NormalizerSerializer.getDefault();
            normalizer = serializer.restore(normalizerPath);
        }

        // Normalize iterator if needed
        if (normalizer != null) {
            normalizer.transform(input);
        }

        // Use model to predict last data
        INDArray prediction = model.output(input,false);
        log.info("predicted probabilities per label s: " + prediction);

        Strategy actionStrategy = StrategyFactory.getStrategy(strategy);
        Action action = actionStrategy.getPredictedAction(
                instrument,
                prediction,
                config.getInt("trade.amount"),
                config.getDouble("trade.probability.treshold")
        );

        // Place order based on result and reward function
        if (!config.getBoolean("trade.simulate")) {
            if (ActionType.SELL == action.getType() || ActionType.BUY == action.getType()) {
                // Retrieve oanda account data
                String transactionId = tradingClient.doAction(action);
                if (transactionId != null) {
                    log.info("Did action: "+transactionId);
                }
            } else {
                log.info("Nothing to do");
            }
        }
        return action;
    }

    public StrategyType getStrategy() {
        return strategy;
    }
}
