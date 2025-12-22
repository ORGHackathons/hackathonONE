package com.sentimentapi.dtos;

// DTO imutável que representa estatísticas de sentimento
// Armazena os percentuais de resultados positivos e negativos
public record StatsDto(
        double positivo,
        double negativo
) {
}
