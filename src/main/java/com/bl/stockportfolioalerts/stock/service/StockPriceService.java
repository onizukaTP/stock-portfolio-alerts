package com.bl.stockportfolioalerts.stock.service;

import com.bl.stockportfolioalerts.stock.client.StockPriceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    private final StockPriceClient stockPriceClient;

    @Cacheable(value = "stockPrices", key = "#ticker")
    public double getStockPrice(String ticker) {

        log.info("Fetching stock price from external API. ticker={}", ticker);

        return stockPriceClient.fetchStockPrice(ticker);
    }
}