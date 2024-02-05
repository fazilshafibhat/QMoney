package com.self_learning.q_money.quotes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.self_learning.q_money.dto.AlphavantageCandle;
import com.self_learning.q_money.dto.AlphavantageDailyResponse;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.exception.StockQuoteServiceException;

public class AlphavantageService implements StockQuotesService {

    public static final String token = "WQ478W3THBMZ77WU";
    private RestTemplate restTemplate;

    public AlphavantageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
            throws JsonProcessingException, StockQuoteServiceException {
        ObjectMapper om = getObjectMapper();
        List<Candle> stocks = new ArrayList<>();
        String result = restTemplate.getForObject(buildUri(symbol), String.class);
        try {
            AlphavantageDailyResponse alphavantageDailyResponse = om.readValue(result, AlphavantageDailyResponse.class);
            Map<LocalDate, AlphavantageCandle> candles = alphavantageDailyResponse.getCandles();
            String response = restTemplate.getForObject(buildUri(symbol), String.class);
            System.out.println(response);
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                AlphavantageCandle candle = candles.get(date);
                if (candle != null) {
                    candle.setDate(date);
                    stocks.add(candle);
                }
            }
        } catch (NullPointerException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return stocks;
    }

    protected String buildUri(String symbol) {
        String uriTemplate = "https://www.alphavantage.co/query?function=" + "TIME_SERIES_DAILY&symbol=" + symbol
                + "&outputsize=full&apikey=" + token;
        return uriTemplate;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
