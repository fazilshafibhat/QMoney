package com.self_learning.q_money.portfolio;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.self_learning.q_money.dto.AnnualizedReturn;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.exception.StockQuoteServiceException;
import com.self_learning.q_money.quotes.StockQuotesService;

public class PortfolioCallable implements Callable<AnnualizedReturn> {
    private PortfolioTrade symbol;
    private LocalDate endDate;
    private StockQuotesService service;
  
      
  
    @Override
    public AnnualizedReturn call() throws Exception {    
      List<Candle> collection = getStockQuote(symbol.getSymbol(), symbol.getPurchaseDate(), endDate);
      AnnualizedReturn x = calculateAnnualizedReturns(endDate, symbol, collection.get(0).getOpen(),
              collection.get(collection.size() - 1).getClose());          
      return x;
    }
  
    public PortfolioCallable(PortfolioTrade symbol, LocalDate endDate, StockQuotesService service) {
      this.symbol = symbol;
      this.endDate = endDate;
      this.service = service;
    }
  
    public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) 
          throws JsonProcessingException, StockQuoteServiceException {

      return this.service.getStockQuote(symbol, from, to);
        
    }
      
    public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, 
        PortfolioTrade trade, Double buyPrice,Double sellPrice) {
      Double totalReturn = (sellPrice - buyPrice) / buyPrice;
      long daysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      double years = (double) daysBetween / 365;
      Double annualret = Math.pow(1 + totalReturn, 1 / years) - 1;
      return new AnnualizedReturn(trade.getSymbol(), annualret, totalReturn);
      
    }
      
      
  }
  
