package com.gustavo.codereviewapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.gustavo.codereviewapi.dto.FileReviewResult;
import com.gustavo.codereviewapi.dto.ReviewRequest;
import com.gustavo.codereviewapi.dto.ReviewResponse;
import com.gustavo.codereviewapi.dto.enums.ReviewStatus;
import com.gustavo.codereviewapi.exception.LlmUnavailableException;
import com.gustavo.codereviewapi.service.ReviewService;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ReviewResponse review(ReviewRequest request) {
        String prompt = ReviewPromptBuilder.build(request);

        String rawResponse;
        try {
            rawResponse = chatModel.chat(prompt);
        } catch (Exception ex) {
            log.error("Falha ao chamar o LLM", ex);
            throw new LlmUnavailableException("Não foi possível obter resposta do provedor de LLM.", ex);
        }

        List<FileReviewResult> results = parseResponse(rawResponse);

        long totalSuggestions = results.stream()
                .mapToLong(f -> f.suggestions().size())
                .sum();

        return new ReviewResponse(
                UUID.randomUUID(),
                ReviewStatus.COMPLETED,
                Instant.now(),
                "%d arquivo(s) analisado(s), %d sugestão(ões) encontrada(s)."
                        .formatted(request.files().size(), totalSuggestions),
                results
        );
    }

    private List<FileReviewResult> parseResponse(String rawResponse) {
        String json = extractJson(rawResponse);
        try {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, FileReviewResult.class);
            return objectMapper.readValue(json, listType);
        } catch (Exception ex) {
            log.error("Falha ao interpretar a resposta do LLM. Resposta bruta: {}", rawResponse, ex);
            throw new LlmUnavailableException(
                    "O provedor de LLM retornou uma resposta em formato inesperado.", ex);
        }
    }

    private String extractJson(String rawResponse) {
        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }
}