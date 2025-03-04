package com.kudaibergenov.exchange.repository;

import com.kudaibergenov.exchange.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
    Optional<CurrencyRate> findByDateAndCurrencyCode(LocalDate date, String currencyCode);
    List<CurrencyRate> findByDate(LocalDate date);
    List<CurrencyRate> findByDateBetween(LocalDate start, LocalDate end);
}