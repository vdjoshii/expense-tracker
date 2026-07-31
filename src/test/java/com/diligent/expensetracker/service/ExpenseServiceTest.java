package com.diligent.expensetracker.service;

import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.exception.ExpenseNotFoundException;
import com.diligent.expensetracker.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseServiceTest {

    private ExpenseService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseService();
    }

    private ExpenseRequest request(String title, String amount, String category, LocalDate date) {
        ExpenseRequest req = new ExpenseRequest();
        req.setTitle(title);
        req.setAmount(new BigDecimal(amount));
        req.setCategory(category);
        req.setDate(date);
        return req;
    }

    @Test
    void addExpense_assignsGeneratedId() {
        Expense created = service.addExpense(
                request("Coffee", "150.00", "Food", LocalDate.of(2026, 7, 1)));

        assertNotNull(created.getId());
        assertEquals("Coffee", created.getTitle());
        assertEquals(0, new BigDecimal("150.00").compareTo(created.getAmount()));
    }

    @Test
    void getAllExpenses_returnsEverythingWhenNoCategoryGiven() {
        service.addExpense(request("Coffee", "150", "Food", LocalDate.of(2026, 7, 1)));
        service.addExpense(request("Bus ticket", "40", "Transport", LocalDate.of(2026, 7, 2)));

        List<Expense> all = service.getAllExpenses(null);

        assertEquals(2, all.size());
    }

    @Test
    void getAllExpenses_filtersByCategoryCaseInsensitively() {
        service.addExpense(request("Coffee", "150", "Food", LocalDate.of(2026, 7, 1)));
        service.addExpense(request("Bus ticket", "40", "Transport", LocalDate.of(2026, 7, 2)));

        List<Expense> food = service.getAllExpenses("food");

        assertEquals(1, food.size());
        assertEquals("Coffee", food.get(0).getTitle());
    }

    @Test
    void getAllExpenses_isSortedByDateDescending() {
        service.addExpense(request("Old", "10", "Food", LocalDate.of(2026, 1, 1)));
        service.addExpense(request("New", "10", "Food", LocalDate.of(2026, 7, 1)));

        List<Expense> all = service.getAllExpenses(null);

        assertEquals("New", all.get(0).getTitle());
        assertEquals("Old", all.get(1).getTitle());
    }

    @Test
    void getTotal_sumsAllExpensesWhenNoCategoryGiven() {
        service.addExpense(request("Coffee", "150.50", "Food", LocalDate.of(2026, 7, 1)));
        service.addExpense(request("Bus ticket", "40.00", "Transport", LocalDate.of(2026, 7, 2)));

        BigDecimal total = service.getTotal(null);

        assertEquals(0, new BigDecimal("190.50").compareTo(total));
    }

    @Test
    void getTotal_sumsOnlyMatchingCategory() {
        service.addExpense(request("Coffee", "150", "Food", LocalDate.of(2026, 7, 1)));
        service.addExpense(request("Lunch", "250", "Food", LocalDate.of(2026, 7, 2)));
        service.addExpense(request("Bus ticket", "40", "Transport", LocalDate.of(2026, 7, 2)));

        BigDecimal foodTotal = service.getTotal("Food");

        assertEquals(0, new BigDecimal("400").compareTo(foodTotal));
    }

    @Test
    void getTotal_returnsZeroWhenNoExpenses() {
        assertEquals(0, BigDecimal.ZERO.compareTo(service.getTotal(null)));
    }

    @Test
    void deleteExpense_removesItFromStore() {
        Expense created = service.addExpense(
                request("Coffee", "150", "Food", LocalDate.of(2026, 7, 1)));

        service.deleteExpense(created.getId());

        assertTrue(service.getAllExpenses(null).isEmpty());
    }

    @Test
    void deleteExpense_throwsWhenIdDoesNotExist() {
        assertThrows(ExpenseNotFoundException.class,
                () -> service.deleteExpense("does-not-exist"));
    }
}
