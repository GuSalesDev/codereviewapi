package com.gustavo.codereviewapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileInput(

        @NotBlank(message = "fileName não pode estar vazio")
        String fileName,

        @NotBlank(message = "code não pode estar vazio")
        @Size(max = 20_000, message = "code excede o tamanho máximo permitido por arquivo (20.000 caracteres)")
        String code
) {
}
