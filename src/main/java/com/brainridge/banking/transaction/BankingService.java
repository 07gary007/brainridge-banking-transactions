package com.brainridge.banking.transaction;

import com.brainridge.banking.account.Account;
import com.brainridge.banking.account.AccountNotFoundException;
import com.brainridge.banking.account.AccountRepository;
import com.brainridge.banking.account.AccountResponse;
import com.brainridge.banking.account.CreateAccountRequest;
import com.brainridge.banking.common.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Object stateLock = new Object();

    public BankingService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        if (request == null || request.initialBalance() == null) {
            throw new InvalidRequestException("initialBalance is required");
        }

        BigDecimal initialBalance = normalizeMoney(request.initialBalance(), "initialBalance");
        if (initialBalance.signum() < 0) {
            throw new InvalidRequestException("initialBalance must be zero or greater");
        }

        synchronized (stateLock) {
            Instant createdAt = Instant.now();
            Account account = new Account(UUID.randomUUID(), initialBalance, createdAt);
            accountRepository.save(account);

            if (initialBalance.signum() > 0) {
                transactionRepository.save(new Transaction(
                        UUID.randomUUID(),
                        account.getId(),
                        TransactionType.INITIAL_DEPOSIT,
                        initialBalance,
                        account.getBalance(),
                        null,
                        null,
                        createdAt
                ));
            }

            return toAccountResponse(account);
        }
    }

    public AccountResponse getAccount(UUID accountId) {
        synchronized (stateLock) {
            return toAccountResponse(findAccount(accountId));
        }
    }

    public TransferResponse transfer(TransferRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Transfer request is required");
        }
        if (request.fromAccountId() == null || request.toAccountId() == null) {
            throw new InvalidRequestException("Both source and destination account IDs are required");
        }
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidRequestException("Source and destination accounts must be different");
        }
        if (request.amount() == null) {
            throw new InvalidRequestException("amount is required");
        }

        BigDecimal amount = normalizeMoney(request.amount(), "amount");
        if (amount.signum() <= 0) {
            throw new InvalidRequestException("amount must be greater than zero");
        }

        synchronized (stateLock) {
            Account source = findAccount(request.fromAccountId());
            Account destination = findAccount(request.toAccountId());

            if (source.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(source.getId(), amount, source.getBalance());
            }

            source.withdraw(amount);
            destination.deposit(amount);
            accountRepository.save(source);
            accountRepository.save(destination);

            Instant createdAt = Instant.now();
            UUID transferId = UUID.randomUUID();
            transactionRepository.save(new Transaction(
                    UUID.randomUUID(),
                    source.getId(),
                    TransactionType.DEBIT,
                    amount,
                    source.getBalance(),
                    destination.getId(),
                    transferId,
                    createdAt
            ));
            transactionRepository.save(new Transaction(
                    UUID.randomUUID(),
                    destination.getId(),
                    TransactionType.CREDIT,
                    amount,
                    destination.getBalance(),
                    source.getId(),
                    transferId,
                    createdAt
            ));

            return new TransferResponse(
                    transferId,
                    source.getId(),
                    destination.getId(),
                    amount,
                    source.getBalance(),
                    destination.getBalance(),
                    createdAt
            );
        }
    }

    public List<TransactionResponse> getTransactionHistory(UUID accountId) {
        synchronized (stateLock) {
            findAccount(accountId);
            return transactionRepository.findByAccountId(accountId)
                    .stream()
                    .map(this::toTransactionResponse)
                    .toList();
        }
    }

    private Account findAccount(UUID accountId) {
        if (accountId == null) {
            throw new AccountNotFoundException(null);
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private BigDecimal normalizeMoney(BigDecimal amount, String fieldName) {
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidRequestException(fieldName + " must have at most 2 decimal places");
        }
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(account.getId(), account.getBalance(), account.getCreatedAt());
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.accountId(),
                transaction.type(),
                transaction.amount(),
                transaction.balanceAfter(),
                transaction.relatedAccountId(),
                transaction.transferId(),
                transaction.createdAt()
        );
    }
}
