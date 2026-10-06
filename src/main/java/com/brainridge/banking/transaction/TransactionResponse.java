package com.brainridge.banking.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        UUID relatedAccountId,
        UUID transferId,
        Instant createdAt
) {
}
