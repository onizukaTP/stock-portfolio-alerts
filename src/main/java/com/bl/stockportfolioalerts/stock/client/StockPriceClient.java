package com.bl.stockportfolioalerts.stock.client;

import com.bl.stockportfolioalerts.stock.dto.StockPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class StockPriceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public double fetchStockPrice(String ticker) {

        log.info("Fetching stock price for ticker={}", ticker);

        // Simulated external API
        String url = "https://dummy-stock-api.com/price/" + ticker;

        try {

            StockPriceResponse response =
                    restTemplate.getForObject(url, StockPriceResponse.class);

            if (response == null) {
                throw new RuntimeException("Stock price not found");
            }

            return response.getPrice();

        } catch (Exception ex) {

            log.error("Failed to fetch stock price for ticker={}", ticker);
            throw new RuntimeException("Stock API error");
        }
    }
}