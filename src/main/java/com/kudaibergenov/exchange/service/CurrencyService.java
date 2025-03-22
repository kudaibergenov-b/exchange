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

    public void importFromExcel(String filePath) {
        try {
            excelImporter.importData(filePath);
            logger.info("Импорт завершен успешно!");
        } catch (Exception e) {
            logger.severe("Ошибка при импорте файла: " + e.getMessage());
        }
    }

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

    public List<CurrencyRate> predictExchangeRate(String currency, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(365); // Используем больше данных

        List<CurrencyRate> historicalRates = repository.findByDateBetweenAndCurrencyCode(startDate, endDate, currency);

        if (historicalRates.size() < 30) {
            logger.warning("Недостаточно данных для прогнозирования валюты: " + currency);
            throw new IllegalStateException("Недостаточно данных для прогнозирования");
        }

        historicalRates = historicalRates.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .toList();

        List<BigDecimal> rates = historicalRates.stream()
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());

        BigDecimal[] predictions = ArimaModel.predict(rates, days);
        LocalDate predictionDate = endDate.plusDays(1);

        return IntStream.range(0, days)
                .mapToObj(i -> new CurrencyRate(
                        predictionDate.plusDays(i),
                        currency,
                        predictions[i]
                ))
                .collect(Collectors.toList());
    }

    // ✅ Метод тестирования модели на прошедшем периоде (например, декабрь 2024)
    public void testModelForPastWeek(String currency, int year, int month, int startDay) {
        LocalDate startOfWeek = LocalDate.of(year, month, startDay);
        LocalDate endOfWeek = startOfWeek.plusDays(6); // Прогнозируем 7 дней

        // ✅ Получаем фактические курсы за указанную неделю
        List<CurrencyRate> actualRates = repository.findByDateBetweenAndCurrencyCode(startOfWeek, endOfWeek, currency);
        if (actualRates.isEmpty()) {
            throw new IllegalStateException("Нет данных за " + startOfWeek + " - " + endOfWeek + " для " + currency);
        }

        // ✅ Используем данные за 2-3 года до начала недели для обучения
        LocalDate lastTrainingDate = startOfWeek.minusDays(1);
        LocalDate trainingStartDate = lastTrainingDate.minusYears(2); // Берем 2 года данных
        List<CurrencyRate> trainingData = repository.findByDateBetweenAndCurrencyCode(trainingStartDate, lastTrainingDate, currency);

        if (trainingData.size() < 100) { // Должно быть хотя бы 100 точек для обучения
            throw new IllegalStateException("Недостаточно данных для обучения модели.");
        }

        // ✅ Сортируем и преобразуем в BigDecimal
        trainingData = trainingData.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .toList();
        List<BigDecimal> trainingRates = trainingData.stream()
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());

        // ✅ Прогнозируем курс на неделю
        int daysToPredict = actualRates.size();
        BigDecimal[] predictedRates = ArimaModel.predict(trainingRates, daysToPredict);

        // ✅ Вычисляем среднюю абсолютную ошибку (MAE)
        BigDecimal totalError = BigDecimal.ZERO;
        for (int i = 0; i < daysToPredict; i++) {
            BigDecimal error = actualRates.get(i).getRate().subtract(predictedRates[i]).abs();
            totalError = totalError.add(error);
        }
        BigDecimal mae = totalError.divide(BigDecimal.valueOf(daysToPredict), BigDecimal.ROUND_HALF_UP);

        // ✅ Логируем результаты
        logger.info("📊 Тестирование модели для " + currency + " за неделю: " + startOfWeek + " - " + endOfWeek);
        for (int i = 0; i < daysToPredict; i++) {
            logger.info("Дата: " + actualRates.get(i).getDate() +
                    " | Фактический: " + actualRates.get(i).getRate() +
                    " | Прогнозируемый: " + predictedRates[i]);
        }
        logger.info("📌 Средняя абсолютная ошибка (MAE): " + mae);
    }

    // ✅ Метод тестирования модели на прошедшей неделе с заданными параметрами ARIMA(p, d, q)
    public void testModelForPastWeekWithFixedParams(String currency, int year, int month, int startDay, int p, int d, int q) {
        LocalDate startOfWeek = LocalDate.of(year, month, startDay);
        LocalDate endOfWeek = startOfWeek.plusDays(6); // Прогнозируем 7 дней

        // ✅ Получаем фактические курсы за указанную неделю
        List<CurrencyRate> actualRates = repository.findByDateBetweenAndCurrencyCode(startOfWeek, endOfWeek, currency);
        if (actualRates.isEmpty()) {
            throw new IllegalStateException("Нет данных за " + startOfWeek + " - " + endOfWeek + " для " + currency);
        }

        // ✅ Используем данные за 2-3 года до начала недели для обучения
        LocalDate lastTrainingDate = startOfWeek.minusDays(1);
        LocalDate trainingStartDate = lastTrainingDate.minusYears(2); // Берем 2 года данных
        List<CurrencyRate> trainingData = repository.findByDateBetweenAndCurrencyCode(trainingStartDate, lastTrainingDate, currency);

        if (trainingData.size() < 100) { // Должно быть хотя бы 100 точек для обучения
            throw new IllegalStateException("Недостаточно данных для обучения модели.");
        }

        // ✅ Сортируем и преобразуем в BigDecimal
        trainingData = trainingData.stream()
                .sorted(Comparator.comparing(CurrencyRate::getDate))
                .toList();
        List<BigDecimal> trainingRates = trainingData.stream()
                .map(CurrencyRate::getRate)
                .collect(Collectors.toList());

        // ✅ Тестируем ARIMA с заданными параметрами
        int daysToPredict = actualRates.size();
        BigDecimal[] predictedRates = ArimaModel.predict(trainingRates, daysToPredict, p, d, q);

        // ✅ Вычисляем среднюю абсолютную ошибку (MAE)
        BigDecimal totalError = BigDecimal.ZERO;
        for (int i = 0; i < daysToPredict; i++) {
            BigDecimal error = actualRates.get(i).getRate().subtract(predictedRates[i]).abs();
            totalError = totalError.add(error);
        }
        BigDecimal mae = totalError.divide(BigDecimal.valueOf(daysToPredict), BigDecimal.ROUND_HALF_UP);

        // ✅ Логируем результаты
        logger.info("📊 Тестирование модели с ARIMA(" + p + "," + d + "," + q + ") для " + currency + " за неделю: " + startOfWeek + " - " + endOfWeek);
        for (int i = 0; i < daysToPredict; i++) {
            logger.info("Дата: " + actualRates.get(i).getDate() +
                    " | Фактический: " + actualRates.get(i).getRate() +
                    " | Прогнозируемый: " + predictedRates[i]);
        }
        logger.info("📌 Средняя абсолютная ошибка (MAE): " + mae);
    }

}
