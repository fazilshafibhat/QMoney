package com.self_learning.q_money.portfolio;

import org.springframework.web.client.RestTemplate;

import com.self_learning.q_money.quotes.StockQuoteServiceFactory;
import com.self_learning.q_money.quotes.StockQuotesService;

public class PortfolioManagerFactory {
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    return new PortfolioManagerImpl(restTemplate);
  }

  public static PortfolioManagerImpl getPortfolioManager(String provider, RestTemplate restTemplate) {
    StockQuotesService stockQuotesService = StockQuoteServiceFactory.INSTANCE.getService(provider, restTemplate);
    return new PortfolioManagerImpl(stockQuotesService);
  }
}
