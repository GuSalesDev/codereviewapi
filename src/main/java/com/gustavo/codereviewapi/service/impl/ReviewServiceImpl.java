package com.gustavo.codereviewapi.service.impl;

import com.gustavo.codereviewapi.dto.FileInput;
import com.gustavo.codereviewapi.dto.FileReviewResult;
import com.gustavo.codereviewapi.dto.ReviewRequest;
import com.gustavo.codereviewapi.dto.ReviewResponse;
import com.gustavo.codereviewapi.dto.enums.ReviewStatus;
import com.gustavo.codereviewapi.service.ReviewService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * TODO: substituir esta implementação mock pela integração real com o LLM
 * (LangChain4j) quando essa etapa do projeto for implementada.
 *
 * Por enquanto, retorna uma lista de sugestões vazia para cada arquivo,
 * apenas para validar o contrato da API end-to-end (controller -> service
 * -> response) antes de plugar a IA de verdade.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Override
    public ReviewResponse review(ReviewRequest request) {
        List<FileReviewResult> results = request.files().stream()
                .map(this::mockResultFor)
                .toList();

        return new ReviewResponse(
                UUID.randomUUID(),
                ReviewStatus.COMPLETED,
                Instant.now(),
                "Mock: %d arquivo(s) recebido(s), integração com LLM ainda não implementada."
                        .formatted(request.files().size()),
                results
        );
    }

    private FileReviewResult mockResultFor(FileInput file) {
        return new FileReviewResult(file.fileName(), List.of());
    }
}
