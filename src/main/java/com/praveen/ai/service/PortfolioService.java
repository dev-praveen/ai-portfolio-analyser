package com.praveen.ai.service;

import com.praveen.ai.dao.PortfolioRepository;
import com.praveen.ai.domain.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

  private final ChatModel chatModel;
  private final PortfolioRepository portfolioRepository;

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

  private static @NonNull String constructStockName(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest,
      Model.PortfolioAnalysisResponse portfolioAnalysisResponse) {

    return portfolioAnalysisResponse
        .stock()
        .concat("#")
        .concat(portfolioAnalysisRequest.exchange().toString())
        .concat("#")
        .concat(portfolioAnalysisRequest.horizon().toString())
        .concat("#")
        .concat(portfolioAnalysisRequest.riskProfile().toString());
  }

  public List<Model.PortfolioAnalysisResponse> getPortFolioAnalysis(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest) {

    final PortfolioAnalysisRequestWithDBResponses portfolioAnalysisRequestWithDBResponses =
        getNewPortfolioAnalysisRequest(portfolioAnalysisRequest);

    final Model.PortfolioAnalysisRequest newPortfolioAnalysisRequest =
        portfolioAnalysisRequestWithDBResponses.portfolioAnalysisRequest();

    if (!newPortfolioAnalysisRequest.symbolAndPriceList().symbolAndAveragePriceList().isEmpty()) {

      final BeanOutputConverter<List<Model.PortfolioAnalysisResponse>> beanOutputConverter =
          new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});

      final String format = beanOutputConverter.getFormat();

      final String stocksAndAveragePrice =
          newPortfolioAnalysisRequest.symbolAndPriceList().symbolAndAveragePriceList().toString();

      final Prompt prompt =
          PromptTemplate.builder()
              .template(template)
              .variables(
                  Map.of(
                      "stocksAndAvgPrice",
                      stocksAndAveragePrice,
                      "exchange",
                      newPortfolioAnalysisRequest.exchange(),
                      "horizon",
                      newPortfolioAnalysisRequest.horizon(),
                      "riskProfile",
                      newPortfolioAnalysisRequest.riskProfile(),
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

      final List<Model.PortfolioAnalysisResponse> portfolioAnalysisResponseList =
          beanOutputConverter.convert(assistantMessage.getText());

      try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
        portfolioAnalysisResponseList.forEach(
            portfolioAnalysisResponse -> {
              final String stockName =
                  constructStockName(newPortfolioAnalysisRequest, portfolioAnalysisResponse);
              log.info(
                  "Saving portfolio analysis for stock: {}", portfolioAnalysisResponse.stock());
              executorService.submit(
                  () ->
                      portfolioRepository.savePortfolioAnalysis(
                          stockName, newPortfolioAnalysisRequest, portfolioAnalysisResponse));
            });
      }
      final List<Model.PortfolioAnalysisResponse> portfolioAnalysisResponses =
          portfolioAnalysisRequestWithDBResponses.responsesFromDB();
      portfolioAnalysisResponseList.addAll(portfolioAnalysisResponses);
      return portfolioAnalysisResponseList;
    }
    return portfolioAnalysisRequestWithDBResponses.responsesFromDB();
  }

  private PortfolioAnalysisRequestWithDBResponses getNewPortfolioAnalysisRequest(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest) {

    OffsetDateTime threshold = OffsetDateTime.now().minusHours(1);

    final List<String> stockSymbols =
        portfolioAnalysisRequest.symbolAndPriceList().symbolAndAveragePriceList().stream()
            .map(Model.SymbolAndPrice::symbol)
            .toList();

    final List<String> stockNames =
        stockSymbols.stream()
            .map(
                symbol ->
                    symbol
                        .concat("#")
                        .concat(portfolioAnalysisRequest.exchange().toString())
                        .concat("#")
                        .concat(portfolioAnalysisRequest.horizon().toString())
                        .concat("#")
                        .concat(portfolioAnalysisRequest.riskProfile().toString()))
            .toList();

    Map<String, Model.PortfolioAnalysisResponse> responsesFromDB = new HashMap<>();

    stockNames.forEach(
        stockName -> {
          var recentAnalysis = portfolioRepository.fetchRecentByStockName(stockName, threshold);

          if (recentAnalysis.isPresent()) {
            log.info("Found recent analysis for stock: {}", stockName);
            responsesFromDB.put(stockName, recentAnalysis.get());
          } else {
            log.info("No recent analysis found for stock: {}", stockName);
            responsesFromDB.put(stockName, null);
          }
        });

    final List<String> stocksInDB =
        responsesFromDB.entrySet().stream()
            .filter(responseFromDB -> Objects.nonNull(responseFromDB.getValue()))
            .map(Map.Entry::getKey)
            .toList();

    final List<Model.PortfolioAnalysisResponse> responses =
        responsesFromDB.values().stream().filter(Objects::nonNull).toList();

    final List<String> stockSymbolsInDB =
        stocksInDB.stream()
            .map(
                stockInDB -> {
                  String[] parts = stockInDB.split("#");
                  return parts[0];
                })
            .toList();

    final List<Model.SymbolAndPrice> symbolAndPrices =
        portfolioAnalysisRequest.symbolAndPriceList().symbolAndAveragePriceList();

    final List<Model.SymbolAndPrice> newSymbolAndPriceList =
        symbolAndPrices.stream()
            .filter(symbolAndPrice -> !stockSymbolsInDB.contains(symbolAndPrice.symbol()))
            .toList();

    new Model.PortfolioAnalysisRequest(
        portfolioAnalysisRequest.exchange(),
        new Model.SymbolAndPriceList(newSymbolAndPriceList),
        portfolioAnalysisRequest.horizon(),
        portfolioAnalysisRequest.riskProfile());

    return new PortfolioAnalysisRequestWithDBResponses(portfolioAnalysisRequest, responses);
  }

  record PortfolioAnalysisRequestWithDBResponses(
      Model.PortfolioAnalysisRequest portfolioAnalysisRequest,
      List<Model.PortfolioAnalysisResponse> responsesFromDB) {}
}
