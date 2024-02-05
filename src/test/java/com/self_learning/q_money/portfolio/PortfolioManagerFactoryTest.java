package com.self_learning.q_money.portfolio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class PortfolioManagerFactoryTest {
    @Mock
  private RestTemplate restTemplate;

  @Test
  void getPortfolioManager() {
    Assertions.assertTrue(PortfolioManagerFactory.getPortfolioManager(restTemplate)
        instanceof PortfolioManagerImpl);
  }

  @Test
  void getPortfolioManagerWithStockQuoteService() {
    Assertions.assertTrue(PortfolioManagerFactory.getPortfolioManager("tiingo", restTemplate)
        instanceof PortfolioManagerImpl);
  }

}
