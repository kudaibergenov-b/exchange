package com.kudaibergenov.exchange.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.FxKgController;
import com.kudaibergenov.exchange.service.FxKgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FxKgControllerSystemTest {

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

    @Test
    void getAverageRates_shouldReturnJson() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("usd", 87.5);

        when(fxKgService.getAverageRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.usd").value(87.5));
    }

    @Test
    void getBestRates_shouldReturnJson() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("eur", 96.3);

        when(fxKgService.getBestRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/best"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eur").value(96.3));
    }

    @Test
    void getCurrentRates_shouldReturnJson() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("rub", 0.92);

        when(fxKgService.getCurrentRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/current"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rub").value(0.92));
    }

    @Test
    void getCentralBankRates_shouldReturnJson() throws Exception {
        ObjectNode mockJson = objectMapper.createObjectNode();
        mockJson.put("kzt", 0.19);

        when(fxKgService.getCentralBankRates()).thenReturn(mockJson);

        mockMvc.perform(get("/fxkg/central"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.kzt").value(0.19));
    }
}