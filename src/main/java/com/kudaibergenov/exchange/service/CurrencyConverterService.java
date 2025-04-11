package com.kudaibergenov.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConverterService {

    private final FxKgService fxKgService;
    private final ObjectMapper objectMapper;

    public CurrencyConverterService(FxKgService fxKgService, ObjectMapper objectMapper) {
        this.fxKgService = fxKgService;
        this.objectMapper = objectMapper;
    }

    public ConversionResult convert(String from, String to, double amount) {
        try {
            String response = fxKgService.getCentralBankRates();
            JsonNode root = objectMapper.readTree(response);

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