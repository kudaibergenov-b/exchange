package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> importExcelData(@RequestParam String filePath) {
        currencyDataService.importFromExcel(filePath);
        return ResponseEntity.ok(ApiResponse.ok("Excel data successfully imported", filePath));
    }

    @DeleteMapping("/delete-all-data")
    public ResponseEntity<ApiResponse<String>> deleteAllData() {
        currencyDataService.deleteAllData();
        return ResponseEntity.ok(ApiResponse.ok("All currency data deleted.", null));
    }
}
