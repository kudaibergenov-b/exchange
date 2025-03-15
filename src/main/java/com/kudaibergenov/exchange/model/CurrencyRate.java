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

    @Getter
    private LocalDate date;

    @Getter
    private String currencyCode;

    private Double rate;

    public CurrencyRate() {}

    public CurrencyRate(LocalDate date, String currencyCode, double rate) {
        this.date = date;
        this.currencyCode = currencyCode;
        this.rate = rate;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Double getRate() {
        return rate;
    }
}