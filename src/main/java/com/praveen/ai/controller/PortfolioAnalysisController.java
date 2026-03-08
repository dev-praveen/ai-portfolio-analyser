package com.praveen.ai.controller;

import com.praveen.ai.domain.Model;
import com.praveen.ai.error.DatabaseException;
import com.praveen.ai.error.LLMException;
import com.praveen.ai.service.PortfolioService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class PortfolioAnalysisController {

  private final PortfolioService portfolioService;

  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping(path = "/analyze", produces = "application/json")
  public ResponseEntity<List<Model.PortfolioAnalysisResponse>> analyzePortfolio(
      @RequestParam Model.Exchange exchange,
      @RequestBody Model.SymbolAndPriceList symbolAndAveragePriceList,
      @RequestParam Model.Horizon horizon,
      @RequestParam Model.RiskProfile riskProfile)
      throws DatabaseException, LLMException {

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

    final List<Model.PortfolioAnalysisResponse> portFolioAnalysis =
        portfolioService.getPortFolioAnalysis(portfolioAnalysisRequest);

    log.info(
        "Portfolio Analysis completed. Check the response for detailed insights on each stock.");

    return ResponseEntity.ok(portFolioAnalysis);
  }
}
