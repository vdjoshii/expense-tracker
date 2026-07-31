package com.diligent.expensetracker.service;

import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.exception.ExpenseNotFoundException;
import com.diligent.expensetracker.model.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory expense store. Data does not persist across application restarts,
 * which is acceptable per the assignment spec ("in memory or a local JSON file").
 */
@Service
public class ExpenseService {

    private final Map<String, Expense> expenses = new ConcurrentHashMap<>();

    public Expense addExpense(ExpenseRequest request) {
        String id = UUID.randomUUID().toString();
        Expense expense = new Expense(
                id,
                request.getTitle(),
                request.getAmount(),
                normalizeCategory(request.getCategory()),
                request.getDate()
        );
        expenses.put(id, expense);
        return expense;
    }

    public List<Expense> getAllExpenses(String category) {
        Collection<Expense> all = expenses.values();
        if (category == null || category.isBlank()) {
            return all.stream()
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .collect(Collectors.toList());
        }
        String normalized = normalizeCategory(category);
        return all.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(normalized))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
    }

    public BigDecimal getTotal(String category) {
        return getAllExpenses(category).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void deleteExpense(String id) {
        if (!expenses.containsKey(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenses.remove(id);
    }

    private String normalizeCategory(String category) {
        return category == null ? null : category.trim();
    }
}
