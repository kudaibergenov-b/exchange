package com.kudaibergenov.exchange.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.FxKgController;
import com.kudaibergenov.exchange.service.FxKgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FxKgControllerPerformanceTest {

    private MockMvc mockMvc;

    @Mock
    private FxKgService fxKgService;

    @InjectMocks
    private FxKgController fxKgController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final long MAX_ALLOWED_TIME_MS = 200;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fxKgController).build();
    }

    @Test
    void performance_getAverageRates_shouldRespondQuickly() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("usd", 87.5);
        when(fxKgService.getAverageRates()).thenReturn(mockJson);

        long start = System.currentTimeMillis();

        mockMvc.perform(get("/fxkg/average"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - start;
        System.out.println("getAverageRates duration: " + duration + "ms");

        assertTrue(duration < MAX_ALLOWED_TIME_MS, "Время ответа превышает лимит");
    }

    @Test
    void performance_getBestRates_shouldRespondQuickly() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("eur", 96.3);
        when(fxKgService.getBestRates()).thenReturn(mockJson);

        long start = System.currentTimeMillis();

        mockMvc.perform(get("/fxkg/best"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - start;
        System.out.println("getBestRates duration: " + duration + "ms");

        assertTrue(duration < MAX_ALLOWED_TIME_MS, "Время ответа превышает лимит");
    }

    @Test
    void performance_getCurrentRates_shouldRespondQuickly() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("rub", 0.92);
        when(fxKgService.getCurrentRates()).thenReturn(mockJson);

        long start = System.currentTimeMillis();

        mockMvc.perform(get("/fxkg/current"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - start;
        System.out.println("getCurrentRates duration: " + duration + "ms");

        assertTrue(duration < MAX_ALLOWED_TIME_MS, "Время ответа превышает лимит");
    }

    @Test
    void performance_getCentralBankRates_shouldRespondQuickly() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("kzt", 0.19);
        when(fxKgService.getCentralBankRates()).thenReturn(mockJson);

        long start = System.currentTimeMillis();

        mockMvc.perform(get("/fxkg/central"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - start;
        System.out.println("getCentralBankRates duration: " + duration + "ms");

        assertTrue(duration < MAX_ALLOWED_TIME_MS, "Время ответа превышает лимит");
    }
}