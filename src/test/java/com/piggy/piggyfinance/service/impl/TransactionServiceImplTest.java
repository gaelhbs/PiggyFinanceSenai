package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.exceptions.BusinessException;
import com.piggy.piggyfinance.factory.CreateTransactionRequestFactory;
import com.piggy.piggyfinance.factory.TransactionFixture;
import com.piggy.piggyfinance.factory.TransactionSummaryItemFactory;
import com.piggy.piggyfinance.factory.UserFactory;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.dto.TransactionSummaryItem;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.responses.TransactionSummaryResponse;
import com.piggy.piggyfinance.factory.TransactionFactory;
import com.piggy.piggyfinance.repository.TransactionRepository;
import com.piggy.piggyfinance.repository.UserRepository;
import com.piggy.piggyfinance.repository.specifications.TransactionSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
    @Test
    void whenCreateTransaction_givenValidIncomeRequest_shouldSaveAndReturnTransaction() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        CreateTransactionRequest request = CreateTransactionRequestFactory.createIncome();
        Transaction expectedTransaction = TransactionFixture.create(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        try (MockedStatic<TransactionFactory> factoryMock = mockStatic(TransactionFactory.class)) {
            factoryMock.when(() -> TransactionFactory.create(request, TransactionSourceEnum.APP, user))
                    .thenReturn(expectedTransaction);
            when(transactionRepository.save(expectedTransaction)).thenReturn(expectedTransaction);

            Transaction result = transactionService.createTransaction(request, TransactionSourceEnum.APP);

            assertEquals(expectedTransaction, result);
            assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(result.getAmount()).isEqualByComparingTo(CreateTransactionRequestFactory.DEFAULT_AMOUNT);
            assertThat(result.getSource()).isEqualTo(TransactionSourceEnum.APP);
        }
    }

    @Test
    void whenCreateTransaction_givenValidExpenseRequest_shouldSaveAndReturnTransaction() {

        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        CreateTransactionRequest request = CreateTransactionRequestFactory.createExpense();
        Transaction expectedTransaction = TransactionFixture.createBuilder(user)
                .type(TransactionType.EXPENSE)
                .category(CategoryType.FOOD)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        try (MockedStatic<TransactionFactory> factoryMock = mockStatic(TransactionFactory.class)) {
            factoryMock.when(() -> TransactionFactory.create(request, TransactionSourceEnum.APP, user))
                    .thenReturn(expectedTransaction);
            when(transactionRepository.save(expectedTransaction)).thenReturn(expectedTransaction);

            Transaction result = transactionService.createTransaction(request, TransactionSourceEnum.APP);

            assertEquals(expectedTransaction, result);
            assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
            assertThat(result.getCategory()).isEqualTo(CategoryType.FOOD);
            assertThat(result.getUser()).isEqualTo(user);
        }
    }

    @Test
    void whenCreateTransaction_givenZeroAmount_shouldThrowBusinessException() {
        CreateTransactionRequest request = CreateTransactionRequestFactory.builder()
                .amount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> transactionService.createTransaction(request, TransactionSourceEnum.APP))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void whenCreateTransaction_givenNegativeAmount_shouldThrowBusinessException() {
        CreateTransactionRequest request = CreateTransactionRequestFactory.builder()
                .amount(new BigDecimal("-1.00"))
                .build();

        assertThatThrownBy(() -> transactionService.createTransaction(request, TransactionSourceEnum.APP))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void whenCreateTransaction_givenExpenseWithoutCategory_shouldThrowBusinessException() {
        CreateTransactionRequest request = CreateTransactionRequestFactory.builder()
                .type(TransactionType.EXPENSE)
                .category(null)
                .build();

        assertThatThrownBy(() -> transactionService.createTransaction(request, TransactionSourceEnum.APP))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void whenCreateTransaction_givenIncomeWithCategory_shouldThrowBusinessException() {
        CreateTransactionRequest request = CreateTransactionRequestFactory.builder()
                .type(TransactionType.INCOME)
                .category(CategoryType.FOOD)
                .build();

        assertThatThrownBy(() -> transactionService.createTransaction(request, TransactionSourceEnum.APP))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void whenCreateTransaction_givenUserNotFound_shouldThrowRuntimeException() {
        UUID userId = UserFactory.DEFAULT_ID;
        mockAuthentication(userId);
        CreateTransactionRequest request = CreateTransactionRequestFactory.createIncome();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request, TransactionSourceEnum.APP))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void whenListTransactions_givenValidFilter_shouldReturnPagedTransactions() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        TransactionFilter filter = new TransactionFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Transaction transaction = TransactionFixture.create(user);
        Page<Transaction> expectedPage = new PageImpl<>(List.of(transaction));
        Specification<Transaction> fixedSpec = (root, query, cb) -> cb.conjunction();

        try (MockedStatic<TransactionSpecification> specMock = mockStatic(TransactionSpecification.class)) {
            specMock.when(() -> TransactionSpecification.byFilter(filter, user.getId()))
                    .thenReturn(fixedSpec);
            when(transactionRepository.findAll(fixedSpec, pageable)).thenReturn(expectedPage);

            Page<Transaction> result = transactionService.listTransactions(filter, pageable);

            assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
            assertEquals(expectedPage.getContent(), result.getContent());
            assertThat(result.getContent()).containsExactly(transaction);
        }
    }


    @Test
    void whenGetSummary_givenBothIncomeAndExpense_shouldCalculateCorrectBalance() {
        UUID userId = UserFactory.DEFAULT_ID;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        LocalDateTime expectedStart = startDate.atStartOfDay();
        LocalDateTime expectedEnd = endDate.atTime(LocalTime.MAX);
        BigDecimal expectedBalance = TransactionSummaryItemFactory.DEFAULT_INCOME
                .subtract(TransactionSummaryItemFactory.DEFAULT_EXPENSE);
        List<TransactionSummaryItem> items = List.of(
                TransactionSummaryItemFactory.createIncome(),
                TransactionSummaryItemFactory.createExpense()
        );
        when(transactionRepository.getSummary(userId, expectedStart, expectedEnd)).thenReturn(items);

        TransactionSummaryResponse result = transactionService.getSummary(userId, startDate, endDate);

        assertThat(result.income()).isEqualByComparingTo(TransactionSummaryItemFactory.DEFAULT_INCOME);
        assertThat(result.expense()).isEqualByComparingTo(TransactionSummaryItemFactory.DEFAULT_EXPENSE);
        assertThat(result.balance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    void whenGetSummary_givenOnlyIncome_shouldReturnZeroExpenseAndPositiveBalance() {
        UUID userId = UserFactory.DEFAULT_ID;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        LocalDateTime expectedStart = startDate.atStartOfDay();
        LocalDateTime expectedEnd = endDate.atTime(LocalTime.MAX);
        when(transactionRepository.getSummary(userId, expectedStart, expectedEnd))
                .thenReturn(List.of(TransactionSummaryItemFactory.createIncome()));

        TransactionSummaryResponse result = transactionService.getSummary(userId, startDate, endDate);

        assertThat(result.income()).isEqualByComparingTo(TransactionSummaryItemFactory.DEFAULT_INCOME);
        assertThat(result.expense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.balance()).isEqualByComparingTo(TransactionSummaryItemFactory.DEFAULT_INCOME);
    }

    @Test
    void whenGetSummary_givenOnlyExpense_shouldReturnZeroIncomeAndNegativeBalance() {
        UUID userId = UserFactory.DEFAULT_ID;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        LocalDateTime expectedStart = startDate.atStartOfDay();
        LocalDateTime expectedEnd = endDate.atTime(LocalTime.MAX);
        BigDecimal expectedBalance = BigDecimal.ZERO.subtract(TransactionSummaryItemFactory.DEFAULT_EXPENSE);
        when(transactionRepository.getSummary(userId, expectedStart, expectedEnd))
                .thenReturn(List.of(TransactionSummaryItemFactory.createExpense()));

        TransactionSummaryResponse result = transactionService.getSummary(userId, startDate, endDate);

        assertThat(result.income()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.expense()).isEqualByComparingTo(TransactionSummaryItemFactory.DEFAULT_EXPENSE);
        assertThat(result.balance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    void whenGetSummary_givenEmptyResult_shouldReturnAllZeros() {
        UUID userId = UserFactory.DEFAULT_ID;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        LocalDateTime expectedStart = startDate.atStartOfDay();
        LocalDateTime expectedEnd = endDate.atTime(LocalTime.MAX);
        when(transactionRepository.getSummary(userId, expectedStart, expectedEnd))
                .thenReturn(Collections.emptyList());

        TransactionSummaryResponse result = transactionService.getSummary(userId, startDate, endDate);

        assertEquals(BigDecimal.ZERO, result.income());
        assertEquals(BigDecimal.ZERO, result.expense());
        assertEquals(BigDecimal.ZERO, result.balance());
    }


    private void mockAuthentication(UUID userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
