package com.trading.system.application.service;

import com.trading.system.domain.model.AggregatedPrice;
import com.trading.system.domain.port.ExchangeClient;
import com.trading.system.domain.port.ExchangeClient.TickerPrice;
import com.trading.system.domain.repository.AggregatedPriceRepository;
import com.trading.system.domain.service.PriceAggregator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAggregationServiceTest {

	@Mock
	ExchangeClient binanceClient;

	@Mock
	ExchangeClient huobiClient;

	@Mock
	AggregatedPriceRepository repository;

	@Mock
	PriceAggregator priceAggregator;

	@InjectMocks
	PriceAggregationService service;

	@Captor
	ArgumentCaptor<AggregatedPrice> priceCaptor;

	private List<ExchangeClient> exchangeClients;

	@BeforeEach
	void setUp() {
		exchangeClients = List.of(binanceClient, huobiClient);
		service = new PriceAggregationService(exchangeClients, repository, priceAggregator);

		when(binanceClient.getExchangeName()).thenReturn("Binance");
		when(huobiClient.getExchangeName()).thenReturn("Huobi");
	}

	@Test
	void shouldAggregateAndSavePricesFromBothExchangesSuccessfully() {
		var binancePrices = Map.of(
				"ETHUSDT", new TickerPrice(new BigDecimal("2800.00"), new BigDecimal("2805.00")),
				"BTCUSDT", new TickerPrice(new BigDecimal("86250.00"), new BigDecimal("86260.00")));

		var huobiPrices = Map.of(
				"ETHUSDT", new TickerPrice(new BigDecimal("2802.00"), new BigDecimal("2807.00")),
				"BTCUSDT", new TickerPrice(new BigDecimal("86255.00"), new BigDecimal("86265.00")));

		when(binanceClient.fetchTickerPrices(any())).thenReturn(binancePrices);
		when(huobiClient.fetchTickerPrices(any())).thenReturn(huobiPrices);

		when(priceAggregator.findBestPrices(any(), any()))
				.thenReturn(new BigDecimal[] { new BigDecimal("2802.00"), new BigDecimal("2805.00") })
				.thenReturn(new BigDecimal[] { new BigDecimal("86255.00"), new BigDecimal("86260.00") });

		when(repository.findLatestBySymbol(any())).thenReturn(Optional.empty());

		service.aggregateAndSavePrices();

		verify(repository, times(2)).save(any());
		verify(priceAggregator, times(2)).findBestPrices(any(), any());
	}

	@Test
	void shouldContinueAggregationWhenBinanceIsDown() {
		when(binanceClient.fetchTickerPrices(any()))
				.thenThrow(new RuntimeException("Binance API unavailable"));

		var huobiPrices = Map.of(
				"ETHUSDT", new TickerPrice(new BigDecimal("2802.00"), new BigDecimal("2807.00")),
				"BTCUSDT", new TickerPrice(new BigDecimal("86255.00"), new BigDecimal("86265.00")));

		when(huobiClient.fetchTickerPrices(any())).thenReturn(huobiPrices);

		when(priceAggregator.findBestPrices(any(), any()))
				.thenReturn(new BigDecimal[] { new BigDecimal("2802.00"), new BigDecimal("2807.00") })
				.thenReturn(new BigDecimal[] { new BigDecimal("86255.00"), new BigDecimal("86265.00") });

		when(repository.findLatestBySymbol(any())).thenReturn(Optional.empty());

		service.aggregateAndSavePrices();

		verify(repository, times(2)).save(any(AggregatedPrice.class));
		verify(huobiClient).fetchTickerPrices(any());
	}

	@Test
	void shouldNotSavePricesWhenBothExchangesAreDown() {
		when(binanceClient.fetchTickerPrices(any()))
				.thenThrow(new RuntimeException("Binance API unavailable"));
		when(huobiClient.fetchTickerPrices(any()))
				.thenThrow(new RuntimeException("Huobi API unavailable"));

		when(priceAggregator.findBestPrices(any(), any())).thenReturn(null);

		service.aggregateAndSavePrices();

		verify(repository, never()).save(any());
		verify(priceAggregator, times(2)).findBestPrices(any(), any());
	}

	@Test
	void shouldHandleWhenOneExchangeReturnsEmptyMap() {
		when(binanceClient.fetchTickerPrices(any())).thenReturn(Map.of());

		var huobiPrices = Map.of(
				"ETHUSDT", new TickerPrice(new BigDecimal("2802.00"), new BigDecimal("2807.00")),
				"BTCUSDT", new TickerPrice(new BigDecimal("86255.00"), new BigDecimal("86265.00")));

		when(huobiClient.fetchTickerPrices(any())).thenReturn(huobiPrices);

		when(priceAggregator.findBestPrices(any(), any()))
				.thenReturn(new BigDecimal[] { new BigDecimal("2802.00"), new BigDecimal("2807.00") })
				.thenReturn(new BigDecimal[] { new BigDecimal("86255.00"), new BigDecimal("86265.00") });

		when(repository.findLatestBySymbol(any())).thenReturn(Optional.empty());

		service.aggregateAndSavePrices();

		verify(repository, times(2)).save(any());
	}

	@Test
	void shouldNotSaveWhenPriceAggregatorReturnsNull() {
		var binancePrices = Map.of(
				"ETHUSDT", new TickerPrice(new BigDecimal("2800.00"), new BigDecimal("2805.00")));

		when(binanceClient.fetchTickerPrices(any())).thenReturn(binancePrices);
		when(huobiClient.fetchTickerPrices(any())).thenReturn(Map.of());

		when(priceAggregator.findBestPrices(any(), any())).thenReturn(null);

		service.aggregateAndSavePrices();

		verify(repository, never()).save(any());
	}
}
