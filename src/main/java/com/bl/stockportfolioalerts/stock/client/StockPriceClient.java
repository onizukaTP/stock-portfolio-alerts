package com.bl.stockportfolioalerts.stock.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class StockPriceClient {

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("Loaded API key: {}", apiKey);
    }

    public double fetchStockPrice(String ticker) {

        String url =
                "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                        + ticker + "&apikey=" + apiKey;

        try {

            String response = restTemplate.getForObject(url, String.class);

            log.info("Stock API response: {}", response);

            JsonNode root = objectMapper.readTree(response);

            String price = root
                    .path("Global Quote")
                    .path("05. price")
                    .asText();

            return Double.parseDouble(price);

        } catch (Exception ex) {

            log.error("Failed to fetch stock price for ticker={}", ticker, ex);
            throw new RuntimeException("Stock API error", ex);

        }
    }
}