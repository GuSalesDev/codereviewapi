package com.gustavo.codereviewapi.dto.enums;

/**
 * Por enquanto o fluxo é sempre síncrono (sempre termina em COMPLETED ou FAILED),
 * mas o campo já existe no contrato pensando em processamento assíncrono futuro
 * (ex: batches grandes de arquivos analisados em background).
 */
public enum ReviewStatus {
    COMPLETED,
    FAILED
}
