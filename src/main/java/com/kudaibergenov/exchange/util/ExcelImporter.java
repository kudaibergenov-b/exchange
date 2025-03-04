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
import java.util.ArrayList;
import java.util.List;

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

                    Cell dateCell = row.getCell(0);
                    if (dateCell == null || !isValidDateCell(dateCell)) {
                        System.out.println("Пропущена строка " + rowIndex + " - невалидная дата");
                        continue;
                    }

                    LocalDate date = getCellAsDate(dateCell);

                    List<CurrencyRate> rates = new ArrayList<>();
                    rates.add(new CurrencyRate(date, "USD", getCellValueAsDouble(row.getCell(1))));
                    rates.add(new CurrencyRate(date, "EUR", getCellValueAsDouble(row.getCell(2))));
                    rates.add(new CurrencyRate(date, "RUB", getCellValueAsDouble(row.getCell(3))));
                    rates.add(new CurrencyRate(date, "KZT", getCellValueAsDouble(row.getCell(4))));

                    repository.saveAll(rates);
                }
            }

            System.out.println("Импорт завершен!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidDateCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return DateUtil.isCellDateFormatted(cell); // Проверяем, содержит ли ячейка дату
        } else if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            return text.matches("\\d{2}\\.\\d{2}\\.\\d{2}"); // Проверяем формат "dd.MM.yy"
        }
        return false;
    }

    private LocalDate getCellAsDate(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return LocalDate.parse(cell.getStringCellValue().trim(), DateTimeFormatter.ofPattern("dd.MM.yy"));
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : 0.0;
    }
}