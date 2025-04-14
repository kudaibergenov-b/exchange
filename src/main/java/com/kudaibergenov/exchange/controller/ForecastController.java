package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.dto.*;
import com.kudaibergenov.exchange.service.CurrencyForecastService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final CurrencyForecastService currencyForecastService;

    public ForecastController(CurrencyForecastService currencyForecastService) {
        this.currencyForecastService = currencyForecastService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ForecastResponse>> forecast(@Valid @RequestBody ForecastRequest request) {
        ForecastResponse response = currencyForecastService.forecast(
                request.getCurrency(),
                request.getStartDate(),
                request.getDays()
        );
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<TestModelResponse>> testModel(@Valid @RequestBody TestModelRequest request) {
        TestModelResponse response = currencyForecastService.testModel(
                request.getCurrency(),
                request.getStartDate(),
                request.getDays()
        );
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
