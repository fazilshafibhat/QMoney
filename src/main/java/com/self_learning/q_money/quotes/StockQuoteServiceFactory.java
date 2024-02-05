package com.self_learning.q_money.quotes;

import org.springframework.web.client.RestTemplate;

public enum StockQuoteServiceFactory {
    INSTANCE;

    public StockQuotesService getService(String provider, RestTemplate restTemplate) {
        if (provider == null) {
            return new AlphavantageService(restTemplate);
        }
        if ("tiingo".equalsIgnoreCase(provider)) {
            return new TiingoService(restTemplate);
        } else {
            return new AlphavantageService(restTemplate);
        }
    }
}
