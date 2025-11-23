package com.trading.system.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends TradingSystemException {

    public InsufficientBalanceException(String currency, BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient %s balance. Required: %s, Available: %s",
                currency, required, available));
    }
}
