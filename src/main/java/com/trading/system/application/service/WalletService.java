package com.trading.system.application.service;

import com.trading.system.application.dto.WalletBalanceResponse;
import com.trading.system.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private static final UUID WALLET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public WalletBalanceResponse getWalletBalance() {
        log.debug("Fetching wallet balance");

        // We only have 1 wallet in the system
        var wallet = walletRepository.findById(WALLET_ID)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return new WalletBalanceResponse(
                wallet.getUsdtBalance(),
                wallet.getEthBalance(),
                wallet.getBtcBalance(),
                wallet.getUpdatedAt());
    }
}
