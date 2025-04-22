package com.kudaibergenov.exchange.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.CurrencyController;
import com.kudaibergenov.exchange.model.CurrencyRate;
import com.kudaibergenov.exchange.service.CurrencyDataService;
import com.kudaibergenov.exchange.service.FxKgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CurrencyControllerSystemTest {

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

    @Test
    void getAvailableCurrencies_shouldReturnCurrencyList() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("usd", "87.5");
        mockJson.put("eur", "99.3");
        mockJson.put("id", 1);
        mockJson.put("created_at", "2024-01-01T00:00:00");

        when(fxKgService.getCentralBankRates()).thenReturn(mockJson);

        mockMvc.perform(get("/currency/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasItems("USD", "EUR")))
                .andExpect(jsonPath("$.data", not(hasItem("ID"))));
    }

    @Test
    void getRatesByDate_shouldReturnRatesForGivenDate() throws Exception {
        LocalDate date = LocalDate.of(2024, 4, 10);

        CurrencyRate rate1 = new CurrencyRate();
        rate1.setDate(date);
        rate1.setCurrencyCode("USD");
        rate1.setRate(new BigDecimal("87.50"));

        CurrencyRate rate2 = new CurrencyRate();
        rate2.setDate(date);
        rate2.setCurrencyCode("EUR");
        rate2.setRate(new BigDecimal("99.30"));

        when(currencyDataService.getRatesByDate(date)).thenReturn(List.of(rate1, rate2));

        mockMvc.perform(get("/currency/rates/{date}", date))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].currencyCode", is("USD")))
                .andExpect(jsonPath("$.data[1].currencyCode", is("EUR")));
    }

    @Test
    void getHistory_shouldReturnRatesForDateRange() throws Exception {
        LocalDate start = LocalDate.of(2024, 4, 1);
        LocalDate end = LocalDate.of(2024, 4, 3);

        CurrencyRate rate1 = new CurrencyRate();
        rate1.setDate(start);
        rate1.setCurrencyCode("USD");
        rate1.setRate(new BigDecimal("87.0"));

        CurrencyRate rate2 = new CurrencyRate();
        rate2.setDate(start.plusDays(1));
        rate2.setCurrencyCode("USD");
        rate2.setRate(new BigDecimal("87.2"));

        when(currencyDataService.getHistory(start, end)).thenReturn(List.of(rate1, rate2));

        mockMvc.perform(get("/currency/history")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rate", is(87.0)))
                .andExpect(jsonPath("$.data[1].rate", is(87.2)));
    }
}