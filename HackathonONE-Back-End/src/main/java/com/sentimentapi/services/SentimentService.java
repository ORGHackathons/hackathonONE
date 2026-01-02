package com.sentimentapi.services;

import com.sentimentapi.dtos.SentimentDadosDTO;
import com.sentimentapi.dtos.StatsDto;
import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.repositories.CommentRepository;
import com.sentimentapi.repositories.SentimentPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional // CORREÇÃO: Garante que o Postgres acesse campos LOB com segurança
public class SentimentService {

    private final RestTemplate restTemplate;
    private final CommentRepository commentRepository;
    private final SentimentPredictionRepository sentimentPredictionRepository;

    @Value("${sentiment.python.url:http://localhost:5000/predict}")
    private String pythonUrl;

    public SentimentPrediction predictSentiment(String text) {
        Map<String, String> body = Map.of("text", text);

        try {
            SentimentDadosDTO dto = restTemplate.postForObject(pythonUrl, body, SentimentDadosDTO.class);
            SentimentPrediction prediction = new SentimentPrediction();

            if (dto != null) {
                // Mapeia os dados do Python (previsao/probabilidade) para a entidade
                prediction.setLabel(dto.getPrevisao());
                prediction.setProbability(dto.getProbabilidade());
            } else {
                prediction.setLabel("Indefinido");
                prediction.setProbability(0.0);
            }
            return prediction;
        } catch (Exception e) {
            // Caso a IA Python esteja fora do ar, não quebra a API Java
            SentimentPrediction errorPrediction = new SentimentPrediction();
            errorPrediction.setLabel("Erro IA");
            errorPrediction.setProbability(0.0);
            return errorPrediction;
        }
    }

    @Transactional(readOnly = true)
    public CommentEntity getPredictionById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public StatsDto getStats(int quantidade) {
        // CORREÇÃO: Busca os últimos registros conforme solicitado pelo front-end
        Pageable pageable = PageRequest.of(0, quantidade);
        List<CommentEntity> comments = commentRepository.buscarPorUltimos(pageable);

        if (comments == null || comments.isEmpty()) {
            return new StatsDto(0.0, 0.0);
        }

        double positivo = 0;
        double negativo = 0;

        for (CommentEntity comment : comments) {
            // Verificação robusta contra nulos para evitar o erro 500
            if (comment.getPrevisao() != null && comment.getPrevisao().getLabel() != null) {
                String label = comment.getPrevisao().getLabel().toLowerCase();

                if (label.contains("positiv") || label.contains("positive")) {
                    positivo++;
                } else if (label.contains("negativ") || label.contains("negative")) {
                    negativo++;
                }
            }
        }

        double total = positivo + negativo;
        if (total == 0) return new StatsDto(0.0, 0.0);

        // Arredondamento para evitar dízimas no Dashboard
        double porcentagemPositivo = Math.round((positivo * 100.0) / total);
        double porcentagemNegativo = Math.round((negativo * 100.0) / total);

        return new StatsDto(porcentagemPositivo, porcentagemNegativo);
    }

    public Optional<CommentEntity> updatePrediction(Long id, String newText) {
        return commentRepository.findById(id).map(commentEntity -> {
            SentimentPrediction prediction = predictSentiment(newText);
            prediction = sentimentPredictionRepository.save(prediction);

            commentEntity.setText(newText);
            commentEntity.setPrevisao(prediction);
            return commentRepository.save(commentEntity);
        });
    }

    public Optional<CommentEntity> deletePrediction(Long id) {
        return commentRepository.findById(id).map(comment -> {
            commentRepository.delete(comment);
            return comment;
        });
    }

    public List<SentimentPrediction> processoUploadCsv(MultipartFile file) {
        List<SentimentPrediction> results = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            for (CSVRecord record : parser) {
                String text = record.get("text");
                SentimentPrediction prediction = createComment(text);
                results.add(prediction);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar csv", e);
        }
        return results;
    }

    public SentimentPrediction createComment(String text) {
        SentimentPrediction prediction = predictSentiment(text);
        // Persiste a predição primeiro
        prediction = sentimentPredictionRepository.save(prediction);

        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setPrevisao(prediction);
        comment.setDataCriacao(LocalDateTime.now());

        // Salva o comentário vinculado à predição
        commentRepository.save(comment);

        return prediction;
    }
}