package com.sentimentapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Entidade que representa um comentário analisado
// Armazena o texto original, a previsão de sentimento
// associada e a data de criação do registro
@Entity
@Table(name = "comentario_tb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Garante o SERIAL no Postgres
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sentiment_prediction_id", nullable = false)
    private SentimentPrediction previsao;

    // Alteração aqui: use @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime dataCriacao;

}