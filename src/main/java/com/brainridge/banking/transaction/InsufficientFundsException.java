package com.brainridge.banking.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super("Insufficient funds in account " + accountId
                + ": requested " + requestedAmount
                + ", available " + availableBalance);
    }
}
