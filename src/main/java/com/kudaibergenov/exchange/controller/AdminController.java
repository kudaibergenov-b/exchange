package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.service.CurrencyDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CurrencyDataService currencyDataService;

    public AdminController(CurrencyDataService currencyDataService) {
        this.currencyDataService = currencyDataService;
    }

    @PostMapping("/import-excel")
    public ResponseEntity<String> importExcelData(@RequestParam String filePath) {
        currencyDataService.importFromExcel(filePath);
        return ResponseEntity.ok("Excel data successfully imported from: " + filePath);
    }

    @DeleteMapping("/delete-all-data")
    public ResponseEntity<String> deleteAllData() {
        currencyDataService.deleteAllData();
        return ResponseEntity.ok("All currency data deleted.");
    }
}