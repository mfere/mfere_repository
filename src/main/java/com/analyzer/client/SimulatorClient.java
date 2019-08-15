package com.analyzer.client;

import com.analyzer.enricher.action.Action;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorClient implements TradingClient {

    private static final Logger log = LoggerFactory.getLogger(OandaTradingClient.class);

    private final RawCandlestickRepository rawCandlestickRepository;

    public SimulatorClient(RawCandlestickRepository rawCandlestickRepository) {
        this.rawCandlestickRepository = rawCandlestickRepository;
    }

    @Override
    public String doAction(Action action) {
        return null;
    }
}
