package com.sentimentapi.repositories;

import com.sentimentapi.entities.SentimentPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositório JPA responsável por persistir e consultar
// as previsões de sentimento no banco de dados
public interface SentimentPredictionRepository
        extends JpaRepository<SentimentPrediction, Long> {

}
