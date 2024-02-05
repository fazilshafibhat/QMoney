package com.self_learning.q_money.exception;

public class StockQuoteServiceException extends Exception {

    public StockQuoteServiceException(String message) {
      super(message);
    }
  
    public StockQuoteServiceException(String message, Throwable cause) {
      
      super(message, cause);
    }
  }
  
