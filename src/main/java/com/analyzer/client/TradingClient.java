package com.analyzer.client;

import com.analyzer.enricher.action.Action;

public interface TradingClient {

    String doAction(Action action);
}
