package com.trading.system.application.service;

import com.trading.system.application.dto.TradeRequest;
import com.trading.system.domain.exception.PriceNotFoundException;
import com.trading.system.domain.exception.StalePriceException;
import com.trading.system.domain.model.AggregatedPrice;
import com.trading.system.domain.model.OrderAction;
import com.trading.system.domain.model.Trade;
import com.trading.system.domain.model.Wallet;
import com.trading.system.domain.repository.AggregatedPriceRepository;
import com.trading.system.domain.repository.TradeRepository;
import com.trading.system.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

	@Mock
	WalletRepository walletRepository;

	@Mock
	TradeRepository tradeRepository;

	@Mock
	AggregatedPriceRepository aggregatedPriceRepository;

	@InjectMocks
	TradeService tradeService;

	private static final UUID WALLET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
	private Wallet wallet;
	private AggregatedPrice ethPrice;
	private AggregatedPrice btcPrice;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(tradeService, "maxPriceAgeSeconds", 30L);

		wallet = new Wallet(
				new BigDecimal("10000.00000000"),
				new BigDecimal("5.00000000"),
				new BigDecimal("0.50000000"));

		ethPrice = new AggregatedPrice(
				"ETHUSDT",
				new BigDecimal("2800.00000000"),
				new BigDecimal("2805.00000000"));
		ethPrice.setTimestamp(LocalDateTime.now());

		btcPrice = new AggregatedPrice(
				"BTCUSDT",
				new BigDecimal("86250.00000000"),
				new BigDecimal("86260.00000000"));
		btcPrice.setTimestamp(LocalDateTime.now());
	}

	@Test
	void shouldExecuteBuyTradeForEthSuccessfully() {
		var request = new TradeRequest(
				"ETHUSDT",
				OrderAction.BUY,
				new BigDecimal("1.00000000"));

		when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
		when(aggregatedPriceRepository.findLatestBySymbol("ETHUSDT")).thenReturn(Optional.of(ethPrice));

		var savedTrade = new Trade("ETHUSDT", OrderAction.BUY, request.quantity(), ethPrice.getAskPrice());
		when(tradeRepository.save(any())).thenReturn(savedTrade);

		var response = tradeService.executeTrade(request);

		assertThat(response).isNotNull();
		assertThat(response.symbol()).isEqualTo("ETHUSDT");
		assertThat(response.orderAction()).isEqualTo(OrderAction.BUY);
		assertThat(response.quantity()).isEqualByComparingTo(new BigDecimal("1.00000000"));
		assertThat(response.executionPrice()).isEqualByComparingTo(ethPrice.getAskPrice());
		assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("2805.00000000"));

		assertThat(wallet.getUsdtBalance()).isEqualByComparingTo(new BigDecimal("7195.00000000"));
		assertThat(wallet.getEthBalance()).isEqualByComparingTo(new BigDecimal("6.00000000"));

		verify(walletRepository).save(wallet);
		verify(tradeRepository).save(any());
	}

	@Test
	void shouldExecuteSellTradeForEthSuccessfully() {
		var request = new TradeRequest(
				"ETHUSDT",
				OrderAction.SELL,
				new BigDecimal("2.00000000"));

		when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
		when(aggregatedPriceRepository.findLatestBySymbol("ETHUSDT")).thenReturn(Optional.of(ethPrice));

		var savedTrade = new Trade("ETHUSDT", OrderAction.SELL, request.quantity(), ethPrice.getBidPrice());
		when(tradeRepository.save(any())).thenReturn(savedTrade);

		var response = tradeService.executeTrade(request);

		assertThat(response).isNotNull();
		assertThat(response.symbol()).isEqualTo("ETHUSDT");
		assertThat(response.orderAction()).isEqualTo(OrderAction.SELL);
		assertThat(response.executionPrice()).isEqualByComparingTo(ethPrice.getBidPrice());
		assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("5600.00000000"));

		assertThat(wallet.getEthBalance()).isEqualByComparingTo(new BigDecimal("3.00000000"));
		assertThat(wallet.getUsdtBalance()).isEqualByComparingTo(new BigDecimal("15600.00000000"));

		verify(walletRepository).save(wallet);
		verify(tradeRepository).save(any());
	}

	@Test
	void shouldThrowPriceNotFoundExceptionWhenPriceNotFound() {
		// Given
		var request = new TradeRequest("ETHUSDT", OrderAction.BUY, new BigDecimal("1.0"));

		when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
		when(aggregatedPriceRepository.findLatestBySymbol("ETHUSDT")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tradeService.executeTrade(request))
				.isInstanceOf(PriceNotFoundException.class)
				.hasMessageContaining("Price not found for symbol: ETHUSDT");

		verify(tradeRepository, never()).save(any());
	}

	@Test
	void shouldThrowStalePriceExceptionWhenPriceIsTooOld() {
		var request = new TradeRequest("ETHUSDT", OrderAction.BUY, new BigDecimal("1.0"));

		var stalePrice = new AggregatedPrice(
				"ETHUSDT",
				new BigDecimal("2800.00000000"),
				new BigDecimal("2805.00000000"));
		stalePrice.setTimestamp(LocalDateTime.now().minusSeconds(60));

		when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
		when(aggregatedPriceRepository.findLatestBySymbol("ETHUSDT")).thenReturn(Optional.of(stalePrice));

		assertThatThrownBy(() -> tradeService.executeTrade(request))
				.isInstanceOf(StalePriceException.class)
				.hasMessageContaining("Price for ETHUSDT is stale");

		verify(tradeRepository, never()).save(any());
		verify(walletRepository, never()).save(any());
	}

	@Test
	void shouldThrowOptimisticLockExceptionOnConcurrentTrade() {
		var request = new TradeRequest(
				"ETHUSDT",
				OrderAction.BUY,
				new BigDecimal("1.00000000"));

		when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
		when(aggregatedPriceRepository.findLatestBySymbol("ETHUSDT")).thenReturn(Optional.of(ethPrice));

		when(walletRepository.save(any()))
				.thenThrow(new ObjectOptimisticLockingFailureException(Wallet.class, WALLET_ID));

		assertThatThrownBy(() -> tradeService.executeTrade(request))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);

		verify(tradeRepository, never()).save(any());
	}

	@Test
	void shouldSucceedAfterOptimisticLockFailure() {
		// Stimulation: Trade A succeeds → Trade B fails → Trade B retry succeeds
		var request = new TradeRequest(
				"ETHUSDT",
				OrderAction.BUY,
				new BigDecimal("1.00000000"));

		var savedTrade = new Trade("ETHUSDT", OrderAction.BUY, request.quantity(), ethPrice.getAskPrice());

		when(walletRepository.findById(WALLET_ID))
				.thenReturn(Optional.of(wallet))
				.thenReturn(Optional.of(wallet))
				.thenAnswer(inv -> { // Trade B - retry (fresh wallet)
					wallet.setVersion(1L);
					wallet.setUsdtBalance(new BigDecimal("7195.00000000"));
					wallet.setEthBalance(new BigDecimal("6.00000000"));
					return Optional.of(wallet);
				});

		when(aggregatedPriceRepository.findLatestBySymbol("ETHUSDT"))
				.thenReturn(Optional.of(ethPrice));

		when(tradeRepository.save(any()))
				.thenReturn(savedTrade);

		when(walletRepository.save(any()))
				.thenReturn(wallet)
				.thenThrow(new ObjectOptimisticLockingFailureException(Wallet.class, WALLET_ID))
				.thenReturn(wallet);

		var responseA = tradeService.executeTrade(request);
		assertThat(responseA).isNotNull();

		assertThatThrownBy(() -> tradeService.executeTrade(request))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);

		var responseBRetry = tradeService.executeTrade(request);
		assertThat(responseBRetry).isNotNull();

		verify(tradeRepository, times(2)).save(any());
		verify(walletRepository, times(3)).save(any());
	}

}
