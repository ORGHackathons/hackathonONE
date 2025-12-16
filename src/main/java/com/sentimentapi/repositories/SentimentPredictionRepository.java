package com.sentimentapi.repositories;

import com.sentimentapi.entities.SentimentPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentimentPredictionRepository extends JpaRepository<SentimentPrediction, Long> {
}
