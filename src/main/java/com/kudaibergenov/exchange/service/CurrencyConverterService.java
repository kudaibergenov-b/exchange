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

            double fromRate;
            if (from.equalsIgnoreCase("KGS")) {
                fromRate = 1.0;
            } else {
                JsonNode fromNode = root.get(from.toLowerCase());
                if (fromNode == null) {
                    throw new IllegalArgumentException("Currency '" + from + "' not found in FX.KG rates");
                }
                fromRate = fromNode.asDouble();
            }

            double toRate;
            if (to.equalsIgnoreCase("KGS")) {
                toRate = 1.0;
            } else {
                JsonNode toNode = root.get(to.toLowerCase());
                if (toNode == null) {
                    throw new IllegalArgumentException("Currency '" + to + "' not found in FX.KG rates");
                }
                toRate = toNode.asDouble();
            }

            double convertedAmount = amount * (fromRate / toRate);

            return new ConversionResult(from.toUpperCase(), to.toUpperCase(), amount, fromRate, toRate, convertedAmount);

        } catch (Exception e) {
            throw new RuntimeException("Error during currency conversion", e);
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

        public String getFrom() { return from; }
        public String getTo() { return to; }
        public double getAmount() { return amount; }
        public double getFromRate() { return fromRate; }
        public double getToRate() { return toRate; }
        public double getConvertedAmount() { return convertedAmount; }
    }
}