package com.self_learning.q_money.portfolio;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.self_learning.q_money.dto.AnnualizedReturn;
import com.self_learning.q_money.dto.PortfolioTrade;
import com.self_learning.q_money.exception.StockQuoteServiceException;

public interface PortfolioManager {
    List<AnnualizedReturn> calculateAnnualizedReturnParallel(
            List<PortfolioTrade> portfolioTrades,
            LocalDate endDate, int numThreads) throws InterruptedException,
            StockQuoteServiceException;

    List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
            LocalDate endDate)
            throws StockQuoteServiceException, JsonProcessingException;
}
