package com.kudaibergenov.exchange.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TimeSeriesProcessor {

    private static final int SCALE = 10; // Количество знаков после запятой

    // ✅ Метод для дифференцирования (убираем тренд)
    public static List<BigDecimal> difference(List<BigDecimal> data) {
        List<BigDecimal> diff = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            BigDecimal diffValue = data.get(i).subtract(data.get(i - 1));
            diff.add(diffValue.setScale(SCALE, RoundingMode.HALF_UP)); // Округляем
        }
        return diff;
    }

    // ✅ Метод для восстановления исходных значений
    public static List<BigDecimal> restore(List<BigDecimal> differenced, BigDecimal firstValue) {
        List<BigDecimal> restored = new ArrayList<>();
        BigDecimal value = firstValue;
        restored.add(value);

        for (BigDecimal diff : differenced) {
            value = value.add(diff).setScale(SCALE, RoundingMode.HALF_UP); // Округляем
            restored.add(value);
        }
        return restored;
    }

    // ✅ Тестируем
    public static void main(String[] args) {
        List<BigDecimal> rates = List.of(
                new BigDecimal("87.45"),
                new BigDecimal("87.60"),
                new BigDecimal("87.80"),
                new BigDecimal("87.95"),
                new BigDecimal("88.12")
        );

        // ✅ 1. Дифференцируем данные
        List<BigDecimal> differenced = difference(rates);
        System.out.println("Дифференцированные данные: " + differenced);

        // ✅ 2. Восстанавливаем данные обратно
        List<BigDecimal> restored = restore(differenced, rates.get(0));
        System.out.println("Восстановленные данные: " + restored);
    }
}