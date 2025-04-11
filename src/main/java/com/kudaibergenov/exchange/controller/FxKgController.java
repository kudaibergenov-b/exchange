package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.dto.ApiResponse;
import com.kudaibergenov.exchange.service.FxKgService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fxkg")
public class FxKgController {

    private final FxKgService fxKgService;

    public FxKgController(FxKgService fxKgService) {
        this.fxKgService = fxKgService;
    }

    @GetMapping("/average")
    public ResponseEntity<ApiResponse<String>> getAverageRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getAverageRates()));
    }

    @GetMapping("/best")
    public ResponseEntity<ApiResponse<String>> getBestRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getBestRates()));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<String>> getCurrentRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getCurrentRates()));
    }

    @GetMapping("/central")
    public ResponseEntity<ApiResponse<String>> getCentralBankRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getCentralBankRates()));
    }
}
