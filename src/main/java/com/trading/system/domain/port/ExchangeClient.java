package com.trading.system.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExchangeClient {

    String getExchangeName();

    Map<String, TickerPrice> fetchTickerPrices(List<String> symbols);

    record TickerPrice(BigDecimal bid, BigDecimal ask) {
        public TickerPrice {
            if (bid != null && ask != null && bid.compareTo(ask) > 0) {
                throw new IllegalArgumentException("Bid price cannot be higher than ask price");
            }
        }
    }
}
