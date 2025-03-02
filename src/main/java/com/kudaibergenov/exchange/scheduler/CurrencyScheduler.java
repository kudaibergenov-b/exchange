package com.kudaibergenov.exchange.scheduler;

import com.kudaibergenov.exchange.service.CurrencyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CurrencyScheduler {

    private final CurrencyService currencyService;

    public CurrencyScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(cron = "0 0 12 * * ?", zone = "Asia/Bishkek") // Каждый день в 12:00
    public void updateRates() {
        System.out.println("Fetching latest currency rates...");
        currencyService.fetchAndSaveHistoricalRates(LocalDate.now(), LocalDate.now());
    }
}
