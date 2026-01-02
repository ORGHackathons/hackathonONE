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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Texto do comentário enviado para análise
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    // Relacionamento obrigatório com a previsão de sentimento
    @ManyToOne(optional = false)
    @JoinColumn(name = "sentiment_prediction_id", nullable = false)
    private SentimentPrediction previsao;

    // Data e hora em que o comentário foi salvo no banco
    private LocalDateTime dataCriacao;

    // Método executado automaticamente antes do insert
    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }
}
