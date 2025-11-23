package com.trading.system.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceAggregatorTest {

    private PriceAggregator priceAggregator;

    @BeforeEach
    void setUp() {
        priceAggregator = new PriceAggregator();
    }

    @Test
    void shouldFindBestBidAndAskFromMultiplePrices() {
        var bidPrices = List.of(
                new BigDecimal("2800.00"),
                new BigDecimal("2805.50"),
                new BigDecimal("2802.75"));
        var askPrices = List.of(
                new BigDecimal("2810.00"),
                new BigDecimal("2807.25"),
                new BigDecimal("2808.50"));

        var result = priceAggregator.findBestPrices(bidPrices, askPrices);
        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo(new BigDecimal("2805.50"));
        assertThat(result[1]).isEqualTo(new BigDecimal("2807.25"));
    }

    @Test
    void shouldHandleSinglePriceInEachList() {
        var bidPrices = List.of(new BigDecimal("3000.00"));
        var askPrices = List.of(new BigDecimal("3005.00"));
        var result = priceAggregator.findBestPrices(bidPrices, askPrices);
        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo(new BigDecimal("3000.00"));
        assertThat(result[1]).isEqualTo(new BigDecimal("3005.00"));
    }

    @Test
    void shouldReturnNullWhenBidPricesListIsEmpty() {
        var bidPrices = List.<BigDecimal>of();
        var askPrices = List.of(new BigDecimal("2800.00"));
        var result = priceAggregator.findBestPrices(bidPrices, askPrices);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenAskPricesListIsEmpty() {
        var bidPrices = List.of(new BigDecimal("2800.00"));
        var askPrices = List.<BigDecimal>of();
        var result = priceAggregator.findBestPrices(bidPrices, askPrices);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenBothListsAreEmpty() {
        var bidPrices = List.<BigDecimal>of();
        var askPrices = List.<BigDecimal>of();
        var result = priceAggregator.findBestPrices(bidPrices, askPrices);
        assertThat(result).isNull();
    }
}
