package com.trading.system.domain.repository;

import com.trading.system.domain.model.AggregatedPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AggregatedPriceRepository extends JpaRepository<AggregatedPrice, Long> {

    @Query("SELECT ap FROM AggregatedPrice ap WHERE ap.symbol = :symbol ORDER BY ap.timestamp DESC LIMIT 1")
    Optional<AggregatedPrice> findLatestBySymbol(String symbol);

}
