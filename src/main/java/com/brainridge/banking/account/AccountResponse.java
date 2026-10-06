package com.brainridge.banking.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        BigDecimal balance,
        Instant createdAt
) {
}
