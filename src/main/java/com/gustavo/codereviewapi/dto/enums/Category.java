package com.gustavo.codereviewapi.dto.enums;

/**
 * Categoria de uma sugestão de code review.
 * Fechado propositalmente: o prompt enviado ao LLM instrui o modelo
 * a classificar cada achado em uma dessas categorias.
 */
public enum Category {
    BUG,
    SECURITY,
    PERFORMANCE,
    BEST_PRACTICE,
    STYLE
}
