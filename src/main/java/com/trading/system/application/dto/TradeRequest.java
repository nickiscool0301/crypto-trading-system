package com.trading.system.application.dto;

import java.math.BigDecimal;

import com.trading.system.domain.model.OrderAction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TradeRequest(
        @NotBlank(message = "Symbol is required") String symbol,

        @NotNull(message = "Order action is required") OrderAction orderAction,

        @NotNull(message = "Quantity is required") @DecimalMin(value = "0.00000001", message = "Quantity must be greater than 0") BigDecimal quantity) {
}
