package com.trading.system.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "aggregated_prices", uniqueConstraints = @UniqueConstraint(columnNames = { "symbol" }))
@Getter
@Setter
@NoArgsConstructor
public class AggregatedPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal askPrice;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AggregatedPrice(String symbol, BigDecimal bidPrice, BigDecimal askPrice) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
    }
}
