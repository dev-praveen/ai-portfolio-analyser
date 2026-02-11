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

                     {format}
                     """;

  public List<Model.StockNewsAnalysis> getPortFolioAnalysis(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest) {

    final BeanOutputConverter<List<Model.StockNewsAnalysis>> beanOutputConverter =
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
    if (generation == null) {
      log.error("Generation result is null for the given prompt: {}", prompt);
      return Collections.emptyList();
    }
    final AssistantMessage assistantMessage = generation.getOutput();
    if (assistantMessage.getText() == null) {
      log.error("AssistantMessage output is null for the given prompt: {}", prompt);
      return Collections.emptyList();
    }

    return beanOutputConverter.convert(assistantMessage.getText());
  }
}
