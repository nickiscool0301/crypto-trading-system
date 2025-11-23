package com.trading.system.infrastructure.adapter.exchange;

import com.trading.system.domain.port.ExchangeClient;
import com.trading.system.infrastructure.adapter.dto.HuobiTickersResponse;
import com.trading.system.infrastructure.adapter.dto.HuobiTickerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HuobiClient implements ExchangeClient {

    private static final String HUOBI_API_URL = "https://api.huobi.pro/market/tickers";

    private final WebClient webClient;

    @Override
    public String getExchangeName() {
        return "Huobi";
    }

    // Tickers API from Houbi does not support get by symbols
    // Fetch all then filter
    @Override
    public Map<String, TickerPrice> fetchTickerPrices(List<String> symbols) {
        var result = new HashMap<String, TickerPrice>();

        try {
            // Get all available tickers
            var response = webClient.get()
                    .uri(HUOBI_API_URL)
                    .retrieve()
                    .bodyToMono(HuobiTickersResponse.class)
                    .block();

            if (response == null || response.data() == null) {
                log.warn("Huobi returned null response");
                return result;
            }

            // Huobi return lowercase symbols
            for (HuobiTickerResponse ticker : response.data()) {
                var upperSymbol = ticker.symbol().toUpperCase();

                if (symbols.contains(upperSymbol)) {
                    var price = new TickerPrice(
                            BigDecimal.valueOf(ticker.bid()),
                            BigDecimal.valueOf(ticker.ask()));

                    result.put(upperSymbol, price);
                    log.info("Huobi {}: bid={}, ask={}", upperSymbol, price.bid(), price.ask());
                }
            }

        } catch (Exception e) {
            log.error("Failed to fetch tickers from Huobi: {}", e.getMessage());
        }

        return result;
    }
}
