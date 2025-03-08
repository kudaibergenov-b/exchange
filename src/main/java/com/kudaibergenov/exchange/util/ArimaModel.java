package com.kudaibergenov.exchange.util;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

public class ArimaModel {

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

    // ✅ Скользящее среднее (MA)
    public static double[] calculateMA(List<BigDecimal> data, int q) {
        List<Double> errors = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            double error = data.get(i).doubleValue() - data.get(i - 1).doubleValue();
            errors.add(error);
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
        List<BigDecimal> rates = List.of(
                new BigDecimal("87.45"),
                new BigDecimal("87.60"),
                new BigDecimal("87.80"),
                new BigDecimal("87.95"),
                new BigDecimal("88.12"),
                new BigDecimal("88.30"),
                new BigDecimal("88.45"),
                new BigDecimal("88.60")
        );

        // ✅ 1. Применяем разности (d=1)
        List<BigDecimal> differenced = difference(rates);
        System.out.println("Первая разность (d=1): " + differenced);

        // ✅ 2. Восстанавливаем данные
        List<BigDecimal> restored = restore(differenced, rates.get(0));
        System.out.println("Восстановленные данные: " + restored);
    }
}