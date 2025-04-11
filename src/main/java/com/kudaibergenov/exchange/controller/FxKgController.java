package com.kudaibergenov.exchange.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
    public ResponseEntity<ApiResponse<JsonNode>> getAverageRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getAverageRates()));
    }

    @GetMapping("/best")
    public ResponseEntity<ApiResponse<JsonNode>> getBestRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getBestRates()));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<JsonNode>> getCurrentRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getCurrentRates()));
    }

    @GetMapping("/central")
    public ResponseEntity<ApiResponse<JsonNode>> getCentralBankRates() {
        return ResponseEntity.ok(new ApiResponse<>(fxKgService.getCentralBankRates()));
    }
}
