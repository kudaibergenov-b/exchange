package com.kudaibergenov.exchange.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kudaibergenov.exchange.dto.ApiResponse;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyDataService currencyDataService;
    private final FxKgService fxKgService;
    private final ObjectMapper objectMapper;

    public CurrencyController(CurrencyDataService currencyDataService, FxKgService fxKgService, ObjectMapper objectMapper) {
        this.currencyDataService = currencyDataService;
        this.fxKgService = fxKgService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableCurrencies() {
        try {
            String response = fxKgService.getCentralBankRates();
            JsonNode jsonNode = objectMapper.readTree(response);

            List<String> currencies = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(code -> {
                if (!List.of("id", "created_at", "updated_at", "is_current").contains(code)) {
                    currencies.add(code.toUpperCase());
                }
            });

            return ResponseEntity.ok(new ApiResponse<>(currencies));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Failed to fetch currency list", null));
        }
    }

    @GetMapping("/rates/{date}")
    public ResponseEntity<ApiResponse<List<CurrencyRate>>> getRatesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CurrencyRate> rates = currencyDataService.getRatesByDate(date);
        return ResponseEntity.ok(new ApiResponse<>(rates));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CurrencyRate>>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CurrencyRate> history = currencyDataService.getHistory(start, end);
        return ResponseEntity.ok(new ApiResponse<>(history));
    }
}
