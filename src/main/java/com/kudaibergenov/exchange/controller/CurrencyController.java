package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.service.CurrencyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyRateRepository repository;
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyRateRepository repository, CurrencyService currencyService) {
        this.repository = repository;
        this.currencyService = currencyService;
    }

    @GetMapping("/latest")
    public CurrencyRate getLatestRate() {
        return repository.findTopByOrderByDateDesc()
                .orElseThrow(() -> new RuntimeException("No currency rates found"));
    }

    @GetMapping("/history")
    public List<CurrencyRate> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return repository.findByDateBetween(start, end);
    }

    @PostMapping("/fetch-historical")
    public String fetchHistoricalRates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        currencyService.fetchAndSaveHistoricalRates(start, end);
        return "Historical data fetched and saved from " + start + " to " + end;
    }

    @PostMapping("/import-excel")
    public String importExcelData(@RequestParam String filePath) {
        currencyService.importFromExcel(filePath);
        return "Excel data imported from: " + filePath;
    }
}