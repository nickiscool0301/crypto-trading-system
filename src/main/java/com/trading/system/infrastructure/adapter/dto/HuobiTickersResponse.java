package com.trading.system.infrastructure.adapter.dto;

import java.util.List;

public record HuobiTickersResponse(
                String status,
                List<HuobiTickerResponse> data) {
}
