package com.trading.system.domain.exception;

public class UnsupportedSymbolException extends TradingSystemException {

    public UnsupportedSymbolException(String symbol) {
        super(String.format("Unsupported trading symbol: %s", symbol));
    }
}
