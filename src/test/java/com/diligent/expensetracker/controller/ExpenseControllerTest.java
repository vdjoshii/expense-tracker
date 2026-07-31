package com.diligent.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Map<String, Object> expensePayload(String title, String amount, String category, String date) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("amount", amount);
        body.put("category", category);
        body.put("date", date);
        return body;
    }

    @Test
    void addExpense_returns201AndCreatedExpense() throws Exception {
        var payload = expensePayload("Coffee", "150.00", "Food", "2026-07-01");

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Coffee"))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void addExpense_returns400WhenAmountIsMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Coffee");
        payload.put("category", "Food");
        payload.put("date", "2026-07-01");

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void addExpense_returns400WhenAmountIsZeroOrNegative() throws Exception {
        var payload = expensePayload("Refund", "-5", "Food", "2026-07-01");

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getExpenses_filtersByCategory() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        expensePayload("Groceries", "500", "Food", "2026-07-05"))));
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        expensePayload("Metro card", "100", "Transport", "2026-07-05"))));

        mockMvc.perform(get("/api/expenses").param("category", "Transport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.category == 'Food')]").doesNotExist());
    }

    @Test
    void getTotal_returnsSumAcrossAllExpenses() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        expensePayload("Item A", "100", "Misc", "2026-07-05"))));
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        expensePayload("Item B", "50", "Misc", "2026-07-05"))));

        mockMvc.perform(get("/api/expenses/total").param("category", "Misc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Misc"));
    }

    @Test
    void deleteExpense_returns404WhenIdUnknown() throws Exception {
        mockMvc.perform(delete("/api/expenses/unknown-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_returns204WhenSuccessful() throws Exception {
        String response = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                expensePayload("Temp", "10", "Misc", "2026-07-05"))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/api/expenses/" + id))
                .andExpect(status().isNoContent());
    }
}
