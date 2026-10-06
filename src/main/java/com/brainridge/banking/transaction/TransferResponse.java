package com.brainridge.banking.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        BigDecimal fromAccountBalance,
        BigDecimal toAccountBalance,
        Instant createdAt
) {
}
