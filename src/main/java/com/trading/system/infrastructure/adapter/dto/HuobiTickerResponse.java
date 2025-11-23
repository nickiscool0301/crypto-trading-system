package com.trading.system.infrastructure.adapter.dto;

public record HuobiTickerResponse(
                String symbol,
                Double bid,
                Double bidSize,
                Double ask,
                Double askSize) {
}
