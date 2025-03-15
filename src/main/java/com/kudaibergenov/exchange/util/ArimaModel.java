package com.kudaibergenov.exchange.util;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class ArimaModel {

    // ✅ Прогнозирование с использованием ARIMA
    public static BigDecimal[] predict(List<BigDecimal> data, int days) {
        // ✅ Копируем данные для работы
        List<BigDecimal> differenced = new ArrayList<>(data);

        // ✅ Определяем параметры ARIMA (p, d, q)
        int[] bestParams = findBestParams(differenced);
        int p = bestParams[0];
        int d = bestParams[1];
        int q = bestParams[2];

        // ✅ Применяем дифференцирование, если d > 0
        for (int i = 0; i < d; i++) {
            differenced = difference(differenced);
        }

        // ✅ Вычисляем AR и MA коэффициенты
        BigDecimal[] arCoefficients = calculateAR(differenced, p);
        BigDecimal[] maCoefficients = calculateMA(differenced, q);

        // ✅ Прогнозируем будущие значения
        BigDecimal[] predictions = new BigDecimal[days];
        for (int i = 0; i < days; i++) {
            predictions[i] = predictNext(differenced, arCoefficients, maCoefficients);
            differenced.add(predictions[i]);
        }

        // ✅ Восстанавливаем прогнозируемые значения (если d > 0)
        if (d > 0) {
            predictions = restorePredictions(predictions, data.get(data.size() - 1));
        }

        return predictions;
    }

    // ✅ Восстанавливаем данные после разностей
    private static BigDecimal[] restorePredictions(BigDecimal[] differenced, BigDecimal lastValue) {
        BigDecimal[] restored = new BigDecimal[differenced.length];
        BigDecimal value = lastValue;
        for (int i = 0; i < differenced.length; i++) {
            value = value.add(differenced[i]);
            restored[i] = value;
        }
        return restored;
    }

    // ✅ Определение лучших параметров ARIMA
    public static int[] findBestParams(List<BigDecimal> data) {
        int bestP = 0, bestD = 0, bestQ = 0;
        BigDecimal bestError = BigDecimal.valueOf(Double.MAX_VALUE);

        int maxP = Math.min(3, data.size() - 2);
        int maxD = Math.min(2, data.size() - 1);
        int maxQ = Math.min(3, data.size() - 2);

        for (int p = 0; p <= maxP; p++) {
            for (int d = 0; d <= maxD; d++) {
                for (int q = 0; q <= maxQ; q++) {
                    try {
                        BigDecimal error = testModel(data, p, d, q);
                        if (error.compareTo(bestError) < 0) {
                            bestError = error;
                            bestP = p;
                            bestD = d;
                            bestQ = q;
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка при p=" + p + ", d=" + d + ", q=" + q + ": " + e.getMessage());
                    }
                }
            }
        }

        return new int[]{bestP, bestD, bestQ};
    }

    // ✅ Тестируем модель
    private static BigDecimal testModel(List<BigDecimal> data, int p, int d, int q) {
        List<BigDecimal> differenced = data;
        for (int i = 0; i < d; i++) {
            differenced = difference(differenced);
        }

        BigDecimal[] arCoefficients = calculateAR(differenced, p);
        BigDecimal[] maCoefficients = calculateMA(differenced, q);

        BigDecimal prediction = predictNext(differenced, arCoefficients, maCoefficients);
        BigDecimal actual = data.get(data.size() - 1);

        return prediction.subtract(actual).abs();
    }

    // ✅ Расчет коэффициентов AR
    public static BigDecimal[] calculateAR(List<BigDecimal> data, int p) {
        int n = data.size();
        if (n <= p) {
            throw new IllegalArgumentException("Недостаточно данных для AR(p)");
        }

        double[] y = new double[n - p];
        double[][] x = new double[n - p][p];

        for (int i = p; i < n; i++) {
            y[i - p] = data.get(i).doubleValue();
            for (int j = 0; j < p; j++) {
                x[i - p][j] = data.get(i - j - 1).doubleValue();
            }
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);

        return DoubleStream.of(regression.estimateRegressionParameters())
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    // ✅ Расчет коэффициентов MA
    public static BigDecimal[] calculateMA(List<BigDecimal> data, int q) {
        if (data.size() < q + 1) {
            throw new IllegalArgumentException("Недостаточно данных для MA(q). Нужно как минимум " + (q + 1) + " точек.");
        }

        List<BigDecimal> errors = IntStream.range(1, data.size())
                .mapToObj(i -> data.get(i).subtract(data.get(i - 1)))
                .toList();

        if (errors.size() < q) {
            throw new IllegalArgumentException("Недостаточно ошибок для MA(q)");
        }

        double[] y = new double[errors.size() - q];
        double[][] x = new double[errors.size() - q][q];

        for (int i = q; i < errors.size(); i++) {
            y[i - q] = errors.get(i).doubleValue();
            for (int j = 0; j < q; j++) {
                x[i - q][j] = errors.get(i - j - 1).doubleValue();
            }
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);

        return DoubleStream.of(regression.estimateRegressionParameters())
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    // ✅ Прогнозирование на основе ARMA
    public static BigDecimal predictNext(List<BigDecimal> history, BigDecimal[] arCoefficients, BigDecimal[] maCoefficients) {
        int p = arCoefficients.length - 1;
        int q = maCoefficients.length - 1;

        BigDecimal prediction = arCoefficients[0];

        for (int i = 0; i < p; i++) {
            prediction = prediction.add(arCoefficients[i + 1].multiply(history.get(history.size() - 1 - i)));
        }

        List<BigDecimal> errors = IntStream.range(1, history.size())
                .mapToObj(i -> history.get(i).subtract(history.get(i - 1)))
                .toList();

        for (int i = 0; i < q && i < errors.size(); i++) {
            prediction = prediction.add(maCoefficients[i + 1].multiply(errors.get(errors.size() - 1 - i)));
        }

        return prediction;
    }

    // ✅ Разности временного ряда
    public static List<BigDecimal> difference(List<BigDecimal> data) {
        return IntStream.range(1, data.size())
                .mapToObj(i -> data.get(i).subtract(data.get(i - 1)))
                .collect(Collectors.toList());
    }
}
