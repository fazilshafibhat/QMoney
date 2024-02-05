package com.self_learning.q_money.portfolio;
import static java.time.temporal.ChronoUnit.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

import com.self_learning.q_money.dto.AnnualizedReturn;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.quotes.StockQuotesService;
public class AnnualizedReturnTask implements Callable<AnnualizedReturn>{

    private PortfolioTrade portfolioTrade;
    private LocalDate endDate;
    private StockQuotesService stockQuotesService;

    public AnnualizedReturnTask(PortfolioTrade portfolioTrade,StockQuotesService stockQuotesService, LocalDate endDate) {
        this.portfolioTrade = portfolioTrade;
        this.endDate = endDate;
        this.stockQuotesService = stockQuotesService;
    }

    private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
            Double buyPrice, Double sellPrice) {
        double total_num_years = DAYS.between(trade.getPurchaseDate(), endDate) / 365.2422;
        double totalReturns = (sellPrice - buyPrice) / buyPrice;
        double annualized_returns = Math.pow((1.0 + totalReturns), (1.0 / total_num_years)) - 1;
        return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
    }

    @Override
    public AnnualizedReturn call() throws Exception {
        List<Candle> candles = 
                stockQuotesService.getStockQuote(portfolioTrade.getSymbol(),
                portfolioTrade.getPurchaseDate(), endDate);
        return calculateAnnualizedReturns(endDate, portfolioTrade,
                candles.get(0).getOpen(), candles.get(candles.size() - 1).getClose());
        
    }
    
}

