package com.praveen.ai.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Model {

  public enum Horizon {
    SHORT_TERM,
    MEDIUM_TERM,
    LONG_TERM
  }

  public enum RiskProfile {
    LOW,
    MODERATE,
    HIGH
  }

  public enum Exchange {
    NSE,
    BSE,
    NYSE,
    NASDAQ
  }

  public record StockNewsAnalysis(
      @JsonProperty("stock") String stock,
      @JsonProperty("news_summary") String newsSummary,
      @JsonProperty("news_type") List<String> newsType,
      @JsonProperty("sentiment") String sentiment,
      @JsonProperty("market_reaction") String marketReaction,
      @JsonProperty("fundamental_impact") FundamentalImpact fundamentalImpact,
      @JsonProperty("time_horizon_impact") TimeHorizonImpact timeHorizonImpact,
      @JsonProperty("risk_level") String riskLevel,
      @JsonProperty("thesis_changed") boolean thesisChanged,
      @JsonProperty("valuation_comment") String valuationComment,
      @JsonProperty("recommended_action") String recommendedAction,
      @JsonProperty("action_reason") String actionReason,
      @JsonProperty("invalidation_triggers") String invalidationTriggers) {}

  record FundamentalImpact(
      @JsonProperty("revenue") String revenue,
      @JsonProperty("margins") String margins,
      @JsonProperty("balance_sheet") String balanceSheet,
      @JsonProperty("long_term_moat") String longTermMoat) {}

  record TimeHorizonImpact(
      @JsonProperty("short_term") String shortTerm, @JsonProperty("long_term") String longTerm) {}

  record SymbolAndPrice(String symbol, String avgBuyPrice) {}

  public record SymbolAndPriceList(List<SymbolAndPrice> symbolAndPriceList) {}

  public record PortfolioAnalysisRequest(
      Exchange exchange,
      SymbolAndPriceList symbolAndPriceList,
      Horizon horizon,
      RiskProfile riskProfile) {}
}
