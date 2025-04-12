package com.kudaibergenov.exchange.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class ForecastResponse {

    private final String currency;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<BigDecimal> predictedRates;

    public ForecastResponse(String currency, LocalDate startDate, LocalDate endDate, List<BigDecimal> predictedRates) {
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.predictedRates = predictedRates;
    }

}
