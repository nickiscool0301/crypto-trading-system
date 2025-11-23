package com.trading.system.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class PriceAggregator {

    public BigDecimal[] findBestPrices(List<BigDecimal> bidPrices, List<BigDecimal> askPrices) {
        if (bidPrices.isEmpty() || askPrices.isEmpty()) {
            log.warn("Cannot aggregate prices: no bid or ask prices available");
            return null;
        }

        // Best prices for sale
        BigDecimal bestBid = bidPrices.stream()
                .max(BigDecimal::compareTo)
                .orElse(null);

        // Best price for buy
        BigDecimal bestAsk = askPrices.stream()
                .min(BigDecimal::compareTo)
                .orElse(null);

        if (bestBid == null || bestAsk == null) {
            return null;
        }

        log.info("Aggregated prices - Best bid: {}, Best ask: {}", bestBid, bestAsk);
        return new BigDecimal[] { bestBid, bestAsk };
    }
}
