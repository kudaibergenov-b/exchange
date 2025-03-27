package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.service.CurrencyForecastService;
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

    public CurrencyController(CurrencyDataService currencyDataService, CurrencyForecastService currencyForecastService) {
        this.currencyDataService = currencyDataService;
        this.currencyForecastService = currencyForecastService;
    }

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
}