package com.kudaibergenov.exchange.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.CurrencyController;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.StopWatch;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class CurrencyControllerPerformanceTest {

    private MockMvc mockMvc;

    @Mock
    private CurrencyDataService currencyDataService;

    @Mock
    private FxKgService fxKgService;

    @InjectMocks
    private CurrencyController currencyController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
    }

    @RepeatedTest(10)
    void getAvailableCurrencies_performanceTest() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("usd", "87.5");
        mockJson.put("eur", "99.3");
        mockJson.put("kzt", "0.19");

        when(fxKgService.getCentralBankRates()).thenReturn(mockJson);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/currency/list")).andReturn();

        stopWatch.stop();
        System.out.println("Execution time for /currency/list: " + stopWatch.getTotalTimeMillis() + " ms");
    }

    @RepeatedTest(10)
    void getRatesByDate_performanceTest() throws Exception {
        LocalDate date = LocalDate.of(2024, 4, 10);

        CurrencyRate rate1 = new CurrencyRate(date, "USD", new BigDecimal("87.50"));
        CurrencyRate rate2 = new CurrencyRate(date, "EUR", new BigDecimal("99.30"));

        when(currencyDataService.getRatesByDate(date)).thenReturn(List.of(rate1, rate2));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/currency/rates/{date}", date)).andReturn();

        stopWatch.stop();
        System.out.println("Execution time for /currency/rates: " + stopWatch.getTotalTimeMillis() + " ms");
    }

    @RepeatedTest(10)
    void getHistory_performanceTest() throws Exception {
        LocalDate start = LocalDate.of(2024, 4, 1);
        LocalDate end = LocalDate.of(2024, 4, 3);

        CurrencyRate rate1 = new CurrencyRate(start, "USD", new BigDecimal("87.0"));
        CurrencyRate rate2 = new CurrencyRate(start.plusDays(1), "USD", new BigDecimal("87.2"));

        when(currencyDataService.getHistory(start, end)).thenReturn(List.of(rate1, rate2));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/currency/history")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andReturn();

        stopWatch.stop();
        System.out.println("Execution time for /currency/history: " + stopWatch.getTotalTimeMillis() + " ms");
    }
}