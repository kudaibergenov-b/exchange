package com.kudaibergenov.exchange.dto;

import com.kudaibergenov.exchange.model.CurrencyRate;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class TestModelResponse {

    private final String currency;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<CurrencyRate> actualRates;
    private final BigDecimal[] predictedRates;
    private final BigDecimal mae;

    public TestModelResponse(String currency, LocalDate startDate, LocalDate endDate,
                             List<CurrencyRate> actualRates, BigDecimal[] predictedRates, BigDecimal mae) {
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.actualRates = actualRates;
        this.predictedRates = predictedRates;
        this.mae = mae;
    }
}
