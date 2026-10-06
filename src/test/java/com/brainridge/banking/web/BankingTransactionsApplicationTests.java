package com.brainridge.banking.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankingTransactionsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exposesCreateTransferAndHistoryFlow() throws Exception {
        UUID sourceId = createAccount(new BigDecimal("100.00"));
        UUID destinationId = createAccount(new BigDecimal("25.00"));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": "%s",
                                  "toAccountId": "%s",
                                  "amount": 40.00
                                }
                                """.formatted(sourceId, destinationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transferId").exists())
                .andExpect(jsonPath("$.fromAccountBalance").value(60.00))
                .andExpect(jsonPath("$.toAccountBalance").value(65.00));

        mockMvc.perform(get("/api/v1/accounts/{accountId}/transactions", sourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[1].type").value("INITIAL_DEPOSIT"));
    }

    @Test
    void returnsBadRequestForInvalidAccountInput() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"initialBalance\": -1.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "initialBalance: initialBalance must be zero or greater"));
    }

    @Test
    void returnsUnprocessableEntityForInsufficientFunds() throws Exception {
        UUID sourceId = createAccount(new BigDecimal("10.00"));
        UUID destinationId = createAccount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": "%s",
                                  "toAccountId": "%s",
                                  "amount": 10.01
                                }
                                """.formatted(sourceId, destinationId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    void returnsNotFoundForUnknownAccount() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{accountId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private UUID createAccount(BigDecimal initialBalance) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"initialBalance\": " + initialBalance + "}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(response.get("id").asText());
    }
}
