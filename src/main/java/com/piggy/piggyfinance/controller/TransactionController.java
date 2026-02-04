package com.piggy.piggyfinance.controller;

import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import com.piggy.piggyfinance.model.requests.CreateTransactionRequest;
import com.piggy.piggyfinance.model.responses.TransactionResponse;
import com.piggy.piggyfinance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.piggy.piggyfinance.mappers.TransactionMapper.TRANSACTION_MAPPER;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/app")
    public TransactionResponse create(@RequestBody @Valid CreateTransactionRequest request, Authentication authentication) {

        String email = authentication.getName();

        return TRANSACTION_MAPPER.toResponse(
                transactionService.createTransaction(request, email, TransactionSourceEnum.APP)
        );
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/whatsapp")
    public TransactionResponse createFromWhatsApp(@RequestBody @Valid CreateTransactionRequest request, Authentication authentication) {

        String email = authentication.getName();

        //TODO: colocar o mappamento direto na service
        return TRANSACTION_MAPPER.toResponse(
                transactionService.createTransaction(request, email, TransactionSourceEnum.WHATSAPP)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionResponse> list(TransactionFilter filter,
                                          @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
                                          Pageable pageable,
                                          Authentication authentication) {
        var page = transactionService.listTransactions(
                authentication.getName(),
                filter,
                pageable
        );

        //TODO: colocar o mappamento direto na service
        return TRANSACTION_MAPPER.toResponsePage(page);
    }
}
