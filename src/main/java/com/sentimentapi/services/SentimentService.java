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
import org.springframework.data.domain.Page; // Importante
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class SentimentService {

    private final RestTemplate restTemplate;
    private final CommentRepository commentRepository;
    private final SentimentPredictionRepository sentimentPredictionRepository;

    @Value("${sentiment.python.url:http://localhost:5000/predict}")
    private String pythonUrl;

    /**
     * AJUSTADO: Agora aceita Pageable e retorna Page.
     * Isso resolve o erro vermelho no SentimentController.
     */
    @Transactional(readOnly = true)
    public Page<CommentEntity> getAllComments(Pageable pageable) {
        return commentRepository.buscarPorUltimos(pageable);
    }

    /**
     * AJUSTADO: Corrigido para lidar com o retorno Page do Repositório.
     */
    @Transactional(readOnly = true)
    public StatsDto getStats(int quantidade) {
        // Criamos um request para pegar a 'quantidade' desejada na primeira página
        Pageable pageable = PageRequest.of(0, quantidade);
        Page<CommentEntity> commentsPage = commentRepository.buscarPorUltimos(pageable);

        if (commentsPage == null || commentsPage.isEmpty()) {
            return new StatsDto(0.0, 0.0);
        }

        // Extraímos a lista da página para fazer o cálculo
        List<CommentEntity> comments = commentsPage.getContent();

        double positivo = 0;
        double negativo = 0;

        for (CommentEntity comment : comments) {
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

        double porcentagemPositivo = Math.round((positivo * 100.0) / total);
        double porcentagemNegativo = Math.round((negativo * 100.0) / total);

        return new StatsDto(porcentagemPositivo, porcentagemNegativo);
    }

    // --- MANTIDOS OS MÉTODOS ABAIXO SEM ALTERAÇÃO ---

    public SentimentPrediction predictSentiment(String text) {
        Map<String, String> body = Map.of("text", text);
        try {
            SentimentDadosDTO dto = restTemplate.postForObject(pythonUrl, body, SentimentDadosDTO.class);
            SentimentPrediction prediction = new SentimentPrediction();
            if (dto != null) {
                prediction.setLabel(dto.getPrevisao());
                prediction.setProbability(dto.getProbabilidade());
            } else {
                prediction.setLabel("Indefinido");
                prediction.setProbability(0.0);
            }
            return prediction;
        } catch (Exception e) {
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
        prediction = sentimentPredictionRepository.save(prediction);

        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setPrevisao(prediction);
        comment.setDataCriacao(LocalDateTime.now());

        commentRepository.save(comment);
        return prediction;
    }
}