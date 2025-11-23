package com.trading.system.domain.exception;

import java.util.UUID;

public class WalletNotFoundException extends TradingSystemException {

    public WalletNotFoundException(UUID walletId) {
        super(String.format("Wallet not found with ID: %s", walletId));
    }
}
