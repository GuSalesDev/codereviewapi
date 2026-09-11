package com.gustavo.codereviewapi.dto;

import com.gustavo.codereviewapi.dto.enums.Category;
import com.gustavo.codereviewapi.dto.enums.Severity;

public record Suggestion(
        Integer line,
        Category category,
        Severity severity,
        String title,
        String description,
        String suggestedFix
) {
}
