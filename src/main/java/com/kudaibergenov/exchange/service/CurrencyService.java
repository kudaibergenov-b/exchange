package com.kudaibergenov.exchange.service;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import com.kudaibergenov.exchange.util.ArimaModel;
import com.kudaibergenov.exchange.util.ExcelImporter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CurrencyService {

    private static final Logger logger = Logger.getLogger(CurrencyService.class.getName());

    private final ExcelImporter excelImporter;
    private final CurrencyRateRepository repository;

    public CurrencyService(ExcelImporter excelImporter, CurrencyRateRepository repository) {
        this.excelImporter = excelImporter;
        this.repository = repository;
    }

    // ✅ Импорт данных из Excel
    public void importFromExcel(String filePath) {
        try {
            excelImporter.importData(filePath);
            logger.info("Импорт завершен успешно!");
        } catch (Exception e) {
            logger.severe("Ошибка при импорте файла: " + e.getMessage());
        }
    }

    // ✅ Получаем исторические данные за последние N дней
    public List<CurrencyRate> getHistoricalRates(String currency, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Количество дней должно быть положительным");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<CurrencyRate> rates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);

        if (rates.isEmpty()) {
            logger.warning("Нет данных за последние " + days + " дней для валюты: " + currency);
        }

        return rates;
    }

    // ✅ Прогнозирование курсов валют на N дней вперед
    public List<CurrencyRate> predictExchangeRate(String currency, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);

        List<CurrencyRate> historicalRates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);

        if (historicalRates.size() < 10) {
            logger.warning("Недостаточно данных для прогнозирования валюты: " + currency);
            throw new IllegalStateException("Недостаточно данных для прогнозирования");
        }

        // ✅ Сортируем данные по дате (если вдруг в БД не в хронологическом порядке)
        historicalRates = historicalRates.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .toList();

        List<BigDecimal> rates = historicalRates.stream()
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());

        BigDecimal[] predictions = ArimaModel.predict(rates, days);
        LocalDate predictionDate = endDate.plusDays(1);

        // ✅ Создаем список прогнозируемых курсов
        List<CurrencyRate> predictedRates = IntStream.range(0, days)
                .mapToObj(i -> new CurrencyRate(
                        predictionDate.plusDays(i),  // Дата прогноза
                        currency,                    // Код валюты
                        predictions[i]               // Прогнозируемый курс
                ))
                .collect(Collectors.toList());

        logger.info("Прогнозирование завершено для " + currency + " на " + days + " дней");

        return predictedRates;
    }
}
