package com.localgig.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {
    private String[] values;
    private boolean ignoreCase;

    @Override
    public void initialize(AllowedValues constraint) {
        values = constraint.value();
        ignoreCase = constraint.ignoreCase();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return Arrays.stream(values).anyMatch(allowed -> ignoreCase
                ? allowed.equalsIgnoreCase(value.trim()) : allowed.equals(value.trim()));
    }
}
