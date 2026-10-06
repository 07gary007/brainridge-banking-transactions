package com.brainridge.banking.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull(message = "initialBalance is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "initialBalance must be zero or greater")
        @Digits(integer = 12, fraction = 2, message = "initialBalance must have at most 12 integer digits and 2 decimal places")
        BigDecimal initialBalance
) {
}
