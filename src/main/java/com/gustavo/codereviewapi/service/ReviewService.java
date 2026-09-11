package com.gustavo.codereviewapi.service;

import com.gustavo.codereviewapi.dto.ReviewRequest;
import com.gustavo.codereviewapi.dto.ReviewResponse;

public interface ReviewService {

    /**
     * Analisa um ou mais arquivos e retorna as sugestões de code review.
     * A estratégia de chamada ao LLM (uma chamada com todos os arquivos vs.
     * uma chamada por arquivo) é decisão de implementação — ainda em aberto.
     */
    ReviewResponse review(ReviewRequest request);
}