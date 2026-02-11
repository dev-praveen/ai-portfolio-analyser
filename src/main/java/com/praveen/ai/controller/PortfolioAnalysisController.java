package com.praveen.ai.controller;

import com.praveen.ai.domain.Model;
import com.praveen.ai.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class PortfolioAnalysisController {

  private final PortfolioService portfolioService;

  @GetMapping(path = "/analyze", produces = "application/json")
  public ResponseEntity<List<Model.StockNewsAnalysis>> analyzePortfolio(
      @RequestParam Model.Exchange exchange,
      @RequestParam Model.SymbolAndPriceList symbolAndAveragePriceList,
      @RequestParam Model.Horizon horizon,
      @RequestParam Model.RiskProfile riskProfile) {

    log.info(
        "Received portfolio analysis request for exchange: {}, stocks: {}, horizon: {}, risk profile: {}",
        exchange,
        symbolAndAveragePriceList,
        horizon,
        riskProfile);
    log.info("Please wait while we analyze your portfolio based on the latest news and events...");

    final var portfolioAnalysisRequest =
        new Model.PortfolioAnalysisRequest(
            exchange, symbolAndAveragePriceList, horizon, riskProfile);

    final List<Model.StockNewsAnalysis> portFolioAnalysis =
        portfolioService.getPortFolioAnalysis(portfolioAnalysisRequest);

    log.info(
        "Portfolio Analysis completed. Check the response for detailed insights on each stock.");

    return ResponseEntity.ok(portFolioAnalysis);
  }
}
