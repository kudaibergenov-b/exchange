package com.kudaibergenov.exchange.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kudaibergenov.exchange.controller.ConverterController;
import com.kudaibergenov.exchange.service.CurrencyConverterService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConverterControllerSystemTest {

    private MockMvc mockMvc;

    @Mock
    private FxKgService fxKgService;

    @InjectMocks
    private CurrencyConverterService converterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ConverterController converterController = new ConverterController(converterService);
        mockMvc = MockMvcBuilders.standaloneSetup(converterController).build();
    }

    @Test
    void convert_shouldReturnCorrectResult() throws Exception {
        ObjectNode mockRates = objectMapper.createObjectNode();
        mockRates.put("usd", 87.5);
        mockRates.put("eur", 99.3);

        when(fxKgService.getCentralBankRates()).thenReturn(mockRates);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("from", "USD");
        requestBody.put("to", "EUR");
        requestBody.put("amount", 100.0);

        mockMvc.perform(post("/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.from", org.hamcrest.Matchers.is("USD")))
                .andExpect(jsonPath("$.data.to", org.hamcrest.Matchers.is("EUR")))
                .andExpect(jsonPath("$.data.amount", org.hamcrest.Matchers.is(100.0)))
                .andExpect(jsonPath("$.data.convertedAmount").isNumber());
    }
}
