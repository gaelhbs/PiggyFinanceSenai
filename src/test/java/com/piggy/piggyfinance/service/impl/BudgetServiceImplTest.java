package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.exceptions.BusinessException;
import com.piggy.piggyfinance.factory.BudgetFixture;
import com.piggy.piggyfinance.factory.CreateBudgetRequestFactory;
import com.piggy.piggyfinance.factory.UserFactory;
import com.piggy.piggyfinance.model.Budget;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.requests.CreateBudgetRequest;
import com.piggy.piggyfinance.model.requests.UpdateBudgetRequest;
import com.piggy.piggyfinance.model.responses.BudgetOverviewResponse;
import com.piggy.piggyfinance.model.responses.BudgetResponse;
import com.piggy.piggyfinance.repository.BudgetRepository;
import com.piggy.piggyfinance.repository.TransactionRepository;
import com.piggy.piggyfinance.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Testa se o orçamento é criado e retornado com sucesso quando a requisição é válida.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreate_givenValidRequest_shouldSaveAndReturnBudget() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        CreateBudgetRequest request = CreateBudgetRequestFactory.create();

        when(budgetRepository.existsByUserIdAndCategory(user.getId(), CategoryType.FOOD)).thenReturn(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return b.toBuilder().id(UUID.randomUUID()).build();
        });

        BudgetResponse result = budgetService.create(request);

        assertThat(result.category()).isEqualTo(CategoryType.FOOD);
        assertThat(result.amount()).isEqualByComparingTo(CreateBudgetRequestFactory.DEFAULT_AMOUNT);
        verify(budgetRepository).save(any(Budget.class));
    }

    /**
     * Testa se lança exceção ao criar orçamento com valor zero.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreate_givenZeroAmount_shouldThrowBusinessException() {
        mockAuthentication(UserFactory.DEFAULT_ID);
        CreateBudgetRequest request = CreateBudgetRequestFactory.builder()
                .amount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> budgetService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Budget amount must be greater than zero");
    }

    /**
     * Testa se lança exceção ao criar orçamento com valor negativo.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreate_givenNegativeAmount_shouldThrowBusinessException() {
        mockAuthentication(UserFactory.DEFAULT_ID);
        CreateBudgetRequest request = CreateBudgetRequestFactory.builder()
                .amount(new BigDecimal("-100.00"))
                .build();

        assertThatThrownBy(() -> budgetService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Budget amount must be greater than zero");
    }

    /**
     * Testa se lança exceção ao criar orçamento com categoria duplicada.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCreate_givenDuplicateCategory_shouldThrowBusinessException() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        CreateBudgetRequest request = CreateBudgetRequestFactory.create();

        when(budgetRepository.existsByUserIdAndCategory(user.getId(), CategoryType.FOOD)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Budget already exists for category FOOD");
    }

    /**
     * Testa se o orçamento é atualizado e retornado com sucesso quando a requisição é válida.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenUpdate_givenValidRequest_shouldUpdateAndReturnBudget() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        Budget budget = BudgetFixture.create(user);
        UpdateBudgetRequest request = new UpdateBudgetRequest(new BigDecimal("700.00"));

        when(budgetRepository.findById(BudgetFixture.DEFAULT_ID)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResponse result = budgetService.update(BudgetFixture.DEFAULT_ID, request);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(result.category()).isEqualTo(CategoryType.FOOD);
    }

    /**
     * Testa se lança exceção ao atualizar orçamento de outro usuário.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenUpdate_givenBudgetFromAnotherUser_shouldThrowBusinessException() {
        User owner = UserFactory.createUserBuilder().id(UUID.randomUUID()).build();
        User otherUser = UserFactory.createUser();
        mockAuthentication(otherUser.getId());
        Budget budget = BudgetFixture.create(owner);
        UpdateBudgetRequest request = new UpdateBudgetRequest(new BigDecimal("700.00"));

        when(budgetRepository.findById(BudgetFixture.DEFAULT_ID)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.update(BudgetFixture.DEFAULT_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Budget not found");
    }

    /**
     * Testa se o orçamento é deletado com sucesso.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenDelete_givenValidBudget_shouldDeleteSuccessfully() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        Budget budget = BudgetFixture.create(user);

        when(budgetRepository.findById(BudgetFixture.DEFAULT_ID)).thenReturn(Optional.of(budget));

        budgetService.delete(BudgetFixture.DEFAULT_ID);

        verify(budgetRepository).delete(budget);
    }

    /**
     * Testa se lança exceção ao deletar orçamento inexistente.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenDelete_givenBudgetNotFound_shouldThrowBusinessException() {
        mockAuthentication(UserFactory.DEFAULT_ID);
        UUID budgetId = UUID.randomUUID();

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(budgetId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Budget not found");
    }

    /**
     * Testa se o resumo retorna os valores corretos quando há orçamentos com gastos.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenGetOverview_givenBudgetsWithSpending_shouldReturnCorrectOverview() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        Budget foodBudget = BudgetFixture.create(user);

        when(budgetRepository.findByUserId(user.getId())).thenReturn(List.of(foodBudget));
        when(transactionRepository.sumByUserAndCategoryAndPeriod(
                eq(user.getId()), eq(CategoryType.FOOD), any(), any()))
                .thenReturn(new BigDecimal("350.00"));

        BudgetOverviewResponse result = budgetService.getOverview();

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).budgeted()).isEqualByComparingTo(BudgetFixture.DEFAULT_AMOUNT);
        assertThat(result.items().get(0).spent()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(result.items().get(0).remaining()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.items().get(0).exceeded()).isFalse();
        assertThat(result.totalBudgeted()).isEqualByComparingTo(BudgetFixture.DEFAULT_AMOUNT);
        assertThat(result.totalSpent()).isEqualByComparingTo(new BigDecimal("350.00"));
    }

    /**
     * Testa se o resumo retorna vazio quando não há orçamentos.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenGetOverview_givenNoBudgets_shouldReturnEmptyOverview() {
        mockAuthentication(UserFactory.DEFAULT_ID);

        when(budgetRepository.findByUserId(UserFactory.DEFAULT_ID)).thenReturn(Collections.emptyList());

        BudgetOverviewResponse result = budgetService.getOverview();

        assertThat(result.items()).isEmpty();
        assertThat(result.totalBudgeted()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Testa se o resumo sinaliza orçamento como excedido quando os gastos ultrapassam o limite.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenGetOverview_givenExceededBudget_shouldFlagAsExceeded() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        Budget foodBudget = BudgetFixture.create(user);

        when(budgetRepository.findByUserId(user.getId())).thenReturn(List.of(foodBudget));
        when(transactionRepository.sumByUserAndCategoryAndPeriod(
                eq(user.getId()), eq(CategoryType.FOOD), any(), any()))
                .thenReturn(new BigDecimal("600.00"));

        BudgetOverviewResponse result = budgetService.getOverview();

        assertThat(result.items().get(0).exceeded()).isTrue();
        assertThat(result.items().get(0).remaining()).isEqualByComparingTo(new BigDecimal("-100.00"));
    }

    /**
     * Testa se o resumo retorna o valor total restante quando não há gastos.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenGetOverview_givenBudgetWithNoSpending_shouldReturnFullRemaining() {
        User user = UserFactory.createUser();
        mockAuthentication(user.getId());
        Budget budget = BudgetFixture.create(user);

        when(budgetRepository.findByUserId(user.getId())).thenReturn(List.of(budget));
        when(transactionRepository.sumByUserAndCategoryAndPeriod(
                eq(user.getId()), eq(CategoryType.FOOD), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        BudgetOverviewResponse result = budgetService.getOverview();

        assertThat(result.items().get(0).spent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.items().get(0).remaining()).isEqualByComparingTo(BudgetFixture.DEFAULT_AMOUNT);
        assertThat(result.items().get(0).exceeded()).isFalse();
    }

    /**
     * Testa se retorna aviso quando o orçamento foi ultrapassado.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCheckBudgetWarning_givenExceededBudget_shouldReturnWarning() {
        User user = UserFactory.createUser();
        Budget budget = BudgetFixture.create(user);

        when(budgetRepository.findByUserIdAndCategory(user.getId(), CategoryType.FOOD))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.sumByUserAndCategoryAndPeriod(
                eq(user.getId()), eq(CategoryType.FOOD), any(), any()))
                .thenReturn(new BigDecimal("550.00"));

        String warning = budgetService.checkBudgetWarning(user.getId(), CategoryType.FOOD);

        assertThat(warning).contains("ultrapassou");
        assertThat(warning).contains("FOOD");
        assertThat(warning).contains("550.00");
        assertThat(warning).contains("500.00");
    }

    /**
     * Testa se retorna nulo quando os gastos estão dentro do orçamento.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCheckBudgetWarning_givenWithinBudget_shouldReturnNull() {
        User user = UserFactory.createUser();
        Budget budget = BudgetFixture.create(user);

        when(budgetRepository.findByUserIdAndCategory(user.getId(), CategoryType.FOOD))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.sumByUserAndCategoryAndPeriod(
                eq(user.getId()), eq(CategoryType.FOOD), any(), any()))
                .thenReturn(new BigDecimal("300.00"));

        String warning = budgetService.checkBudgetWarning(user.getId(), CategoryType.FOOD);

        assertThat(warning).isNull();
    }

    /**
     * Testa se retorna nulo quando não existe orçamento para a categoria.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCheckBudgetWarning_givenNoBudget_shouldReturnNull() {
        when(budgetRepository.findByUserIdAndCategory(UserFactory.DEFAULT_ID, CategoryType.FOOD))
                .thenReturn(Optional.empty());

        String warning = budgetService.checkBudgetWarning(UserFactory.DEFAULT_ID, CategoryType.FOOD);

        assertThat(warning).isNull();
    }

    /**
     * Testa se retorna nulo quando a categoria é nula.
     * Criado por: Gabriel Braga em 25/03/2026
     */
    @Test
    void whenCheckBudgetWarning_givenNullCategory_shouldReturnNull() {
        String warning = budgetService.checkBudgetWarning(UserFactory.DEFAULT_ID, null);

        assertThat(warning).isNull();
    }

    private void mockAuthentication(UUID userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}