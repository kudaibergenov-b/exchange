package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.service.CurrencyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyRateRepository repository;
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyRateRepository repository, CurrencyService currencyService) {
        this.repository = repository;
        this.currencyService = currencyService;
    }

    @PostMapping("/import-excel")
    public String importExcelData(@RequestParam String filePath) {
        currencyService.importFromExcel(filePath);
        return "Excel data imported from: " + filePath;
    }

    @GetMapping("/rates/{date}")
    public ResponseEntity<List<CurrencyRate>> getRatesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CurrencyRate> rates = repository.findByDate(date);
        return rates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rates);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CurrencyRate>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CurrencyRate> rates = repository.findByDateBetween(start, end);
        return rates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rates);
    }

    // ✅ Получить исторические курсы за последние N дней
    @GetMapping("/history/{currency}/{days}")
    public List<CurrencyRate> getHistoricalRates(@PathVariable String currency, @PathVariable int days) {
        return currencyService.getHistoricalRates(currency, days);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCurrencyRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String currencyCode,
            @RequestParam Double rate) {

        Optional<CurrencyRate> existingRate = repository.findByDateAndCurrencyCode(date, currencyCode);
        if (existingRate.isPresent()) {
            return ResponseEntity.badRequest().body("Rate for " + currencyCode + " on " + date + " already exists!");
        }

        CurrencyRate newRate = new CurrencyRate(date, currencyCode, rate);
        repository.save(newRate);
        return ResponseEntity.ok("Added new currency rate: " + newRate);
    }
}