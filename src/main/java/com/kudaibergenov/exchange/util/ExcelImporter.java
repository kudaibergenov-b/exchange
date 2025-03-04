package com.kudaibergenov.exchange.util;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class ExcelImporter {

    private final CurrencyRateRepository repository;

    public ExcelImporter(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    public void importData(String filePath) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new HSSFWorkbook(fis)) {

            for (Sheet sheet : workbook) {
                System.out.println("Обрабатываем лист: " + sheet.getSheetName());

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    //  Проверяем, является ли ячейка датой
                    Cell dateCell = row.getCell(0);
                    if (dateCell == null || !isValidDateCell(dateCell)) {
                        System.out.println("Пропущена строка " + rowIndex + " - невалидная дата");
                        continue;
                    }

                    //  Преобразуем дату
                    LocalDate date = getCellAsDate(dateCell);

                    //  Читаем и сохраняем курсы валют (с проверкой дубликатов)
                    saveOrUpdateRate(date, "USD", row.getCell(1));
                    saveOrUpdateRate(date, "EUR", row.getCell(2));
                    saveOrUpdateRate(date, "RUB", row.getCell(3));
                    saveOrUpdateRate(date, "KZT", row.getCell(4));
                }
            }

            System.out.println("Импорт завершен!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  Метод для обновления существующей записи или вставки новой
    private void saveOrUpdateRate(LocalDate date, String currencyCode, Cell cell) {
        if (cell == null) return; // Пропускаем пустые значения

        double rate = getCellValueAsDouble(cell);
        if (rate == 0.0) return; // Пропускаем нулевые значения

        Optional<CurrencyRate> existingRate = repository.findByDateAndCurrencyCode(date, currencyCode);

        if (existingRate.isPresent()) {
            CurrencyRate updatedRate = existingRate.get();
            updatedRate.setRate(rate);
            repository.save(updatedRate); // Обновляем существующую запись
            System.out.println("Обновлен курс: " + updatedRate);
        } else {
            CurrencyRate newRate = new CurrencyRate(date, currencyCode, rate);
            repository.save(newRate); // Вставляем новую запись
            System.out.println("Добавлен новый курс: " + newRate);
        }
    }

    //  Проверяем, является ли ячейка датой
    private boolean isValidDateCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return DateUtil.isCellDateFormatted(cell);
        } else if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            return text.matches("\\d{2}\\.\\d{2}\\.\\d{2}");
        }
        return false;
    }

    //  Преобразуем ячейку в `LocalDate`
    private LocalDate getCellAsDate(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return LocalDate.parse(cell.getStringCellValue().trim(), DateTimeFormatter.ofPattern("dd.MM.yy"));
        }
    }

    //  Безопасное получение `double`
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : 0.0;
    }
}