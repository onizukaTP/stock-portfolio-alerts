package com.bl.stockportfolioalerts.stock.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class StockPriceClient {

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    @Value("${app.stock.fallback.enabled:true}")
    private boolean fallbackEnabled;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Double> FALLBACK_PRICES = Map.of(
            "AAPL",  182.50,
            "TSLA",  175.00,
            "MSFT",  420.00,
            "GOOGL", 175.50,
            "AMZN",  185.00,
            "NVDA",  900.00,
            "META",  500.00,
            "NFLX",  650.00
    );

    @PostConstruct
    public void init() {
        log.info("StockPriceClient initialized — fallback={}", fallbackEnabled);
    }

    public double fetchStockPrice(String ticker) {
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                + ticker + "&apikey=" + apiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            log.debug("Alpha Vantage raw response for ticker={}: {}", ticker, response);

            JsonNode root = objectMapper.readTree(response);

            // Rate limit or invalid plan returns "Information" key instead of data
            if (root.has("Information") || root.has("Note")) {
                String reason = root.has("Information")
                        ? root.path("Information").asText()
                        : root.path("Note").asText();
                log.warn("Alpha Vantage API limit hit for ticker={} — reason: {}", ticker, reason);
                return handleFallback(ticker, "rate limited");
            }

            JsonNode quoteNode = root.path("Global Quote");

            if (quoteNode.isMissingNode() || quoteNode.isEmpty()) {
                log.warn("Empty Global Quote for ticker={}", ticker);
                return handleFallback(ticker, "empty response");
            }

            String priceStr = quoteNode.path("05. price").asText();

            if (priceStr == null || priceStr.isBlank()) {
                log.warn("Blank price field for ticker={}", ticker);
                return handleFallback(ticker, "blank price");
            }

            double price = Double.parseDouble(priceStr);
            log.info("Fetched live price for ticker={} price={}", ticker, price);
            return price;

        } catch (Exception ex) {
            log.error("Exception fetching stock price for ticker={}", ticker, ex);
            return handleFallback(ticker, ex.getMessage());
        }
    }

    private double handleFallback(String ticker, String reason) {
        if (!fallbackEnabled) {
            throw new RuntimeException(
                    "Stock price unavailable for ticker=" + ticker + " reason=" + reason
            );
        }
        double fallback = FALLBACK_PRICES.getOrDefault(ticker.toUpperCase(), 100.00);
        log.warn("Using fallback price for ticker={} price={} reason={}", ticker, fallback, reason);
        return fallback;
    }
}