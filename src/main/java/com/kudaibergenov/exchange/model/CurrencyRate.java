package com.kudaibergenov.exchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currency_rates", uniqueConstraints = @UniqueConstraint(columnNames = {"date", "currency_code"}))
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal rate;

    public CurrencyRate(LocalDate date, String currencyCode, BigDecimal rate) {
        this.date = date;
        this.currencyCode = currencyCode;
        this.rate = rate;
    }
}