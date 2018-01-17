package com.analyzer.client;

import com.analyzer.constants.InstrumentValue;
import com.analyzer.enricher.rewardfunction.RewardFunctionBuilder;
import com.oanda.v20.Context;
import com.oanda.v20.account.*;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PriceValue;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class OandaTradingClient {

    private static final Logger log = LoggerFactory.getLogger(OandaTradingClient.class);


    private final Context ctx;
    private final AccountID accountID;
    private double balance;

    public OandaTradingClient(
            String url,
            String token,
            String accountIdValue
    ) {
        // See http://developer.oanda.com/rest-live-v20/instrument-ep/
        ctx = new Context(url, token);
        accountID = new AccountID(accountIdValue);

        // Make sure we have a valid account
        try {
            // Execute the request and obtain a response object
            AccountListResponse response = ctx.account.list();
            // Retrieve account list from response object
            List<AccountProperties> accountProperties;
            accountProperties = response.getAccounts();
            // Check for the configured account
            boolean hasAccount = false;
            for (AccountProperties account : accountProperties) {
                if (account.getId().equals(accountID))
                    hasAccount = true;
            }
            if (!hasAccount)
                throw new Exception("Account "+accountID+" not found");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Make sure the account has a non zero balance
        try {
            // Execute the request and retrieve a response object
            AccountGetResponse response = ctx.account.get(accountID);
            // Retrieve the contents of the result
            Account account;
            account = response.getAccount();
            // Check the balance
            balance = account.getBalance().doubleValue();
            if (balance <= 0.0) {
                throw new Exception("Account " + accountID + " balance " + balance + " <= 0.0");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String sell(InstrumentValue instrumentValue, RewardFunctionBuilder rewardFunctionBuilder, int amount) {
        return buy(instrumentValue, rewardFunctionBuilder, -amount);
    }

    public String buy(InstrumentValue instrumentValue, RewardFunctionBuilder rewardFunctionBuilder, int amount) {
        InstrumentName instrument = instrumentValue.getInstrumentName();

        try {
            List<InstrumentName> instruments = new ArrayList<>();
            instruments.add(instrumentValue.getInstrumentName());
            PricingGetRequest priceRequest = new PricingGetRequest(accountID, instruments);
            priceRequest.setSince(Instant.now().minusSeconds(30).toString());
            PricingGetResponse resp = ctx.pricing.get(priceRequest);
            Price prices = resp.getPrices().get(resp.getPrices().size()-1);

            // Create the new request
            OrderCreateRequest request = new OrderCreateRequest(accountID);
            // Create the required body parameter
            MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
            // Populate the body parameter fields
            marketOrderRequest.setInstrument(instrument);
            marketOrderRequest.setUnits(amount);
            StopLossDetails stopLossDetails = new StopLossDetails();
            DecimalFormat df = new DecimalFormat("#.#####");
            Double midAmount = (Double.valueOf(prices.getCloseoutBid().toString()) + Double.valueOf(prices.getCloseoutAsk().toString())) / 2;
            if (amount > 0) {
                // TODO refactor reward function is something that makes more sense to that I can use its distance here
                stopLossDetails.setPrice(df.format(midAmount-(midAmount*0.005d)));
            } else {
                stopLossDetails.setPrice(df.format(midAmount+(midAmount*0.005d)));
            }
            TakeProfitDetails takeProfitDetails = new TakeProfitDetails();
            if (amount > 0) {
                takeProfitDetails.setPrice(df.format(midAmount+(midAmount*0.005d)));
            } else {
                takeProfitDetails.setPrice(df.format(midAmount-(midAmount*0.005d)));
            }
            marketOrderRequest.setStopLossOnFill(stopLossDetails);
            marketOrderRequest.setTakeProfitOnFill(takeProfitDetails);
            // Attach the body parameter to the request
            request.setOrder(marketOrderRequest);
            // Execute the request and obtain the response object
            OrderCreateResponse response = ctx.order.create(request);
            // Extract the Order Fill transaction for the executed Market Order
            Transaction transaction = response.getOrderCreateTransaction();
            if (response.getOrderCancelTransaction() != null) {
                throw new Exception("Could not create order:" + response.getOrderCancelTransaction().getReason().toString());
            }
            // Extract the trade ID of the created trade from the transaction and keep it for future action
            return transaction.getId().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
