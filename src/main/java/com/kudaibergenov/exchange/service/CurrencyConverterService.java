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

    public double convert(String from, String to, double amount) {
        try {
            // Получаем JSON с курсами валют
            String response = fxKgService.getCentralBankRates();
            JsonNode jsonNode = objectMapper.readTree(response);

            // Проверяем, есть ли нужные валюты
            if (!jsonNode.has(from) || !jsonNode.has(to)) {
                throw new IllegalArgumentException("Currency not found in rates");
            }

            // Извлекаем курсы
            double fromRate = jsonNode.get(from).asDouble();
            double toRate = jsonNode.get(to).asDouble();

            // Конвертация
            return amount * (toRate / fromRate);

        } catch (Exception e) {
            throw new RuntimeException("Error processing JSON response", e);
        }
    }
}