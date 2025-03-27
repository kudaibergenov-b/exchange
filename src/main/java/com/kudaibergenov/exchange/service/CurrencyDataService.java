package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.ExcelImporter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CurrencyDataService {

    private final CurrencyRateRepository repository;
    private final ExcelImporter excelImporter;

    public CurrencyDataService(CurrencyRateRepository repository, ExcelImporter excelImporter) {
        this.repository = repository;
        this.excelImporter = excelImporter;
    }

    public void importFromExcel(String filePath) {
        excelImporter.importData(filePath);
    }

    public void deleteAllData() {
        repository.deleteAll();
    }

    public List<CurrencyRate> getRatesByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public List<CurrencyRate> getHistory(LocalDate start, LocalDate end) {
        return repository.findByDateBetween(start, end);
    }
}