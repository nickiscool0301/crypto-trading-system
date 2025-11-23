package com.trading.system.domain.exception;

import java.time.Duration;
import java.time.LocalDateTime;

public class StalePriceException extends TradingSystemException {

    public StalePriceException(String symbol, LocalDateTime priceTimestamp, Duration maxAge) {
        super(String.format(
                "Price for %s is stale. Last updated: %s (age: %d seconds, max allowed: %d seconds)",
                symbol,
                priceTimestamp,
                Duration.between(priceTimestamp, LocalDateTime.now()).getSeconds(),
                maxAge.getSeconds()));
    }
}
