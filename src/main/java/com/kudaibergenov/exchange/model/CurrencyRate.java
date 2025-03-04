package com.kudaibergenov.exchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@Table(name = "currency_rates", uniqueConstraints = @UniqueConstraint(columnNames = {"date", "currencyCode"}))
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    private LocalDate date;
    @Setter
    @Getter
    private String currencyCode;

    @Getter
    private Double rate;

    public CurrencyRate() {}

    public CurrencyRate(LocalDate date, String currencyCode, double rate) {
        this.date = date;
        this.currencyCode = currencyCode;
        this.rate = rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}