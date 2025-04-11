package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.dto.ApiResponse;
import com.kudaibergenov.exchange.service.CurrencyConverterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/convert")
public class ConverterController {

    private final CurrencyConverterService converterService;

    public ConverterController(CurrencyConverterService converterService) {
        this.converterService = converterService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CurrencyConverterService.ConversionResult>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount
    ) {
        return ResponseEntity.ok(new ApiResponse<>(converterService.convert(from, to, amount)));
    }
}
