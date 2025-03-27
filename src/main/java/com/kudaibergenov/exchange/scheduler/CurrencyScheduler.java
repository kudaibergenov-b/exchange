package com.kudaibergenov.exchange.scheduler;

import com.kudaibergenov.exchange.service.CurrencyForecastService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CurrencyScheduler {

    private final CurrencyForecastService currencyForecastService;

    public CurrencyScheduler(CurrencyForecastService currencyForecastService) {
        this.currencyForecastService = currencyForecastService;
    }

    @Scheduled(cron = "0 0 12 * * ?", zone = "Asia/Bishkek") // Каждый день в 12:00
    public void updateRates() {
        System.out.println("Fetching latest currency rates...");
        //currencyService.fetchAndSaveHistoricalRates(LocalDate.now(), LocalDate.now());
    }
}
