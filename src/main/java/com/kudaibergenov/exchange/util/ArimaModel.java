package com.kudaibergenov.exchange.util;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ArimaModel {

    // ✅ Основной метод прогнозирования
    public static BigDecimal[] predict(List<BigDecimal> data, int days) {
        if (data.size() < 10) {
            throw new IllegalArgumentException("Недостаточно данных для прогнозирования");
        }

        // ✅ Определяем параметры ARIMA
        int[] bestParams = findBestParams(data);
        int p = bestParams[0];
        int d = bestParams[1];
        int q = bestParams[2];
        System.out.println("📌 Оптимальные параметры ARIMA(p, d, q): " + p + ", " + d + ", " + q);

        // ✅ Дифференцируем данные
        List<BigDecimal> differenced = applyDifferencing(data, d);

        // ✅ Вычисляем коэффициенты AR и MA
        double[] arCoefficients = calculateAR(differenced, p);
        double[] maCoefficients = calculateMA(differenced, q);

        // ✅ Прогнозируем
        BigDecimal[] predictions = new BigDecimal[days];
        for (int i = 0; i < days; i++) {
            double predictedValue = predictNext(differenced, arCoefficients, maCoefficients);
            differenced.add(BigDecimal.valueOf(predictedValue));
            predictions[i] = BigDecimal.valueOf(predictedValue);
        }

        // ✅ Восстанавливаем данные после разностей
        return restorePredictions(predictions, data, d);
    }

    // ✅ Метод подбора параметров p, d, q
    private static int[] findBestParams(List<BigDecimal> data) {
        int bestP = 0, bestD = 0, bestQ = 0;
        double bestAIC = Double.MAX_VALUE;

        for (int d = 0; d <= 2; d++) {
            List<BigDecimal> diffData = applyDifferencing(data, d);
            for (int p = 0; p <= Math.min(3, diffData.size() - 2); p++) {
                for (int q = 0; q <= Math.min(3, diffData.size() - 2); q++) {
                    try {
                        double aic = calculateAIC(diffData, p, q);
                        if (aic < bestAIC) {
                            bestAIC = aic;
                            bestP = p;
                            bestD = d;
                            bestQ = q;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка при p=" + p + ", d=" + d + ", q=" + q + ": " + e.getMessage());
                    }
                }
            }
        }
        return new int[]{bestP, bestD, bestQ};
    }

    // ✅ Вычисление AIC (Akaike Information Criterion)
    private static double calculateAIC(List<BigDecimal> data, int p, int q) {
        double[] arCoefficients = calculateAR(data, p);
        double[] maCoefficients = calculateMA(data, q);

        double error = 0;
        for (int i = 0; i < data.size(); i++) {
            double predicted = predictNext(data, arCoefficients, maCoefficients);
            error += Math.pow(data.get(i).doubleValue() - predicted, 2);
        }
        double variance = error / data.size();
        return data.size() * Math.log(variance) + 2 * (p + q);
    }

    // ✅ Дифференцирование данных
    private static List<BigDecimal> applyDifferencing(List<BigDecimal> data, int d) {
        List<BigDecimal> result = new ArrayList<>(data);
        for (int i = 0; i < d; i++) {
            result = difference(result);
        }
        return result;
    }

    // ✅ Метод вычисления разностей (обычная и логарифмическая)
    private static List<BigDecimal> difference(List<BigDecimal> data) {
        List<BigDecimal> diff = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            diff.add(data.get(i).subtract(data.get(i - 1)));
        }
        return diff;
    }

    // ✅ Восстановление данных после разностей
    private static BigDecimal[] restorePredictions(BigDecimal[] predictions, List<BigDecimal> originalData, int d) {
        if (d == 0) return predictions;

        BigDecimal[] restored = new BigDecimal[predictions.length];
        BigDecimal lastValue = originalData.get(originalData.size() - 1);

        for (int i = 0; i < predictions.length; i++) {
            lastValue = lastValue.add(predictions[i]);
            restored[i] = lastValue;
        }
        return restored;
    }

    // ✅ Вычисление коэффициентов AR
    private static double[] calculateAR(List<BigDecimal> data, int p) {
        if (p == 0) return new double[]{0};

        int n = data.size();
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

    // ✅ Вычисление коэффициентов MA
    private static double[] calculateMA(List<BigDecimal> data, int q) {
        if (q == 0) return new double[]{0};

        List<Double> errors = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            errors.add(data.get(i).doubleValue() - data.get(i - 1).doubleValue());
        }

        if (errors.size() < q) {
            throw new IllegalArgumentException("Недостаточно данных для MA(q)");
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
    private static double predictNext(List<BigDecimal> history, double[] arCoefficients, double[] maCoefficients) {
        int p = arCoefficients.length - 1;
        int q = maCoefficients.length - 1;

        double prediction = arCoefficients[0];

        for (int i = 0; i < p; i++) {
            prediction += arCoefficients[i + 1] * history.get(history.size() - 1 - i).doubleValue();
        }

        List<Double> errors = history.stream()
                .map(BigDecimal::doubleValue)
                .toList();

        for (int i = 0; i < q && i < errors.size(); i++) {
            prediction += maCoefficients[i + 1] * errors.get(errors.size() - 1 - i);
        }

        return prediction;
    }
}
