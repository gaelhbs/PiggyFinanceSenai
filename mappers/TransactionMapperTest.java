package com.piggy.piggyfinance.mappers;

import com.piggy.piggyfinance.enums.CategoryType;
import com.piggy.piggyfinance.enums.TransactionSourceEnum;
import com.piggy.piggyfinance.enums.TransactionType;
import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.model.responses.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;

    private Transaction transaction;
    private final UUID transactionId = UUID.randomUUID();
    private final LocalDateTime timestamp = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        transactionMapper = Mappers.getMapper(TransactionMapper.class);

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        transaction = Transaction.builder()
                .id(transactionId)
                .description("Supermercado")
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .source(TransactionSourceEnum.APP)
                .category(CategoryType.FOOD)
                .timestamp(timestamp)
                .user(user)
                .build();
    }

    @Test
    void toResponse_ShouldMapAllFields_WhenTransactionIsValid() {
        TransactionResponse response = transactionMapper.toResponse(transaction);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(transactionId);
        assertThat(response.description()).isEqualTo("Supermercado");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void toResponse_ShouldReturnNull_WhenTransactionIsNull() {
        TransactionResponse response = transactionMapper.toResponse(null);

        assertThat(response).isNull();
    }

    @Test
    void toResponsePage_ShouldMapPageCorrectly_WhenPageHasContent() {
        Page<Transaction> page = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(0, 10),
                1
        );

        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(page);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getContent()).hasSize(1);

        TransactionResponse response = responsePage.getContent().get(0);
        assertThat(response.id()).isEqualTo(transactionId);
        assertThat(response.description()).isEqualTo("Supermercado");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void toResponsePage_ShouldReturnEmptyPage_WhenPageIsEmpty() {
        Page<Transaction> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(emptyPage);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isZero();
        assertThat(responsePage.getContent()).isEmpty();
    }

    @Test
    void toResponsePage_ShouldMapAllTransactions_WhenPageHasMultipleItems() {
        Transaction transaction2 = transaction.toBuilder()
                .id(UUID.randomUUID())
                .description("Uber")
                .amount(new BigDecimal("25.50"))
                .type(TransactionType.EXPENSE)
                .category(CategoryType.TRANSPORT)
                .build();

        Page<Transaction> page = new PageImpl<>(
                List.of(transaction, transaction2),
                PageRequest.of(0, 10),
                2
        );

        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(page);

        assertThat(responsePage.getTotalElements()).isEqualTo(2);
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent().get(1).description()).isEqualTo("Uber");
        assertThat(responsePage.getContent().get(1).amount()).isEqualByComparingTo(new BigDecimal("25.50"));
    }
}
