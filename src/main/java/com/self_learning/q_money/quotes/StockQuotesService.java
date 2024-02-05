package com.self_learning.q_money.quotes;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.exception.StockQuoteServiceException;

public interface StockQuotesService {
    List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
            throws JsonProcessingException, StockQuoteServiceException;
}
