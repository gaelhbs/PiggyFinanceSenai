package com.piggy.piggyfinance.controller;

import com.piggy.piggyfinance.model.requests.CreateBudgetRequest;
import com.piggy.piggyfinance.model.requests.UpdateBudgetRequest;
import com.piggy.piggyfinance.model.responses.BudgetOverviewResponse;
import com.piggy.piggyfinance.model.responses.BudgetResponse;
import com.piggy.piggyfinance.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@RequestBody @Valid CreateBudgetRequest request) {
        return budgetService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BudgetResponse> list() {
        return budgetService.listByUser();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BudgetResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateBudgetRequest request) {
        return budgetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        budgetService.delete(id);
    }

    @GetMapping("/overview")
    @ResponseStatus(HttpStatus.OK)
    public BudgetOverviewResponse overview() {
        return budgetService.getOverview();
    }
}