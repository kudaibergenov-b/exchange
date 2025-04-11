package com.kudaibergenov.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConverterService {

    private final FxKgService fxKgService;

    public CurrencyConverterService(FxKgService fxKgService) {
        this.fxKgService = fxKgService;
    }

    public ConversionResult convert(String from, String to, double amount) {
        try {
            // Получаем данные с FX.KG API в виде JsonNode
            JsonNode root = fxKgService.getCentralBankRates();

            double fromRate = getCurrencyRate(root, from);
            double toRate = getCurrencyRate(root, to);

            double convertedAmount = amount * (fromRate / toRate);

            return new ConversionResult(from.toUpperCase(), to.toUpperCase(), amount, fromRate, toRate, convertedAmount);

        } catch (Exception e) {
            throw new RuntimeException("Error during currency conversion", e);
        }
    }

    private double getCurrencyRate(JsonNode root, String currency) {
        if (currency.equalsIgnoreCase("KGS")) {
            return 1.0;
        } else {
            JsonNode currencyNode = root.get(currency.toLowerCase());
            if (currencyNode == null) {
                throw new IllegalArgumentException("Currency '" + currency + "' not found in FX.KG rates");
            }
            return currencyNode.asDouble();
        }
    }

    public static class ConversionResult {
        public String from;
        public String to;
        public double amount;
        public double fromRate;
        public double toRate;
        public double convertedAmount;

        public ConversionResult(String from, String to, double amount, double fromRate, double toRate, double convertedAmount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.fromRate = fromRate;
            this.toRate = toRate;
            this.convertedAmount = convertedAmount;
        }
    }
}
