package com.kudaibergenov.exchange.util;

import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.repository.CurrencyRateRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class ExcelImporter {

    private static final Logger logger = Logger.getLogger(ExcelImporter.class.getName());
    private final CurrencyRateRepository repository;

    public ExcelImporter(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    public void importData(String filePath) {
        Path path = Paths.get(filePath).toAbsolutePath();
        logger.info("Начинаем импорт данных из файла: " + path);

        try (FileInputStream fis = new FileInputStream(path.toFile());
             Workbook workbook = createWorkbook(path.toFile(), fis)) {

            for (Sheet sheet : workbook) {
                logger.info("Обрабатываем лист: " + sheet.getSheetName());

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    LocalDate date = parseDateCell(row.getCell(0));
                    if (date == null) {
                        logger.warning("Пропущена строка " + rowIndex + " - невалидная дата");
                        continue;
                    }

                    processCurrencyRate(date, "USD", row.getCell(1));
                    processCurrencyRate(date, "EUR", row.getCell(2));
                    processCurrencyRate(date, "RUB", row.getCell(3));
                    processCurrencyRate(date, "KZT", row.getCell(4));
                }
            }

            logger.info("Импорт завершен!");
        } catch (Exception e) {
            logger.severe("Ошибка импорта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Workbook createWorkbook(File file, FileInputStream fis) throws Exception {
        if (file.getName().toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else if (file.getName().toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("Неподдерживаемый формат файла: " + file.getName());
        }
    }

    private void processCurrencyRate(LocalDate date, String currencyCode, Cell cell) {
        BigDecimal rate = getCellValueAsBigDecimal(cell);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) return;

        Optional<CurrencyRate> existingRate = repository.findByDateAndCurrencyCode(date, currencyCode);

        CurrencyRate currencyRate = existingRate.orElse(new CurrencyRate(date, currencyCode, BigDecimal.ZERO));
        currencyRate.setRate(rate);
        repository.save(currencyRate);

        logger.info((existingRate.isPresent() ? "Обновлен" : "Добавлен") + " курс: " + currencyRate);
    }

    private LocalDate parseDateCell(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim(),
                        DateTimeFormatter.ofPattern("dd.MM.yy", Locale.getDefault()));
            } catch (Exception e) {
                logger.warning("Ошибка парсинга даты: " + cell.getStringCellValue());
                return null;
            }
        }
        return null;
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());

            case STRING:
                try {
                    // ✅ Убираем возможные запятые в числах
                    return new BigDecimal(cell.getStringCellValue().replace(",", ".").trim());
                } catch (NumberFormatException e) {
                    logger.warning("Ошибка парсинга курса: " + cell.getStringCellValue());
                }
        }
        return null;
    }
}
