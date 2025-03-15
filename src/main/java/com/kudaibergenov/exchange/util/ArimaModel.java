package com.kudaibergenov.exchange.util;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

public class ArimaModel {

    // ✅ Финальная модель ARIMA
    public static double[] predict(List<BigDecimal> data, int days) {
        // ✅ Создаем изменяемый список вместо прямой ссылки
        List<BigDecimal> differenced = new ArrayList<>(data);

        // Определяем лучшие параметры p, d, q
        int[] bestParams = findBestParams(differenced);
        int p = bestParams[0];
        int d = bestParams[1];
        int q = bestParams[2];

        // Применяем дифференцирование (если d > 0)
        for (int i = 0; i < d; i++) {
            differenced = TimeSeriesProcessor.difference(differenced);
        }

        // Вычисляем AR и MA коэффициенты
        double[] arCoefficients = calculateAR(differenced, p);
        double[] maCoefficients = calculateMA(differenced, q);

        // Прогнозируем N будущих значений
        double[] predictions = new double[days];
        for (int i = 0; i < days; i++) {
            predictions[i] = predictNext(differenced, arCoefficients, maCoefficients);
            differenced.add(BigDecimal.valueOf(predictions[i])); // ✅ Теперь add() работает корректно
        }

        // Восстанавливаем данные (если d > 0)
        if (d > 0) {
            predictions = restorePredictions(predictions, data.get(data.size() - 1).doubleValue());
        }

        return predictions;
    }

    // ✅ Метод для восстановления данных после разностей
    private static double[] restorePredictions(double[] differenced, double lastValue) {
        double[] restored = new double[differenced.length];
        double value = lastValue;
        for (int i = 0; i < differenced.length; i++) {
            value += differenced[i];
            restored[i] = value;
        }
        return restored;
    }

    // ✅ Метод для подбора оптимальных p, d, q
    public static int[] findBestParams(List<BigDecimal> data) {
        int bestP = 0, bestD = 0, bestQ = 0;
        double bestError = Double.MAX_VALUE;

        int maxP = Math.min(3, data.size() - 2);
        int maxD = Math.min(2, data.size() - 1);
        int maxQ = Math.min(3, data.size() - 2);

        for (int p = 0; p <= maxP; p++) {
            for (int d = 0; d <= maxD; d++) {
                for (int q = 0; q <= maxQ; q++) {
                    try {
                        double error = testModel(data, p, d, q);
                        if (error < bestError) {
                            bestError = error;
                            bestP = p;
                            bestD = d;
                            bestQ = q;
                        }
                    } catch (Exception e) {
                        System.out.println("Ошибка при p=" + p + ", d=" + d + ", q=" + q + ": " + e.getMessage());
                    }
                }
            }
        }

        return new int[]{bestP, bestD, bestQ};
    }


    // ✅ Тестируем модель с разными p, d, q
    private static double testModel(List<BigDecimal> data, int p, int d, int q) {
        List<BigDecimal> differenced = data;
        for (int i = 0; i < d; i++) {
            differenced = TimeSeriesProcessor.difference(differenced);
        }

        double[] arCoefficients = calculateAR(differenced, p);
        double[] maCoefficients = calculateMA(differenced, q);

        double prediction = predictNext(differenced, arCoefficients, maCoefficients);
        double actual = data.get(data.size() - 1).doubleValue();

        return Math.abs(prediction - actual);
    }

    // ✅ Авторегрессия (AR)
    public static double[] calculateAR(List<BigDecimal> data, int p) {
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
        return regression.estimateRegressionParameters();
    }

    public static double[] calculateMA(List<BigDecimal> data, int q) {
        if (data.size() < q + 1) {
            throw new IllegalArgumentException("Недостаточно данных для MA(q). Нужно как минимум " + (q + 1) + " точек.");
        }

        List<Double> errors = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            double error = data.get(i).doubleValue() - data.get(i - 1).doubleValue();
            errors.add(error);
        }

        if (errors.size() < q) {
            throw new IllegalArgumentException("Недостаточно ошибок для MA(q)");
        }

        double[] y = new double[errors.size() - q];
        double[][] x = new double[errors.size() - q][q];

        for (int i = q; i < errors.size(); i++) {
            y[i - q] = errors.get(i);
            for (int j = 0; j < q; j++) {
                x[i - q][j] = errors.get(i - j - 1);
            }
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        return regression.estimateRegressionParameters();
    }

    // ✅ Прогнозирование на основе ARMA
    public static double predictNext(List<BigDecimal> history, double[] arCoefficients, double[] maCoefficients) {
        int p = arCoefficients.length - 1;
        int q = maCoefficients.length - 1;

        double prediction = arCoefficients[0]; // Свободный член AR

        for (int i = 0; i < p; i++) {
            prediction += arCoefficients[i + 1] * history.get(history.size() - 1 - i).doubleValue();
        }

        List<Double> errors = new ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            double error = history.get(i).doubleValue() - history.get(i - 1).doubleValue();
            errors.add(error);
        }

        for (int i = 0; i < q && i < errors.size(); i++) {
            prediction += maCoefficients[i + 1] * errors.get(errors.size() - 1 - i);
        }

        return prediction;
    }

    // ✅ Метод для вычисления первой разности (I)
    public static List<BigDecimal> difference(List<BigDecimal> data) {
        List<BigDecimal> diff = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            diff.add(data.get(i).subtract(data.get(i - 1)));
        }
        return diff;
    }

    // ✅ Метод для восстановления значений после разности
    public static List<BigDecimal> restore(List<BigDecimal> differenced, BigDecimal firstValue) {
        List<BigDecimal> restored = new ArrayList<>();
        BigDecimal value = firstValue;
        restored.add(value);

        for (BigDecimal diff : differenced) {
            value = value.add(diff);
            restored.add(value);
        }
        return restored;
    }

    public static void main(String[] args) {
        // ✅ Исторические курсы USD
        List<BigDecimal> rates = List.of(
                new BigDecimal("87.45"),
                new BigDecimal("87.60"),
                new BigDecimal("87.80"),
                new BigDecimal("87.95"),
                new BigDecimal("88.12"),
                new BigDecimal("88.30"),
                new BigDecimal("88.45"),
                new BigDecimal("88.60"),
                new BigDecimal("88.75"),
                new BigDecimal("88.92"),
                new BigDecimal("89.10"),
                new BigDecimal("89.30")
        );

        // ✅ Определяем параметры ARIMA (p, d, q)
        int[] bestParams = findBestParams(rates);
        int p = bestParams[0];
        int d = bestParams[1];
        int q = bestParams[2];
        System.out.println("Оптимальные параметры ARIMA(p, d, q): " + p + ", " + d + ", " + q);

        // ✅ Прогнозируем курс на 5 дней вперед
        int daysToPredict = 5;
        double[] predictions = predict(rates, daysToPredict);

        // ✅ Выводим прогноз
        System.out.println("Прогнозируемые курсы USD на следующие " + daysToPredict + " дней:");
        for (int i = 0; i < daysToPredict; i++) {
            System.out.println("День " + (i + 1) + ": " + predictions[i]);
        }
    }

}