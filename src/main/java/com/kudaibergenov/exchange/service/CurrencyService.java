package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.util.ExcelImporter;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {
    private final ExcelImporter excelImporter;

    public CurrencyService(ExcelImporter excelImporter) {
        this.excelImporter = excelImporter;
    }

    public void importFromExcel(String filePath) {
        excelImporter.importData(filePath);
    }
}