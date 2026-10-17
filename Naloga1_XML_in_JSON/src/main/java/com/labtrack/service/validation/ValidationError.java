package com.labtrack.service.validation;

import java.util.List;

public record ValidationError(List<String> errors) {

    public ValidationError {
        errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}