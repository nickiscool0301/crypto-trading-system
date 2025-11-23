package com.trading.system.domain.exception;

public class PriceNotFoundException extends TradingSystemException {

    public PriceNotFoundException(String symbol) {
        super(String.format("Price not found for symbol: %s", symbol));
    }
}
