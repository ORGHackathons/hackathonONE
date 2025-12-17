package com.sentimentapi.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String text;


    @ManyToOne
    @JoinColumn(name = "sentiment_prediction_id")
    private SentimentPrediction previsao;

}


