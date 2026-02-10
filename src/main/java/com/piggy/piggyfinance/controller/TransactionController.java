package com.piggy.piggyfinance.controller;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.responses.TransactionResponse;
import com.piggy.piggyfinance.model.responses.TransactionSummaryResponse;
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

import static com.piggy.piggyfinance.mappers.TransactionMapper.TRANSACTION_MAPPER;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/app")
    public TransactionResponse create(@RequestBody @Valid CreateTransactionRequest request) {

        return TRANSACTION_MAPPER.toResponse(
                transactionService.createTransaction(request,TransactionSourceEnum.APP)
        );
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/whatsapp")
    public TransactionResponse createFromWhatsApp(@RequestBody @Valid CreateTransactionRequest request) {

        //TODO: colocar o mappamento direto na service
        return TRANSACTION_MAPPER.toResponse(
                transactionService.createTransaction(request,TransactionSourceEnum.WHATSAPP)
        );
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

}
