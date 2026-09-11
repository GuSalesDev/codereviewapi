package com.gustavo.codereviewapi.dto;

import com.gustavo.codereviewapi.dto.enums.ReviewStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        ReviewStatus status,
        Instant createdAt,
        String summary,
        List<FileReviewResult> files
) {
}
