package com.self_learning.q_money.dto;

import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public class AlphavantageDailyResponse {
    @JsonProperty(value = "Time Series (Daily)")
    private Map<LocalDate, AlphavantageCandle> candles;

    public Map<LocalDate, AlphavantageCandle> getCandles() {
        return candles;
    }

    public void setCandles(Map<LocalDate, AlphavantageCandle> candles) {
        this.candles = candles;
    }
}
