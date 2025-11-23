package com.trading.system.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletBalanceResponse(
        BigDecimal usdtBalance,
        BigDecimal ethBalance,
        BigDecimal btcBalance,
        LocalDateTime updatedAt) {
}
