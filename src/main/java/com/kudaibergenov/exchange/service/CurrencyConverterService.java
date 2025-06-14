package com.kudaibergenov.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kudaibergenov.exchange.dto.ConversionResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyConverterService {

    private final FxKgService fxKgService;

    public CurrencyConverterService(FxKgService fxKgService) {
        this.fxKgService = fxKgService;
    }

    public ConversionResult convert(String from, String to, double amount, Double customFromRate, Double customToRate) {
        double fromRate;
        double toRate;

        if (customFromRate != null && customToRate != null) {
            fromRate = customFromRate;
            toRate = customToRate;
        } else {
            JsonNode root = fxKgService.getCentralBankRates();
            fromRate = getCurrencyRate(root, from);
            toRate = getCurrencyRate(root, to);
        }

        double convertedAmount = amount * (fromRate / toRate);

        return new ConversionResult(
                from.toUpperCase(),
                to.toUpperCase(),
                amount,
                fromRate,
                toRate,
                round(convertedAmount)
        );
    }

    private double getCurrencyRate(JsonNode root, String currency) {
        if (currency.equalsIgnoreCase("KGS")) {
            return 1.0;
        }

        JsonNode currencyNode = root.get(currency.toLowerCase());
        if (currencyNode == null) {
            throw new IllegalArgumentException("Currency '" + currency + "' not found in FX.KG rates");
        }
        return currencyNode.asDouble();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}