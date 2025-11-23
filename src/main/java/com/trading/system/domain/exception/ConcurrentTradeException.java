package com.trading.system.domain.exception;

public class ConcurrentTradeException extends TradingSystemException {

    public ConcurrentTradeException() {
        super("Another trade is being processed. Please try again.");
    }
}
