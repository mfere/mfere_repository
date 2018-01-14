package com.analyzer.client;

import com.oanda.v20.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class OandaTradingClient {

    private static final Logger log = LoggerFactory.getLogger(OandaTradingClient.class);


    private final Context ctx;

    public OandaTradingClient(
            @Value("${oanda.url}") String url,
            @Value("${oanda.token}") String token,
            String accountId

    ) {
        // See http://developer.oanda.com/rest-live-v20/instrument-ep/
        ctx = new Context(url, token);
    }
}
