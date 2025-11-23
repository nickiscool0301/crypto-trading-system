package com.trading.system.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        String symbol,
        BigDecimal bidPrice,
        BigDecimal askPrice,
        LocalDateTime timestamp) {
}
