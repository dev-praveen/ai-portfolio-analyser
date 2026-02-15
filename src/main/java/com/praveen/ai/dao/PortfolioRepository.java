package com.praveen.ai.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praveen.ai.db.tables.PortfolioAnalysis;
import com.praveen.ai.domain.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PortfolioRepository {

  private static final PortfolioAnalysis PORTFOLIO_ANALYSIS_TABLE =
      PortfolioAnalysis.PORTFOLIO_ANALYSIS;

  private final DSLContext dsl;

  private static String convertToJsonString(
      Model.PortfolioAnalysisResponse portfolioAnalysisResponse) {

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(portfolioAnalysisResponse);
    } catch (JsonProcessingException e) {
      log.error("Error converting PortfolioAnalysisResponse to JSON string: {}", e.getMessage());
    }
    return "";
  }

  @Transactional
  public void savePortfolioAnalysis(
      String stockName,
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest,
      Model.PortfolioAnalysisResponse portfolioAnalysisResponse) {

    final String response = convertToJsonString(portfolioAnalysisResponse);

    try {
      dsl.insertInto(PORTFOLIO_ANALYSIS_TABLE)
          .set(PORTFOLIO_ANALYSIS_TABLE.ID, UUID.randomUUID())
          .set(PORTFOLIO_ANALYSIS_TABLE.STOCK_NAME, stockName)
          .set(PORTFOLIO_ANALYSIS_TABLE.STOCK_SYMBOL, portfolioAnalysisResponse.stock())
          .set(PORTFOLIO_ANALYSIS_TABLE.CREATED_AT, OffsetDateTime.now())
          .set(PORTFOLIO_ANALYSIS_TABLE.LAST_UPDATED_AT, OffsetDateTime.now())
          .set(PORTFOLIO_ANALYSIS_TABLE.ANALYSIS_DATA, JSONB.valueOf(response))
          .set(PORTFOLIO_ANALYSIS_TABLE.EXCHANGE, portfolioAnalysisRequest.exchange().toString())
          .set(PORTFOLIO_ANALYSIS_TABLE.HORIZON, portfolioAnalysisRequest.horizon().toString())
          .set(
              PORTFOLIO_ANALYSIS_TABLE.RISK_PROFILE,
              portfolioAnalysisRequest.riskProfile().toString())
          .onConflict(PORTFOLIO_ANALYSIS_TABLE.STOCK_NAME)
          .doUpdate()
          .set(PORTFOLIO_ANALYSIS_TABLE.LAST_UPDATED_AT, OffsetDateTime.now())
          .set(PORTFOLIO_ANALYSIS_TABLE.ANALYSIS_DATA, JSONB.valueOf(response))
          .execute();
    } catch (Exception e) {
      log.error("Error saving portfolio analysis to database: {}", e.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public Optional<Model.PortfolioAnalysisResponse> fetchRecentByStockName(
      String stockName, OffsetDateTime threshold) {

    try {

      var stockRecord =
          dsl.select(PORTFOLIO_ANALYSIS_TABLE.ANALYSIS_DATA)
              .from(PORTFOLIO_ANALYSIS_TABLE)
              .where(PORTFOLIO_ANALYSIS_TABLE.STOCK_NAME.eq(stockName))
              .and(PORTFOLIO_ANALYSIS_TABLE.LAST_UPDATED_AT.lessOrEqual(threshold))
              .fetchOne();

      if (stockRecord == null) {
        return Optional.empty();
      }

      JSONB analysisData = stockRecord.get(PORTFOLIO_ANALYSIS_TABLE.ANALYSIS_DATA);
      if (analysisData == null || analysisData.data().isEmpty()) {
        return Optional.empty();
      }

      ObjectMapper mapper = new ObjectMapper();
      Model.PortfolioAnalysisResponse response =
          mapper.readValue(analysisData.data(), Model.PortfolioAnalysisResponse.class);

      return Optional.of(response);
    } catch (Exception e) {
      log.error("Error fetching recent portfolio analysis for {}: {}", stockName, e.getMessage());
      return Optional.empty();
    }
  }
}
