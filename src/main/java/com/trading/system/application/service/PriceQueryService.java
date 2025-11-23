package com.trading.system.application.service;

import com.trading.system.application.dto.PriceResponse;
import com.trading.system.domain.model.AggregatedPrice;
import com.trading.system.domain.repository.AggregatedPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class PriceQueryService {

    private final AggregatedPriceRepository priceRepository;

    @Transactional(readOnly = true)
    public List<PriceResponse> getAllLatestPrices() {
        log.debug("Querying all latest aggregated prices");

        return priceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PriceResponse> getLatestPriceBySymbol(String symbol) {
        log.debug("Querying latest price for symbol: {}", symbol);

        return priceRepository.findLatestBySymbol(symbol)
                .map(this::toResponse);
    }

    private PriceResponse toResponse(AggregatedPrice price) {
        return new PriceResponse(
                price.getSymbol(),
                price.getBidPrice(),
                price.getAskPrice(),
                price.getTimestamp());
    }
}
