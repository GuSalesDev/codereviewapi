package com.gustavo.codereviewapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewRequest(

        String language,

        @NotEmpty(message = "files não pode estar vazio")
        @Size(max = 10, message = "no máximo 10 arquivos por requisição")
        @Valid
        List<FileInput> files,

        List<String> focus
) {
    // language default "java" quando não informado
    public String languageOrDefault() {
        return (language == null || language.isBlank()) ? "java" : language;
    }
}
