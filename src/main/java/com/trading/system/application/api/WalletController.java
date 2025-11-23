package com.trading.system.application.api;

import com.trading.system.application.dto.WalletBalanceResponse;
import com.trading.system.application.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletBalanceResponse> getWalletBalance() {
        log.info("GET /api/wallet - Fetching wallet balance");
        WalletBalanceResponse balance = walletService.getWalletBalance();
        return ResponseEntity.ok(balance);
    }
}
