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

    // O mapper que a gente quer testar — MapStruct gera a implementação em tempo de compilação
    private TransactionMapper transactionMapper;

    // Transação base reutilizada nos testes
    private Transaction transaction;
    private final UUID transactionId = UUID.randomUUID();
    private final LocalDateTime timestamp = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Pega a instância gerada pelo MapStruct
        transactionMapper = Mappers.getMapper(TransactionMapper.class);

        // Usuário só existe pra popular o relacionamento da transação
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        // Transação base com todos os campos preenchidos
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
        // Arrange - transaction já pronto no setUp

        // Act
        TransactionResponse response = transactionMapper.toResponse(transaction);

        // Assert - garante que todos os campos foram mapeados corretamente
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(transactionId);
        assertThat(response.description()).isEqualTo("Supermercado");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void toResponse_ShouldReturnNull_WhenTransactionIsNull() {
        // Arrange - não tem nada, o input é null mesmo

        // Act
        TransactionResponse response = transactionMapper.toResponse(null);

        // Assert - MapStruct retorna null quando o input é null, padrão dele
        assertThat(response).isNull();
    }

    @Test
    void toResponse_ShouldMapIncomeType_WhenTransactionTypeIsIncome() {
        // Arrange - recria a transação com tipo INCOME pra cobrir o outro enum
        Transaction income = transaction.toBuilder()
                .type(TransactionType.INCOME)
                .build();

        // Act
        TransactionResponse response = transactionMapper.toResponse(income);

        // Assert - só garante que INCOME não se perdeu no caminho
        assertThat(response.type()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    void toResponsePage_ShouldMapPageCorrectly_WhenPageHasContent() {
        // Arrange
        Page<Transaction> page = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(0, 10),
                1
        );

        // Act
        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(page);

        // Assert - página mapeada com os dados corretos
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
        // Arrange - página vazia, zero conteúdo
        Page<Transaction> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        // Act
        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(emptyPage);

        // Assert - não explode e retorna página vazia bonitinha
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isZero();
        assertThat(responsePage.getContent()).isEmpty();
    }

    @Test
    void toResponsePage_ShouldMapAllTransactions_WhenPageHasMultipleItems() {
        // Arrange - segunda transação reutilizando o builder da primeira
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

        // Act
        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(page);

        // Assert - as duas transações foram mapeadas na ordem certa
        assertThat(responsePage.getTotalElements()).isEqualTo(2);
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent().get(1).description()).isEqualTo("Uber");
        assertThat(responsePage.getContent().get(1).amount()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    void toResponsePage_ShouldPreservePageMetadata_WhenPageableIsProvided() {
        // Arrange - página 1 (segunda página), tamanho 5
        Page<Transaction> page = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(1, 5),
                6
        );

        // Act
        Page<TransactionResponse> responsePage = transactionMapper.toResponsePage(page);

        // Assert - metadados de paginação não podem ser perdidos no mapeamento
        assertThat(responsePage.getNumber()).isEqualTo(1);
        assertThat(responsePage.getSize()).isEqualTo(5);
        assertThat(responsePage.getTotalElements()).isEqualTo(6);
        assertThat(responsePage.getTotalPages()).isEqualTo(2);
    }
}