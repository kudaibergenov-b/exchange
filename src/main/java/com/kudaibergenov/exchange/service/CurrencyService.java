package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.ExcelImporter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CurrencyService {

    private final ExcelImporter excelImporter;
    private final CurrencyRateRepository repository;

    public CurrencyService(ExcelImporter excelImporter, CurrencyRateRepository repository) {
        this.excelImporter = excelImporter;
        this.repository = repository;
    }

    public void importFromExcel(String filePath) {
        excelImporter.importData(filePath);
    }

    // ✅ Получаем исторические данные за последние N дней
    public List<CurrencyRate> getHistoricalRates(String currency, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);
    }
}