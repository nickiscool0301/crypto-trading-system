package com.trading.system.application.service;

import com.trading.system.application.dto.WalletBalanceResponse;
import com.trading.system.domain.model.Wallet;
import com.trading.system.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private static final Long WALLET_ID = 1L;
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
