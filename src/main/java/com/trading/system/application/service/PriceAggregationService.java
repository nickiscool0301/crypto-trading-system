package com.trading.system.application.service;

import com.trading.system.domain.model.AggregatedPrice;
import com.trading.system.domain.port.ExchangeClient;
import com.trading.system.domain.port.ExchangeClient.TickerPrice;
import com.trading.system.domain.repository.AggregatedPriceRepository;
import com.trading.system.domain.service.PriceAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAggregationService {

    private static final List<String> SUPPORTED_SYMBOLS = List.of("ETHUSDT", "BTCUSDT");

    private final List<ExchangeClient> exchangeClients;
    private final AggregatedPriceRepository repository;
    private final PriceAggregator priceAggregator;

    @Transactional
    public void aggregateAndSavePrices() {
        log.info("Starting price aggregation for {} symbols across {} exchanges",
                SUPPORTED_SYMBOLS.size(), exchangeClients.size());

        var allExchangePrices = fetchPricesFromExchanges();

        for (String symbol : SUPPORTED_SYMBOLS) {
            try {
                aggregateAndSaveForSymbol(symbol, allExchangePrices);
            } catch (Exception e) {
                log.error("Failed to aggregate price for {}: {}", symbol, e.getMessage(), e);
            }
        }
    }

    private Map<String, Map<String, TickerPrice>> fetchPricesFromExchanges() {
        var allExchangePrices = new HashMap<String, Map<String, TickerPrice>>();

        for (ExchangeClient client : exchangeClients) {
            try {
                var prices = client.fetchTickerPrices(SUPPORTED_SYMBOLS);
                allExchangePrices.put(client.getExchangeName(), prices);
                log.info("{} returned {} prices", client.getExchangeName(), prices.size());
            } catch (Exception e) {
                log.error("Failed to fetch prices from {}: {}", client.getExchangeName(), e.getMessage(), e);
            }
        }

        return allExchangePrices;
    }

    private void aggregateAndSaveForSymbol(String symbol, Map<String, Map<String, TickerPrice>> allExchangePrices) {
        var bidPrices = new ArrayList<BigDecimal>();
        var askPrices = new ArrayList<BigDecimal>();

        for (Map.Entry<String, Map<String, TickerPrice>> entry : allExchangePrices.entrySet()) {
            var exchangeName = entry.getKey();
            var price = entry.getValue().get(symbol);

            if (price != null) {
                bidPrices.add(price.bid());
                askPrices.add(price.ask());
                log.debug("{} {}: bid={}, ask={}", exchangeName, symbol, price.bid(), price.ask());
            }
        }

        var bestPrices = priceAggregator.findBestPrices(bidPrices, askPrices);

        if (bestPrices != null) {
            updateOrCreatePrice(symbol, bestPrices[0], bestPrices[1]);
        } else {
            log.warn("No prices available for {}", symbol);
        }
    }

    private void updateOrCreatePrice(String symbol, BigDecimal bestBid, BigDecimal bestAsk) {
        var price = repository.findLatestBySymbol(symbol)
                .orElse(new AggregatedPrice());

        // Update change
        price.setSymbol(symbol);
        price.setBidPrice(bestBid);
        price.setAskPrice(bestAsk);

        repository.save(price);
        log.info("Updated aggregated price for {}: bid={}, ask={}", symbol, bestBid, bestAsk);
    }
}
