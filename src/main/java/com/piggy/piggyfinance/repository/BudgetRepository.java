package com.piggy.piggyfinance.repository;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserId(UUID userId);

    Optional<Budget> findByUserIdAndCategory(UUID userId, CategoryType category);

    boolean existsByUserIdAndCategory(UUID userId, CategoryType category);
}
