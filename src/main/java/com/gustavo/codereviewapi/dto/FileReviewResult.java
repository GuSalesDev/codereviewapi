package com.gustavo.codereviewapi.dto;

import java.util.List;

public record FileReviewResult(
        String fileName,
        List<Suggestion> suggestions
) {
}
