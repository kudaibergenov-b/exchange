package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.dto.ApiResponse;
import com.kudaibergenov.exchange.service.CurrencyForecastService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

    private final CurrencyForecastService currencyForecastService;

    public ForecastController(CurrencyForecastService currencyForecastService) {
        this.currencyForecastService = currencyForecastService;
    }

    @GetMapping("/week")
    public ResponseEntity<ApiResponse<?>> forecastForWeek(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(new ApiResponse<>(currencyForecastService.forecastForWeek(currency, startDate)));
    }

    @GetMapping("/test/week")
    public ResponseEntity<ApiResponse<?>> testModelForWeek(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(new ApiResponse<>(currencyForecastService.testModelForWeek(currency, startDate)));
    }
}
