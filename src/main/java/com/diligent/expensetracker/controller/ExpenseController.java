package com.diligent.expensetracker.controller;

import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.TotalResponse;
import com.diligent.expensetracker.model.Expense;
import com.diligent.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // POST /api/expenses - add a new expense
    @PostMapping
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody ExpenseRequest request) {
        Expense created = expenseService.addExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/expenses            - view all expenses
    // GET /api/expenses?category=X - filter by category
    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(expenseService.getAllExpenses(category));
    }

    // GET /api/expenses/total            - overall total
    // GET /api/expenses/total?category=X - total for one category
    @GetMapping("/total")
    public ResponseEntity<TotalResponse> getTotal(
            @RequestParam(required = false) String category) {
        var matching = expenseService.getAllExpenses(category);
        var total = expenseService.getTotal(category);
        return ResponseEntity.ok(new TotalResponse(category, total, matching.size()));
    }

    // DELETE /api/expenses/{id} - delete an expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
