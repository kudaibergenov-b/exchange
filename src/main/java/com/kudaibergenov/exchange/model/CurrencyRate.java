package com.kudaibergenov.exchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currency_rates")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private Double usd;
    private Double eur;
    private Double rub;
    private Double kzt;

    public CurrencyRate(LocalDateTime createdAt, Double usd, Double eur, Double rub, Double kzt) {
        this.createdAt = createdAt;
        this.usd = usd;
        this.eur = eur;
        this.rub = rub;
        this.kzt = kzt;
    }
}


