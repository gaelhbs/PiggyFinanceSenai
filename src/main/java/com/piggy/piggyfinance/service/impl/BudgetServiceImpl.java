package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.exceptions.BusinessException;
import com.piggy.piggyfinance.model.Budget;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.responses.BudgetOverviewItem;
import com.piggy.piggyfinance.model.responses.BudgetOverviewResponse;
import com.piggy.piggyfinance.model.responses.BudgetResponse;
import com.piggy.piggyfinance.model.requests.CreateBudgetRequest;
import com.piggy.piggyfinance.model.requests.UpdateBudgetRequest;
import com.piggy.piggyfinance.repository.BudgetRepository;
import com.piggy.piggyfinance.repository.TransactionRepository;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.service.BudgetService;
import com.piggy.piggyfinance.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public BudgetResponse create(CreateBudgetRequest request) {
        validateAmount(request.amount());

        UUID userId = SecurityUtils.getAuthenticatedUserId();

        if (budgetRepository.existsByUserIdAndCategory(userId, request.category())) {
            throw new BusinessException("Budget already exists for category " + request.category());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = Budget.builder()
                .user(user)
                .category(request.category())
                .amount(request.amount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        budget = budgetRepository.save(budget);

        return toResponse(budget);
    }

    @Override
    public List<BudgetResponse> listByUser() {
        UUID userId = SecurityUtils.getAuthenticatedUserId();

        return budgetRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BudgetResponse update(UUID budgetId, UpdateBudgetRequest request) {
        validateAmount(request.amount());

        UUID userId = SecurityUtils.getAuthenticatedUserId();

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BusinessException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new BusinessException("Budget not found");
        }

        budget.setAmount(request.amount());
        budget.setUpdatedAt(LocalDateTime.now());

        budget = budgetRepository.save(budget);

        return toResponse(budget);
    }

    @Override
    public void delete(UUID budgetId) {
        UUID userId = SecurityUtils.getAuthenticatedUserId();

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BusinessException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new BusinessException("Budget not found");
        }

        budgetRepository.delete(budget);
    }

    @Override
    public BudgetOverviewResponse getOverview() {
        UUID userId = SecurityUtils.getAuthenticatedUserId();

        List<Budget> budgets = budgetRepository.findByUserId(userId);

        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);

        List<BudgetOverviewItem> items = new ArrayList<>();
        BigDecimal totalBudgeted = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository.sumByUserAndCategoryAndPeriod(
                    userId, budget.getCategory(), monthStart, monthEnd
            );

            if (spent == null) {
                spent = BigDecimal.ZERO;
            }

            BigDecimal remaining = budget.getAmount().subtract(spent);
            boolean exceeded = spent.compareTo(budget.getAmount()) > 0;

            items.add(new BudgetOverviewItem(
                    budget.getCategory(),
                    budget.getAmount(),
                    spent,
                    remaining,
                    exceeded
            ));

            totalBudgeted = totalBudgeted.add(budget.getAmount());
            totalSpent = totalSpent.add(spent);
        }

        return new BudgetOverviewResponse(items, totalBudgeted, totalSpent);
    }

    @Override
    public String checkBudgetWarning(UUID userId, CategoryType category) {
        if (category == null) {
            return null;
        }

        return budgetRepository.findByUserIdAndCategory(userId, category)
                .map(budget -> {
                    LocalDate now = LocalDate.now();
                    LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
                    LocalDateTime monthEnd = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);

                    BigDecimal spent = transactionRepository.sumByUserAndCategoryAndPeriod(
                            userId, category, monthStart, monthEnd
                    );

                    if (spent == null) {
                        spent = BigDecimal.ZERO;
                    }

                    if (spent.compareTo(budget.getAmount()) > 0) {
                        return String.format("Voce ultrapassou o orcamento de %s: R$%s/R$%s",
                                category, spent.toPlainString(), budget.getAmount().toPlainString());
                    }

                    return null;
                })
                .orElse(null);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Budget amount must be greater than zero");
        }
    }

    private BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(budget.getId(), budget.getCategory(), budget.getAmount());
    }
}