package com.self_learning.q_money.quotes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.self_learning.q_money.dto.Candle;
import com.self_learning.q_money.dto.TiingoCandle;
import com.self_learning.q_money.exception.StockQuoteServiceException;



public class TiingoService implements StockQuotesService {
    private RestTemplate restTemplate;

    protected TiingoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
            throws JsonProcessingException, StockQuoteServiceException {
        ObjectMapper om = getObjectMapper();
        String result = restTemplate.getForObject(buildUri(symbol, from, to), String.class);
        List<TiingoCandle> collection = om.readValue(result, new TypeReference<ArrayList<TiingoCandle>>() {
        });
        return new ArrayList<Candle>(collection);

    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
        String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
                + startDate.toString() + "&endDate=" + endDate.toString() + "&token="
                + "c010f4bd4369796a57865b9a9e48b2663c9de69f";
        return uriTemplate;
    }
}
