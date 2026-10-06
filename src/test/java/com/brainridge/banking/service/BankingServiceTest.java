package com.brainridge.banking.service;

import com.brainridge.banking.account.AccountNotFoundException;
import com.brainridge.banking.account.AccountResponse;
import com.brainridge.banking.account.CreateAccountRequest;
import com.brainridge.banking.account.InMemoryAccountRepository;
import com.brainridge.banking.common.InvalidRequestException;
import com.brainridge.banking.transaction.BankingService;
import com.brainridge.banking.transaction.InMemoryTransactionRepository;
import com.brainridge.banking.transaction.InsufficientFundsException;
import com.brainridge.banking.transaction.TransactionResponse;
import com.brainridge.banking.transaction.TransactionType;
import com.brainridge.banking.transaction.TransferRequest;
import com.brainridge.banking.transaction.TransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankingServiceTest {

    private BankingService bankingService;

    @BeforeEach
    void setUp() {
        bankingService = new BankingService(
                new InMemoryAccountRepository(),
                new InMemoryTransactionRepository()
        );
    }

    @Test
    void createsAccountAndRecordsInitialDeposit() {
        AccountResponse account = bankingService.createAccount(
                new CreateAccountRequest(new BigDecimal("100.00"))
        );

        assertThat(account.id()).isNotNull();
        assertThat(account.balance()).isEqualByComparingTo("100.00");

        List<TransactionResponse> history = bankingService.getTransactionHistory(account.id());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.INITIAL_DEPOSIT);
        assertThat(history.get(0).amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void transfersFundsAndUpdatesBothAccountBalances() {
        AccountResponse source = bankingService.createAccount(
                new CreateAccountRequest(new BigDecimal("100.00"))
        );
        AccountResponse destination = bankingService.createAccount(
                new CreateAccountRequest(new BigDecimal("25.00"))
        );

        TransferResponse transfer = bankingService.transfer(new TransferRequest(
                source.id(),
                destination.id(),
                new BigDecimal("40.00")
        ));

        assertThat(transfer.transferId()).isNotNull();
        assertThat(transfer.amount()).isEqualByComparingTo("40.00");
        assertThat(transfer.fromAccountBalance()).isEqualByComparingTo("60.00");
        assertThat(transfer.toAccountBalance()).isEqualByComparingTo("65.00");
        assertThat(bankingService.getAccount(source.id()).balance()).isEqualByComparingTo("60.00");
        assertThat(bankingService.getAccount(destination.id()).balance()).isEqualByComparingTo("65.00");
        assertThat(bankingService.getTransactionHistory(source.id())).hasSize(2);
        assertThat(bankingService.getTransactionHistory(destination.id())).hasSize(2);
    }

    @Test
    void rejectsTransferWhenFundsAreInsufficient() {
        AccountResponse source = bankingService.createAccount(
                new CreateAccountRequest(new BigDecimal("10.00"))
        );
        AccountResponse destination = bankingService.createAccount(
                new CreateAccountRequest(BigDecimal.ZERO)
        );

        assertThatThrownBy(() -> bankingService.transfer(new TransferRequest(
                source.id(), destination.id(), new BigDecimal("10.01")
        )))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(bankingService.getAccount(source.id()).balance()).isEqualByComparingTo("10.00");
        assertThat(bankingService.getAccount(destination.id()).balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsSelfTransfer() {
        AccountResponse account = bankingService.createAccount(
                new CreateAccountRequest(new BigDecimal("10.00"))
        );

        assertThatThrownBy(() -> bankingService.transfer(new TransferRequest(
                account.id(), account.id(), new BigDecimal("1.00")
        )))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Source and destination accounts must be different");
    }

    @Test
    void rejectsUnknownAccount() {
        assertThatThrownBy(() -> bankingService.getTransactionHistory(java.util.UUID.randomUUID()))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
