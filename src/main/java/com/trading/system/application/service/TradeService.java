package com.trading.system.application.service;

import com.trading.system.application.dto.TradeRequest;
import com.trading.system.application.dto.TradeResponse;
import com.trading.system.domain.model.AggregatedPrice;
import com.trading.system.domain.model.OrderAction;
import com.trading.system.domain.model.Trade;
import com.trading.system.domain.model.Wallet;
import com.trading.system.domain.repository.AggregatedPriceRepository;
import com.trading.system.domain.repository.TradeRepository;
import com.trading.system.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private static final UUID WALLET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final WalletRepository walletRepository;
    private final TradeRepository tradeRepository;
    private final AggregatedPriceRepository aggregatedPriceRepository;

    @Transactional
    public TradeResponse executeTrade(TradeRequest request) {
        log.info("Executing trade: symbol={}, action={}, quantity={}",
                request.symbol(), request.orderAction(), request.quantity());

        var wallet = walletRepository.findById(WALLET_ID)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        var latestPrice = aggregatedPriceRepository.findLatestBySymbol(request.symbol())
                .orElseThrow(() -> new RuntimeException("Price not found for symbol: " + request.symbol()));

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

    private BigDecimal determineExecutionPrice(AggregatedPrice price, OrderAction orderAction) {
        return orderAction == OrderAction.BUY ? price.getAskPrice() : price.getBidPrice();
    }

    private void updateWalletBalance(Wallet wallet, String symbol, OrderAction orderAction,
            BigDecimal quantity, BigDecimal totalAmount) {
        switch (symbol) {
            case "ETHUSDT" -> updateEthBalance(wallet, orderAction, quantity, totalAmount);
            case "BTCUSDT" -> updateBtcBalance(wallet, orderAction, quantity, totalAmount);
            default -> throw new RuntimeException("Unsupported symbol: " + symbol);
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
