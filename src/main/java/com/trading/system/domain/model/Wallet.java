package com.trading.system.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal usdtBalance;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal ethBalance;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal btcBalance;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Wallet(BigDecimal usdtBalance, BigDecimal ethBalance, BigDecimal btcBalance) {
        this.usdtBalance = usdtBalance;
        this.ethBalance = ethBalance;
        this.btcBalance = btcBalance;
    }

    public void deductUsdt(BigDecimal amount) {
        if (usdtBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient USDT balance");
        }
        this.usdtBalance = this.usdtBalance.subtract(amount);
    }

    public void addUsdt(BigDecimal amount) {
        this.usdtBalance = this.usdtBalance.add(amount);
    }

    public void deductEth(BigDecimal amount) {
        if (ethBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient ETH balance");
        }
        this.ethBalance = this.ethBalance.subtract(amount);
    }

    public void addEth(BigDecimal amount) {
        this.ethBalance = this.ethBalance.add(amount);
    }

    public void deductBtc(BigDecimal amount) {
        if (btcBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient BTC balance");
        }
        this.btcBalance = this.btcBalance.subtract(amount);
    }

    public void addBtc(BigDecimal amount) {
        this.btcBalance = this.btcBalance.add(amount);
    }
}
