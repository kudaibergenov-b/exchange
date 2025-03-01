package com.kudaibergenov.exchange.controller;

import org.springframework.web.bind.annotation.*;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/test")
public class TestController {
    private final CurrencyRateRepository repository;

    public TestController(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/save")
    public CurrencyRate saveRate() {
        CurrencyRate rate = new CurrencyRate(LocalDateTime.now(), 87.45, 91.61, 0.99, 0.174);

        return repository.save(rate);
    }
}

