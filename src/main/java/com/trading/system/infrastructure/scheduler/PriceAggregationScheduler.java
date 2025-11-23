package com.trading.system.infrastructure.scheduler;

import com.trading.system.application.service.PriceAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceAggregationScheduler {

    private final PriceAggregationService priceAggregationService;

    @Scheduled(fixedRate = 10000)
    public void schedulePriceAggregation() {
        priceAggregationService.aggregateAndSavePrices();
    }
}
