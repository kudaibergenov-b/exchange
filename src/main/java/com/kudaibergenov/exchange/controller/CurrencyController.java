package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.service.CurrencyForecastService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyDataService currencyDataService;
    private final CurrencyForecastService currencyForecastService;
    private final FxKgService fxKgService;

    public CurrencyController(CurrencyDataService currencyDataService,
                              CurrencyForecastService currencyForecastService,
                              FxKgService fxKgService) {
        this.currencyDataService = currencyDataService;
        this.currencyForecastService = currencyForecastService;
        this.fxKgService = fxKgService;
    }

    // --- Существующие эндпоинты ---

    @GetMapping("/rates/{date}")
    public ResponseEntity<List<CurrencyRate>> getRatesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(currencyDataService.getRatesByDate(date));
    }

    @GetMapping("/history")
    public ResponseEntity<List<CurrencyRate>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(currencyDataService.getHistory(start, end));
    }

    @GetMapping("/forecast/week")
    public ResponseEntity<?> forecastForWeek(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(currencyForecastService.forecastForWeek(currency, startDate));
    }

    @GetMapping("/test/week")
    public ResponseEntity<?> testModelForWeek(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(currencyForecastService.testModelForWeek(currency, startDate));
    }

    // --- Новые эндпоинты для API fx.kg ---

    @GetMapping("/fxkg/average")
    public ResponseEntity<String> getAverageRates() {
        return ResponseEntity.ok(fxKgService.getAverageRates());
    }

    @GetMapping("/fxkg/best")
    public ResponseEntity<String> getBestRates() {
        return ResponseEntity.ok(fxKgService.getBestRates());
    }

    @GetMapping("/fxkg/current")
    public ResponseEntity<String> getCurrentRates() {
        return ResponseEntity.ok(fxKgService.getCurrentRates());
    }

    @GetMapping("/fxkg/central")
    public ResponseEntity<String> getCentralBankRates() {
        return ResponseEntity.ok(fxKgService.getCentralBankRates());
    }
}