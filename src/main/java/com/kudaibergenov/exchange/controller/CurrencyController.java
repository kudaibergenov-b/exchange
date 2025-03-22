package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.service.CurrencyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private static final Logger logger = Logger.getLogger(CurrencyController.class.getName());

    private final CurrencyRateRepository repository;
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyRateRepository repository, CurrencyService currencyService) {
        this.repository = repository;
        this.currencyService = currencyService;
    }

    // ✅ Импорт данных из Excel
    @PostMapping("/import-excel")
    public ResponseEntity<String> importExcelData(@RequestParam String filePath) {
        try {
            currencyService.importFromExcel(filePath);
            return ResponseEntity.ok("Excel data successfully imported from: " + filePath);
        } catch (Exception e) {
            logger.severe("Ошибка импорта файла: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Ошибка импорта: " + e.getMessage());
        }
    }

    // ✅ Получить курсы за конкретную дату
    @GetMapping("/rates/{date}")
    public ResponseEntity<List<CurrencyRate>> getRatesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CurrencyRate> rates = repository.findByDate(date);
        return rates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rates);
    }

    // ✅ Получить курсы за период (start → end)
    @GetMapping("/history")
    public ResponseEntity<List<CurrencyRate>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<CurrencyRate> rates = repository.findByDateBetween(start, end);
        return rates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rates);
    }

    // ✅ Получить исторические курсы за последние N дней
    @GetMapping("/history/{currency}/{days}")
    public ResponseEntity<List<CurrencyRate>> getHistoricalRates(
            @PathVariable String currency,
            @PathVariable int days) {

        if (days <= 0) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<CurrencyRate> rates = currencyService.getHistoricalRates(currency, days);
        return rates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rates);
    }

    // ✅ Прогнозирование курса на неделю
    @GetMapping("/forecast/week")
    public ResponseEntity<?> forecastForWeek(
            @RequestParam String currency,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int startDay) {
        return ResponseEntity.ok(currencyService.forecastForWeek(currency, year, month, startDay));
    }

    // ✅ Тестирование модели на исторических данных
    @GetMapping("/test/week")
    public ResponseEntity<?> testModelForWeek(
            @RequestParam String currency,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int startDay) {
        return ResponseEntity.ok(currencyService.testModelForWeek(currency, year, month, startDay));
    }
}
