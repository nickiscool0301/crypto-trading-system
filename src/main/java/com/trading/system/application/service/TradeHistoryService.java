package com.trading.system.application.service;

import com.trading.system.application.dto.TradeHistoryResponse;
import com.trading.system.domain.model.Trade;
import com.trading.system.domain.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeHistoryService {

    private final TradeRepository tradeRepository;

    @Transactional(readOnly = true)
    public List<TradeHistoryResponse> getAllTrades() {
        log.debug("Fetching all trade history");
        return tradeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private TradeHistoryResponse toResponse(Trade trade) {
        return new TradeHistoryResponse(
                trade.getId(),
                trade.getSymbol(),
                trade.getOrderAction(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getTotalAmount(),
                trade.getTimestamp());
    }
}
