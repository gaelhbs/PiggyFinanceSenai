package com.piggy.piggyfinance.controller;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.requests.UpdateTransactionRequest;
import com.piggy.piggyfinance.model.responses.TransactionResponse;
import com.piggy.piggyfinance.model.responses.TransactionSummaryResponse;
import com.piggy.piggyfinance.service.BudgetService;
import com.piggy.piggyfinance.service.TransactionService;
import com.piggy.piggyfinance.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import static com.piggy.piggyfinance.mappers.TransactionMapper.TRANSACTION_MAPPER;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final BudgetService budgetService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/app")
    public TransactionResponse create(@RequestBody @Valid CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request, TransactionSourceEnum.APP);
        return toResponseWithBudgetWarning(transaction);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/whatsapp")
    public TransactionResponse createFromWhatsApp(@RequestBody @Valid CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request, TransactionSourceEnum.WHATSAPP);
        return toResponseWithBudgetWarning(transaction);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionResponse getById(@PathVariable UUID id) {
        return TRANSACTION_MAPPER.toResponse(transactionService.getTransaction(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionResponse update(@PathVariable UUID id,
                                      @RequestBody @Valid UpdateTransactionRequest request) {
        Transaction transaction = transactionService.updateTransaction(id, request);
        return toResponseWithBudgetWarning(transaction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionResponse> list(TransactionFilter filter,
                                          @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
                                          Pageable pageable) {
        var page = transactionService.listTransactions(
                filter,
                pageable
        );

        //TODO: colocar o mappamento direto na service
        return TRANSACTION_MAPPER.toResponsePage(page);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/summary")
    public TransactionSummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return transactionService.getSummary(
                SecurityUtils.getAuthenticatedUserId(),
                startDate,
                endDate
        );
    }

    private TransactionResponse toResponseWithBudgetWarning(Transaction transaction) {
        TransactionResponse base = TRANSACTION_MAPPER.toResponse(transaction);

        String warning = null;
        if (transaction.getType() == TransactionType.EXPENSE) {
            warning = budgetService.checkBudgetWarning(
                    transaction.getUser().getId(),
                    transaction.getCategory()
            );
        }

        return new TransactionResponse(
                base.id(),
                base.description(),
                base.amount(),
                base.type(),
                base.timestamp(),
                warning
        );
    }
}
