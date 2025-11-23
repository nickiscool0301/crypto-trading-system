package com.trading.system.application.api;

import com.trading.system.application.dto.PriceResponse;
import com.trading.system.application.service.PriceQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Slf4j
public class PriceController {

    private final PriceQueryService priceQueryService;

    @GetMapping("/latest")
    public ResponseEntity<List<PriceResponse>> getLatestPrices() {
        log.info("GET /api/prices/latest - Fetching all latest prices");

        List<PriceResponse> prices = priceQueryService.getAllLatestPrices();
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/latest/{symbol}")
    public ResponseEntity<PriceResponse> getLatestPriceBySymbol(@PathVariable String symbol) {
        log.info("GET /api/prices/latest/{} - Fetching price for symbol", symbol);

        return priceQueryService.getLatestPriceBySymbol(symbol.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
