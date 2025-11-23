package com.trading.system.application.dto;

import com.trading.system.domain.model.OrderAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeHistoryResponse(
        Long id,
        String symbol,
        OrderAction orderAction,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        LocalDateTime timestamp) {
}
