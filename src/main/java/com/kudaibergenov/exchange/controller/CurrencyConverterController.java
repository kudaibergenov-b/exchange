package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.service.CurrencyConverterService;
import com.kudaibergenov.exchange.service.CurrencyConverterService.ConversionResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currency")
public class CurrencyConverterController {

    private final CurrencyConverterService converterService;

    public CurrencyConverterController(CurrencyConverterService converterService) {
        this.converterService = converterService;
    }

    @GetMapping("/convert")
    public ConversionResult convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount
    ) {
        return converterService.convert(from, to, amount);
    }
}