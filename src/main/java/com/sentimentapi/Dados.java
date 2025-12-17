package com.sentimentapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentimentapi.dtos.SentimentDadosDTO;
import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.repositories.CommentRepository;
import com.sentimentapi.repositories.SentimentPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

        if (commentRepository.count() > 0) return;

        if (new ClassPathResource("data.json").exists()) {
                DadosJson();

            }

        if (new ClassPathResource("data.csv").exists()) {
            DadosCsv();
        }
    }

    private void DadosJson() throws IOException {

        InputStream inputStream = new ClassPathResource("data.json").getInputStream();

        List<SentimentDadosDTO> dados =
                objectMapper.readValue(inputStream, new TypeReference<>() {
                });

        for (SentimentDadosDTO dto : dados) {
            salvar(dto);
        }
    }

    private void DadosCsv() throws IOException {

        Reader reader = new InputStreamReader(
                new ClassPathResource("dados.csv").getInputStream()
        );

        CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(reader);

        for (CSVRecord dadosCsv : parser) {
            SentimentDadosDTO dto = new SentimentDadosDTO(
                    dadosCsv.get("text"),
                    dadosCsv.get("previsao"),
                    Double.parseDouble(dadosCsv.get("probabilidade"))
            );
            salvar(dto);
        }


    }

    private void salvar(SentimentDadosDTO dto) {
        SentimentPrediction prediction = new SentimentPrediction(
                dto.getPrevisao(),
                dto.getProbabilidade()
        );

        predictionRepository.save(prediction);

        CommentEntity comment = new CommentEntity();
        comment.setText(dto.getText());
        comment.setPrevisao(prediction);

        commentRepository.save(comment);
    }
}





























































