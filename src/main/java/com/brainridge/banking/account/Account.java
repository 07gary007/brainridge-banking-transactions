package com.brainridge.banking.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public final class Account {

    private final UUID id;
    private final Instant createdAt;
    private BigDecimal balance;

    public Account(UUID id, BigDecimal initialBalance, Instant createdAt) {
        this.id = id;
        this.balance = initialBalance.setScale(2, RoundingMode.UNNECESSARY);
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Account balance cannot become negative");
        }
        balance = balance.subtract(amount).setScale(2, RoundingMode.UNNECESSARY);
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount).setScale(2, RoundingMode.UNNECESSARY);
    }
}
