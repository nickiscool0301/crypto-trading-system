package com.trading.system.application.dto;

import com.trading.system.domain.model.OrderAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
        Long tradeId,
        String symbol,
        OrderAction orderAction,
        BigDecimal quantity,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        LocalDateTime createdAt) {
}
