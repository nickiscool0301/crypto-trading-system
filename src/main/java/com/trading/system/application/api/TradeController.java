package com.trading.system.application.api;

import com.trading.system.application.dto.TradeHistoryResponse;
import com.trading.system.application.dto.TradeRequest;
import com.trading.system.application.dto.TradeResponse;
import com.trading.system.application.service.TradeHistoryService;
import com.trading.system.application.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Slf4j
public class TradeController {

    private final TradeService tradeService;
    private final TradeHistoryService tradeHistoryService;

    @PostMapping
    public ResponseEntity<TradeResponse> executeTrade(@Valid @RequestBody TradeRequest request) {
        log.info("POST /api/trades - Executing trade: {}", request);
        TradeResponse response = tradeService.executeTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TradeHistoryResponse>> getTradeHistory() {
        log.info("GET /api/trades - Fetching trade history");
        List<TradeHistoryResponse> history = tradeHistoryService.getAllTrades();
        return ResponseEntity.ok(history);
    }
}
