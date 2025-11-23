package com.trading.system.application.service;

import com.trading.system.application.dto.TradeRequest;
import com.trading.system.application.dto.TradeResponse;
import com.trading.system.domain.exception.PriceNotFoundException;
import com.trading.system.domain.exception.StalePriceException;
import com.trading.system.domain.exception.UnsupportedSymbolException;
import com.trading.system.domain.exception.WalletNotFoundException;
import com.trading.system.domain.model.AggregatedPrice;
import com.trading.system.domain.model.OrderAction;
import com.trading.system.domain.model.Trade;
import com.trading.system.domain.model.Wallet;
import com.trading.system.domain.repository.AggregatedPriceRepository;
import com.trading.system.domain.repository.TradeRepository;
import com.trading.system.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private static final UUID WALLET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Value("${trading.price.max-age-seconds:30}")
    private long maxPriceAgeSeconds;

    private final WalletRepository walletRepository;
    private final TradeRepository tradeRepository;
    private final AggregatedPriceRepository aggregatedPriceRepository;

    @Transactional
    public TradeResponse executeTrade(TradeRequest request) {
        log.info("Executing trade: symbol={}, action={}, quantity={}",
                request.symbol(), request.orderAction(), request.quantity());

        var wallet = walletRepository.findById(WALLET_ID)
                .orElseThrow(() -> new WalletNotFoundException(WALLET_ID));

        var latestPrice = aggregatedPriceRepository.findLatestBySymbol(request.symbol())
                .orElseThrow(() -> new PriceNotFoundException(request.symbol()));

        validatePriceFreshness(latestPrice);

        BigDecimal executionPrice = determineExecutionPrice(latestPrice, request.orderAction());
        BigDecimal totalAmount = request.quantity().multiply(executionPrice);

        updateWalletBalance(wallet, request.symbol(), request.orderAction(), request.quantity(), totalAmount);
        walletRepository.save(wallet);

        var trade = new Trade(request.symbol(), request.orderAction(), request.quantity(), executionPrice);
        var savedTrade = tradeRepository.save(trade);

        log.info("Trade executed successfully: tradeId={}, executionPrice={}, totalAmount={}",
                savedTrade.getId(), executionPrice, totalAmount);

        return new TradeResponse(
                savedTrade.getId(),
                savedTrade.getSymbol(),
                savedTrade.getOrderAction(),
                savedTrade.getQuantity(),
                executionPrice,
                totalAmount,
                savedTrade.getTimestamp());
    }

    private void validatePriceFreshness(AggregatedPrice price) {
        var priceAge = Duration.between(price.getTimestamp(), LocalDateTime.now());
        var maxAge = Duration.ofSeconds(maxPriceAgeSeconds);

        if (priceAge.compareTo(maxAge) > 0) {
            log.warn("Price for {} is stale. Age: {} seconds, Max allowed: {} seconds",
                    price.getSymbol(), priceAge.getSeconds(), maxAge.getSeconds());
            throw new StalePriceException(price.getSymbol(), price.getTimestamp(), maxAge);
        }

        log.debug("Price for {} is fresh. Age: {} seconds", price.getSymbol(), priceAge.getSeconds());
    }

    private BigDecimal determineExecutionPrice(AggregatedPrice price, OrderAction orderAction) {
        return orderAction == OrderAction.BUY ? price.getAskPrice() : price.getBidPrice();
    }

    private void updateWalletBalance(Wallet wallet, String symbol, OrderAction orderAction,
            BigDecimal quantity, BigDecimal totalAmount) {
        switch (symbol) {
            case "ETHUSDT" -> updateEthBalance(wallet, orderAction, quantity, totalAmount);
            case "BTCUSDT" -> updateBtcBalance(wallet, orderAction, quantity, totalAmount);
            default -> throw new UnsupportedSymbolException(symbol);
        }
    }

    private void updateEthBalance(Wallet wallet, OrderAction orderAction, BigDecimal quantity, BigDecimal totalAmount) {
        if (orderAction == OrderAction.BUY) {
            wallet.deductUsdt(totalAmount);
            wallet.addEth(quantity);
        } else {
            wallet.deductEth(quantity);
            wallet.addUsdt(totalAmount);
        }
    }

    private void updateBtcBalance(Wallet wallet, OrderAction orderAction, BigDecimal quantity, BigDecimal totalAmount) {
        if (orderAction == OrderAction.BUY) {
            wallet.deductUsdt(totalAmount);
            wallet.addBtc(quantity);
        } else {
            wallet.deductBtc(quantity);
            wallet.addUsdt(totalAmount);
        }
    }
}
