package com.localgig.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProjectPaymentRequest(@NotNull @Positive Long projectId,
                                    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) { }
