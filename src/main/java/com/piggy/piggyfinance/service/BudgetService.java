package com.piggy.piggyfinance.service;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.model.requests.CreateBudgetRequest;
import com.piggy.piggyfinance.model.requests.UpdateBudgetRequest;
import com.piggy.piggyfinance.model.responses.BudgetOverviewResponse;
import com.piggy.piggyfinance.model.responses.BudgetResponse;

import java.util.List;
import java.util.UUID;

public interface BudgetService {

    BudgetResponse create(CreateBudgetRequest request);

    List<BudgetResponse> listByUser();

    BudgetResponse update(UUID budgetId, UpdateBudgetRequest request);

    void delete(UUID budgetId);

    BudgetOverviewResponse getOverview();

    String checkBudgetWarning(UUID userId, CategoryType category);
}