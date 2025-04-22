package com.kudaibergenov.exchange.stress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.FxKgController;
import com.kudaibergenov.exchange.service.FxKgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FxKgControllerStressTest {

    private MockMvc mockMvc;

    @Mock
    private FxKgService fxKgService;

    @InjectMocks
    private FxKgController fxKgController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fxKgController).build();
    }

    @RepeatedTest(100) // 💥 Выполняем 100 раз подряд
    void stressTest_getAverageRates() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("usd", 87.5);
        when(fxKgService.getAverageRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @RepeatedTest(100)
    void stressTest_getBestRates() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("eur", 96.3);
        when(fxKgService.getBestRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/best"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @RepeatedTest(100)
    void stressTest_getCurrentRates() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("rub", 0.92);
        when(fxKgService.getCurrentRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/current"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @RepeatedTest(100)
    void stressTest_getCentralBankRates() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("kzt", 0.19);
        when(fxKgService.getCentralBankRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/central"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}