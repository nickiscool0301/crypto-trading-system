package com.trading.system.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet(
                new BigDecimal("10000.00000000"),
                new BigDecimal("5.00000000"),
                new BigDecimal("0.50000000"));
    }

    @Test
    void shouldDeductUsdtWhenBalanceIsSufficient() {
        var amount = new BigDecimal("1000.00000000");
        wallet.deductUsdt(amount);
        assertThat(wallet.getUsdtBalance()).isEqualTo(new BigDecimal("9000.00000000"));
    }

    @Test
    void shouldThrowExceptionWhenDeductingUsdtWithInsufficientBalance() {
        var amount = new BigDecimal("15000.00000000");
        assertThatThrownBy(() -> wallet.deductUsdt(amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient USDT balance");
        assertThat(wallet.getUsdtBalance()).isEqualTo(new BigDecimal("10000.00000000"));
    }

    @Test
    void shouldAddUsdtToBalance() {
        var amount = new BigDecimal("5000.00000000");
        wallet.addUsdt(amount);
        assertThat(wallet.getUsdtBalance()).isEqualTo(new BigDecimal("15000.00000000"));
    }

    @Test
    void shouldDeductEthWhenBalanceIsSufficient() {
        var amount = new BigDecimal("2.00000000");
        wallet.deductEth(amount);
        assertThat(wallet.getEthBalance()).isEqualTo(new BigDecimal("3.00000000"));
    }

    @Test
    void shouldThrowExceptionWhenDeductingEthWithInsufficientBalance() {
        var amount = new BigDecimal("10.00000000");
        assertThatThrownBy(() -> wallet.deductEth(amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient ETH balance");
        assertThat(wallet.getEthBalance()).isEqualTo(new BigDecimal("5.00000000"));
    }

    @Test
    void shouldAddEthToBalance() {
        var amount = new BigDecimal("3.00000000");
        wallet.addEth(amount);
        assertThat(wallet.getEthBalance()).isEqualTo(new BigDecimal("8.00000000"));
    }

    @Test
    void shouldDeductBtcWhenBalanceIsSufficient() {
        var amount = new BigDecimal("0.25000000");
        wallet.deductBtc(amount);
        assertThat(wallet.getBtcBalance()).isEqualTo(new BigDecimal("0.25000000"));
    }

    @Test
    void shouldThrowExceptionWhenDeductingBtcWithInsufficientBalance() {
        var amount = new BigDecimal("1.00000000");
        assertThatThrownBy(() -> wallet.deductBtc(amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient BTC balance");
        assertThat(wallet.getBtcBalance()).isEqualTo(new BigDecimal("0.50000000"));
    }

    @Test
    void shouldAddBtcToBalance() {
        var amount = new BigDecimal("0.75000000");
        wallet.addBtc(amount);
        assertThat(wallet.getBtcBalance()).isEqualTo(new BigDecimal("1.25000000"));
    }

    @Test
    void shouldHandleExactBalanceDeductionForUsdt() {
        var amount = new BigDecimal("10000.00000000");
        wallet.deductUsdt(amount);
        assertThat(wallet.getUsdtBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleExactBalanceDeductionForEth() {
        var amount = new BigDecimal("5.00000000");
        wallet.deductEth(amount);
        assertThat(wallet.getEthBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleExactBalanceDeductionForBtc() {
        var amount = new BigDecimal("0.50000000");
        wallet.deductBtc(amount);
        assertThat(wallet.getBtcBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
