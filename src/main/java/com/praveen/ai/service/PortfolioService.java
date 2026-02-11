package com.praveen.ai.service;

import com.praveen.ai.domain.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

  private final ChatModel chatModel;

  String prompt =
      """
                     You are a professional equity research analyst.

                     Stocks: {stocksAndAvgPrice}
                     Exchange: {exchange}
                     Horizon: {horizon}
                     RiskProfile: {riskProfile}

                     Task:
                     Search latest real-time news/events related to the stocks.
                     Ignore noise. Focus only on items that materially affect price or fundamentals.
                     Assess whether the news changes the investment thesis.

                     Respond STRICTLY in the following structured fields(as I am mapping the response to Model.StockNewsAnalysis):
                     [
                       {
                         "stock": "",
                         "news_summary": "",
                         "news_type": ["earnings","regulatory","management","macro","sector","competition","one_time","structural"],
                         "sentiment": "bullish|neutral|bearish",
                         "market_reaction": "up|down|flat|unknown",
                         "fundamental_impact": {
                           "revenue": "positive|neutral|negative",
                           "margins": "positive|neutral|negative",
                           "balance_sheet": "positive|neutral|negative",
                           "long_term_moat": "improving|stable|weakening"
                         },
                         "time_horizon_impact": {
                           "short_term": "",
                           "long_term": ""
                         },
                         "risk_level": "low|medium|high",
                         "thesis_changed": true|false,
                         "valuation_comment": "",
                         "recommended_action": "exit|partial_exit|hold|accumulate|buy",
                         "action_reason": "",
                         "invalidation_triggers": ""
                       }
                     ]

                     Rules:
                     - Be objective and concise.
                     - Separate facts from opinion.
                     - No generic investment advice.
                     - End user is a layman, so explain jargon in simple terms.

                     """;

  public List<Model.StockNewsAnalysis> getPortFolioAnalysis(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest) {

    log.info("Constructing prompt for portfolio analysis...");
    return ChatClient.create(chatModel)
        .prompt()
        .user(
            promptUserSpec ->
                promptUserSpec
                    .text(prompt)
                    .params(
                        Map.of(
                            "stocksAndAvgPrice",
                            portfolioAnalysisRequest.symbolAndPriceList(),
                            "exchange",
                            portfolioAnalysisRequest.exchange(),
                            "horizon",
                            portfolioAnalysisRequest.horizon(),
                            "riskProfile",
                            portfolioAnalysisRequest.riskProfile())))
        .call()
        .entity(new ParameterizedTypeReference<>() {});
  }
}
