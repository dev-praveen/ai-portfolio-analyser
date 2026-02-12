package com.praveen.ai.service;

import com.praveen.ai.domain.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

  private final ChatModel chatModel;

  String template =
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

                     Rules:
                     - Be objective and concise.
                     - Separate facts from opinion.
                     - No generic investment advice.
                     - End user is a layman, so explain jargon in simple terms.
                     Response Mapping rules:
                        Restrict yourself to the following mapping rules for each stock:
                        news_type: [earnings|regulatory|management|macro|sector|competition|one_time|structural]
                        sentiment: [bullish|neutral|bearish]
                        market_reaction: [up|down|flat|unknown]
                        revenue: [positive|neutral|negative]
                        margins: [positive|neutral|negative]
                        balance_sheet: [positive|neutral|negative]
                        long_term_moat: [improving|stable|weakening]
                        risk_level: [low|medium|high]
                        thesis_changed: [true|false]
                        recommended_action: [exit|partial_exit|hold|accumulate|buy]
                     {format}
                     """;

  public List<Model.PortfolioAnalysisResponse> getPortFolioAnalysis(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest) {

    final BeanOutputConverter<List<Model.PortfolioAnalysisResponse>> beanOutputConverter =
        new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});

    final String format = beanOutputConverter.getFormat();

    final String stocksAndAveragePrice =
        portfolioAnalysisRequest.symbolAndPriceList().symbolAndAveragePriceList().toString();

    final Prompt prompt =
        PromptTemplate.builder()
            .template(template)
            .variables(
                Map.of(
                    "stocksAndAvgPrice",
                    stocksAndAveragePrice,
                    "exchange",
                    portfolioAnalysisRequest.exchange(),
                    "horizon",
                    portfolioAnalysisRequest.horizon(),
                    "riskProfile",
                    portfolioAnalysisRequest.riskProfile(),
                    "format",
                    format))
            .build()
            .create();

    final Generation generation = chatModel.call(prompt).getResult();
    final AssistantMessage assistantMessage = generation == null ? null : generation.getOutput();

    if (assistantMessage == null || assistantMessage.getText() == null) {
      log.error("Generation or assistant output is null for the given prompt");
      return Collections.emptyList();
    }

    return beanOutputConverter.convert(assistantMessage.getText());
  }
}
