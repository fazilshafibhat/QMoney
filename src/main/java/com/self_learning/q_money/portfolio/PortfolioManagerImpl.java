package com.self_learning.q_money.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

// import org.apache.logging.log4j.util.PropertySource.Comparator;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.self_learning.q_money.dto.AnnualizedReturn;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.exception.StockQuoteServiceException;
import com.self_learning.q_money.quotes.StockQuotesService;

public class PortfolioManagerImpl implements PortfolioManager {
  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(RestTemplate restTemplate, StockQuotesService stockQuoteService) {
    this.restTemplate = restTemplate;
  }

  public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException, StockQuoteServiceException {

    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for (PortfolioTrade portfolioTrade : portfolioTrades) {
      List<Candle> collection = getStockQuote(portfolioTrade.getSymbol(), portfolioTrade.getPurchaseDate(), endDate);
      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, portfolioTrade,
          collection.get(0).getOpen(),
          collection.get(collection.size() - 1).getClose());
      annualizedReturns.add(annualizedReturn);
    }
    return annualizedReturns.stream().sorted(getComparator()).collect(Collectors.toList());
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    LocalDate purchase = trade.getPurchaseDate();
    Double noYears = purchase.until(endDate, ChronoUnit.DAYS) / 365.24;
    Double annualized_returns = Math.pow(1 + totalReturns, (1 / noYears)) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    return stockQuotesService.getStockQuote(symbol, from, to);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate.toString() + "&endDate=" + endDate.toString() + "&token="
        + "c010f4bd4369796a57865b9a9e48b2663c9de69f";
    return uriTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException, StockQuoteServiceException {
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    List<AnnualizedReturn> annualReturns = new ArrayList<AnnualizedReturn>();
    List<Future<AnnualizedReturn>> list = new ArrayList<Future<AnnualizedReturn>>();

    for (PortfolioTrade symbol : portfolioTrades) {
      Callable<AnnualizedReturn> callable = new PortfolioCallable(symbol, endDate, this.stockQuotesService);
      Future<AnnualizedReturn> future = executor.submit(callable);
      list.add(future);
    }

    for (Future<AnnualizedReturn> fut : list) {
      try {
        annualReturns.add(fut.get());
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException("Execution exception");
      }
    }
    Collections.sort(annualReturns, getComparator());
    executor.shutdown();
    return annualReturns;
  }
}
