package com.trading.system.infrastructure.adapter.dto;

public record BinanceTickerResponse(
                String symbol,
                String bidPrice,
                String bidQty,
                String askPrice,
                String askQty) {
}
