package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.dto.ApiResponse;
import com.kudaibergenov.exchange.dto.ConvertRequest;
import com.kudaibergenov.exchange.dto.ConversionResult;
import com.kudaibergenov.exchange.service.CurrencyConverterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/convert")
public class ConverterController {

    private final CurrencyConverterService converterService;

    public ConverterController(CurrencyConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversionResult>> convert(
            @RequestBody @Valid ConvertRequest request
    ) {
        ConversionResult result = converterService.convert(request.getFrom(), request.getTo(), request.getAmount());
        return ResponseEntity.ok(new ApiResponse<>(result));
    }
}