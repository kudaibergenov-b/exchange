package com.kudaibergenov.exchange.controller;

import com.kudaibergenov.exchange.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/update")
    public String updateRates() {
        currencyService.fetchAndSaveRates();
        return "Currency rates updated!";
    }
}