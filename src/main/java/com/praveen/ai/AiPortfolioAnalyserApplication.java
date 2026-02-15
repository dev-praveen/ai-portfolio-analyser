package com.praveen.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.praveen.ai.dao.PortfolioRepository;
import com.praveen.ai.domain.Model;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class AiPortfolioAnalyserApplication {

  static void main(String[] args) {
    SpringApplication.run(AiPortfolioAnalyserApplication.class, args);
  }

  /*@Bean
  ApplicationRunner applicationRunner(PortfolioRepository portfolioRepository) {
    return args -> {
      var response =
          new Model.PortfolioAnalysisResponse(
              "AAPL",
              "Apple Inc. reported strong earnings for the quarter, driven by robust iPhone sales and growth in services revenue.",
              List.of("Earnings Report", "Product Launch"),
              "Positive",
              "Stock price rose by 5% following the earnings announcement.",
              new Model.FundamentalImpact(
                  "Revenue increased by 10%",
                  "Margins improved by 2%",
                  "Strong balance sheet with $200B in cash",
                  "Maintains a strong moat with brand loyalty and ecosystem"),
              new Model.TimeHorizonImpact(
                  "Short-term positive due to strong earnings",
                  "Long-term positive due to continued innovation and market leadership"),
              "Moderate",
              false,
              "Valuation remains attractive given growth prospects",
              "Hold",
              "The stock is fairly valued with potential for growth, but we recommend holding to see how the market reacts in the coming weeks.",
              "If the stock price drops by more than 10% or if there are signs of weakening demand for key products.");

      portfolioRepository.savePortfolioAnalysis(response);
    };
  }*/
}
