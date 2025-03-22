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

    // ✅ Запросить прогноз курса валют
    @GetMapping("/predict/{currency}/{days}")
    public ResponseEntity<List<CurrencyRate>> getPrediction(
            @PathVariable String currency,
            @PathVariable int days) {

        if (days <= 0) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            List<CurrencyRate> predictions = currencyService.predictExchangeRate(currency, days);
            return ResponseEntity.ok(predictions);
        } catch (IllegalStateException e) {
            logger.warning("Ошибка при прогнозировании для " + currency + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // ✅ Запрос на тестирование прогноза на неделю с фактическими данными
    @GetMapping("/test-week/{currency}/{year}/{month}/{startDay}")
    public ResponseEntity<String> testModelForPastWeek(
            @PathVariable String currency,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int startDay) {
        try {
            currencyService.testModelForPastWeek(currency, year, month, startDay);
            return ResponseEntity.ok("Тест модели на неделю успешно выполнен!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/test-fixed-arima-week")
    public ResponseEntity<String> testFixedArimaWeek(
            @RequestParam String currency,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int startDay,
            @RequestParam int p,
            @RequestParam int d,
            @RequestParam int q) {
        currencyService.testModelForPastWeekWithFixedParams(currency, year, month, startDay, p, d, q);
        return ResponseEntity.ok("ARIMA (" + p + "," + d + "," + q + ") протестирована для " + currency + " на неделю.");
    }

}
