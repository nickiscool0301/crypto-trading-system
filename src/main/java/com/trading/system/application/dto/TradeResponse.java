package com.trading.system.application.dto;

import com.trading.system.domain.model.OrderAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResponse(
        UUID tradeId,
        String symbol,
        OrderAction orderAction,
        BigDecimal quantity,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        LocalDateTime createdAt) {
}
