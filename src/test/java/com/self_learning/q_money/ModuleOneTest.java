package com.self_learning.q_money;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ModuleOneTest {
    @Test
    void mainReadFile() throws Exception {
      //given
      String filename = "assessments/trades.json";
      List<String> expected = Arrays.asList(new String[]{"MSFT", "CSCO", "CTS"});
  
      //when
      List<String> results = PortfolioManagerApplication
          .mainReadFile(new String[]{filename});
  
      //then
      Assertions.assertEquals(expected, results);
    }
  
    @Test
    void mainReadFileEdgecase() throws Exception {
      //given
      String filename = "assessments/empty.json";
      List<String> expected = Arrays.asList(new String[]{});
  
      //when
      List<String> results = PortfolioManagerApplication
          .mainReadFile(new String[]{filename});
  
      //then
      Assertions.assertEquals(expected, results);
    }
  
  }
  