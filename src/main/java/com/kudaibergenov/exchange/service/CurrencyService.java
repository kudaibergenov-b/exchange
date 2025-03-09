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
        // Получаем исторические данные
        List<CurrencyRate> historicalRates = getHistoricalRates(currency, 90);

        // Если данных мало, прогноз невозможен
        if (historicalRates.size() < 2) {
            throw new IllegalStateException("Недостаточно данных для прогнозирования");
        }

        // Извлекаем курсы валют (rate) и конвертируем в BigDecimal
        List<BigDecimal> rates = historicalRates.stream()
                .map(rate -> BigDecimal.valueOf(rate.getRate())) // ✅ Преобразуем double → BigDecimal
                .collect(Collectors.toList());

        // Прогнозируем на N дней вперед
        double[] predictions = ArimaModel.predict(rates, days);

        // Создаем список прогнозов с датами
        LocalDate startDate = LocalDate.now().plusDays(1);
        return IntStream.range(0, days)
                .mapToObj(i -> new CurrencyRate(startDate.plusDays(i), currency, predictions[i]))
                .collect(Collectors.toList());
    }

}