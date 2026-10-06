package com.brainridge.banking.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "fromAccountId is required")
        UUID fromAccountId,
        @NotNull(message = "toAccountId is required")
        UUID toAccountId,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "amount must have at most 12 integer digits and 2 decimal places")
        BigDecimal amount
) {
}
