package com.sentimentapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentimentapi.dtos.SentimentDadosDTO;
import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.repositories.CommentRepository;
import com.sentimentapi.repositories.SentimentPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;


@Component
@RequiredArgsConstructor
@Profile("dev")
public class Dados implements CommandLineRunner {

    private final CommentRepository commentRepository;
    private final SentimentPredictionRepository predictionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        if (commentRepository.count() > 0) {
            return;
        }

        InputStream inputStream = new ClassPathResource("data.json").getInputStream();

        List<SentimentDadosDTO> dados = objectMapper.readValue(inputStream,
                new TypeReference<List<SentimentDadosDTO>>() {});

        for(SentimentDadosDTO dto : dados) {
            SentimentPrediction prediction = new SentimentPrediction();
            prediction.setLabel(dto.getPrevisao());
            prediction.setProbability(dto.getProbabilidade());


            predictionRepository.save(prediction);

            CommentEntity comment = new CommentEntity();
            comment.setText(dto.getText());
            comment.setPrevisao(prediction);

            commentRepository.save(comment);

        }


    }
}





























































