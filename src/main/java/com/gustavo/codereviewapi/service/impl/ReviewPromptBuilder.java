package com.gustavo.codereviewapi.service.impl;

import com.gustavo.codereviewapi.dto.FileInput;
import com.gustavo.codereviewapi.dto.ReviewRequest;

final class ReviewPromptBuilder {

    private ReviewPromptBuilder() {
    }

    static String build(ReviewRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um revisor de código sênior especializado em ")
                .append(request.languageOrDefault())
                .append(". Analise os arquivos abaixo e aponte problemas de ")
                .append(focusList(request))
                .append(".\n\n");

        prompt.append("""
                Responda APENAS com um array JSON válido, sem markdown, sem explicações,
                seguindo EXATAMENTE este formato (uma entrada por arquivo, na mesma ordem em que foram enviados):

                [
                  {
                    "fileName": "NomeDoArquivo.java",
                    "suggestions": [
                      {
                        "line": 12,
                        "category": "BUG" | "SECURITY" | "PERFORMANCE" | "BEST_PRACTICE" | "STYLE",
                        "severity": "HIGH" | "MEDIUM" | "LOW",
                        "title": "Título curto do problema",
                        "description": "Explicação do problema",
                        "suggestedFix": "Como corrigir, ou null se não aplicável"
                      }
                    ]
                  }
                ]

                Se um arquivo não tiver problemas, retorne "suggestions": [].
                Não invente números de linha; se não souber a linha exata, use null.

                """);

        prompt.append("Arquivos para revisar:\n\n");

        for (FileInput file : request.files()) {
            prompt.append("### Arquivo: ").append(file.fileName()).append("\n");
            prompt.append("```java\n");
            prompt.append(file.code());
            prompt.append("\n```\n\n");
        }

        return prompt.toString();
    }

    private static String focusList(ReviewRequest request) {
        if (request.focus() == null || request.focus().isEmpty()) {
            return "bugs, segurança, performance e boas práticas";
        }
        return String.join(", ", request.focus());
    }
}