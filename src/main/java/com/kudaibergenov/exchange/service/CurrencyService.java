package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.ArimaModel;
import com.kudaibergenov.exchange.util.ExcelImporter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CurrencyService {

    private final ExcelImporter excelImporter;
    private final CurrencyRateRepository repository;

    public CurrencyService(ExcelImporter excelImporter, CurrencyRateRepository repository) {
        this.excelImporter = excelImporter;
        this.repository = repository;
    }

    public void importFromExcel(String filePath) {
        excelImporter.importData(filePath);
    }

    // ✅ Получаем исторические данные за последние N дней
    public List<CurrencyRate> getHistoricalRates(String currency, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);
    }

    public List<CurrencyRate> predictExchangeRate(String currency, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);

        List<CurrencyRate> historicalRates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);

        if (historicalRates.size() < 10) {
            throw new IllegalStateException("Недостаточно данных для прогнозирования");
        }

        List<BigDecimal> rates = historicalRates.stream()
                .map(rate -> BigDecimal.valueOf(rate.getRate())) // ✅ Явное преобразование Double → BigDecimal
                .collect(Collectors.toList());

        double[] predictions = ArimaModel.predict(rates, days);
        LocalDate predictionDate = endDate.plusDays(1);

        // ✅ Добавляем дату и валюту в результат
        return IntStream.range(0, days)
                .mapToObj(i -> new CurrencyRate(
                        predictionDate.plusDays(i), // Дата прогноза
                        currency,                   // Код валюты
                        predictions[i]               // Прогнозируемый курс
                ))
                .collect(Collectors.toList());
    }


}