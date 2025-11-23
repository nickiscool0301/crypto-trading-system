package com.trading.system.domain.exception;

public abstract class TradingSystemException extends RuntimeException {

    protected TradingSystemException(String message) {
        super(message);
    }

    protected TradingSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
