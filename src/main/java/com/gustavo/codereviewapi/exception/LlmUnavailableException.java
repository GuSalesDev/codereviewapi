package com.gustavo.codereviewapi.exception;

/**
 * Lançada quando a chamada ao provedor de LLM falha (timeout, erro 5xx,
 * circuit breaker aberto, etc). Mapeada para HTTP 503 pelo GlobalExceptionHandler.
 */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmUnavailableException(String message) {
        super(message);
    }
}
