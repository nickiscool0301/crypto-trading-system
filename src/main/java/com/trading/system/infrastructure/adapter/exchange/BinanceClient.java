package com.trading.system.infrastructure.adapter.exchange;

import com.trading.system.domain.port.ExchangeClient;
import com.trading.system.infrastructure.adapter.dto.BinanceTickerResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class BinanceClient implements ExchangeClient {

    private static final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/bookTicker";

    private final WebClient webClient;
    private final RateLimiter rateLimiter;

    public BinanceClient(WebClient webClient, RateLimiterRegistry rateLimiterRegistry) {
        this.webClient = webClient;
        this.rateLimiter = rateLimiterRegistry.rateLimiter("binance");
    }

    @Override
    public String getExchangeName() {
        return "Binance";
    }

    @Override
    public Map<String, TickerPrice> fetchTickerPrices(List<String> symbols) {
        var result = new HashMap<String, TickerPrice>();

        // Binance: call API once per symbol
        for (String symbol : symbols) {
            try {
                var response = fetchTickerBySymbol(symbol);
                if (response != null) {
                    var price = new TickerPrice(
                            new BigDecimal(response.bidPrice()),
                            new BigDecimal(response.askPrice()));
                    result.put(symbol, price);
                    log.info("Binance {}: bid={}, ask={}", symbol, price.bid(), price.ask());
                } else {
                    log.warn("Binance returned null response for symbol: {}", symbol);
                }
            } catch (Exception e) {
                log.error("Failed to fetch ticker from Binance for {}: {}", symbol, e.getMessage());
            }
        }

        return result;
    }

    private BinanceTickerResponse fetchTickerBySymbol(String symbol) {
        return rateLimiter.executeSupplier(() -> {
            String url = BINANCE_API_URL + "?symbol=" + symbol;
            log.info("Fetching Binance ticker for {} (rate limiter: {} available)",
                    symbol, rateLimiter.getMetrics().getAvailablePermissions());
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(BinanceTickerResponse.class)
                    .block();
        });
    }
}
