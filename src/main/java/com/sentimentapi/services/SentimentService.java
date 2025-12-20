package com.sentimentapi.services;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Classe de serviço responsável pela lógica de negócio
// relacionada à análise de sentimentos
@Service
@RequiredArgsConstructor
public class SentimentService {

    // Cliente HTTP usado para se comunicar com o microserviço Python
    private final RestTemplate restTemplate;

    // Repositório responsável por persistir comentários
    private final CommentRepository commentRepository;

    // Repositório responsável por persistir previsões de sentimento
    private final SentimentPredictionRepository sentimentPredictionRepository;

    // URL do microserviço Python.
    // Caso não exista configuração externa, usa o valor padrão
    @Value("${sentiment.python.url:http://localhost:5000/predict}")
    private String pythonUrl;

    /**
     * Envia um texto ao microserviço Python e retorna a previsão de sentimento.
     * Esse método centraliza a integração externa da aplicação.
     */
    public SentimentPrediction predictSentiment(String text) {

        // Corpo da requisição enviado ao serviço Python
        Map<String, String> body = Map.of("text", text);

        // Chamada HTTP POST ao microserviço
        SentimentPrediction prediction =
                restTemplate.postForObject(
                        pythonUrl,
                        body,
                        SentimentPrediction.class
                );

        // Tratamento defensivo:
        // garante que a aplicação não quebre caso o serviço Python falhe
        if (prediction == null) {
            return new SentimentPrediction("Indefinido", 0.0);
        }

        return prediction;
    }

    /**
     * Busca um comentário e sua previsão pelo ID.
     */
    public CommentEntity getPredictionById(Long id) {
        // Retorna o comentário se existir ou null caso contrário
        return commentRepository.findById(id).orElse(null);
    }

    /**
     * Calcula estatísticas de sentimento (percentual)
     * com base nos últimos N comentários.
     */
    public StatsDto getStats(int quantidade) {

        // Total de comentários existentes no banco
        long totalComments = commentRepository.count();

        // Evita divisão por zero quando não há dados
        if (totalComments == 0) {
            return new StatsDto(0.0, 0.0);
        }

        double positivo = 0;
        double negativo = 0;

        // Limita a consulta aos últimos N registros
        Pageable pageable = PageRequest.of(0, quantidade);
        List<CommentEntity> comments =
                commentRepository.buscarPorUltimos(pageable);

        // Contabiliza os sentimentos encontrados
        for (CommentEntity comment : comments) {

            // Aqui ocorre o erro que você viu antes se previsao for null
            if (comment.getPrevisao() == null) {
                continue;
            }

            String label = comment.getPrevisao().getLabel();

            if ("positivo".equalsIgnoreCase(label)) {
                positivo++;
            } else if ("negativo".equalsIgnoreCase(label)) {
                negativo++;
            }
        }

        double total = positivo + negativo;

        // Caso existam comentários sem classificação válida
        if (total == 0) {
            return new StatsDto(0.0, 0.0);
        }

        // Cálculo percentual
        double porcentagemPositivo = (positivo * 100.0) / total;
        double porcentagemNegativo = (negativo * 100.0) / total;

        return new StatsDto(porcentagemPositivo, porcentagemNegativo);
    }

    /**
     * Atualiza o texto de um comentário existente
     * e recalcula sua previsão de sentimento.
     */
    public Optional<CommentEntity> updatePrediction(Long id, String newText) {

        // Busca o comentário pelo ID
        Optional<CommentEntity> optionalComment =
                commentRepository.findById(id);

        // Retorna vazio caso o comentário não exista
        if (optionalComment.isEmpty()) {
            return Optional.empty();
        }

        CommentEntity commentEntity = optionalComment.get();

        // Gera nova previsão para o texto atualizado
        SentimentPrediction prediction =
                predictSentiment(newText);

        // Persiste a nova previsão
        prediction = sentimentPredictionRepository.save(prediction);

        // Atualiza os dados do comentário
        commentEntity.setText(newText);
        commentEntity.setPrevisao(prediction);

        return Optional.of(
                commentRepository.save(commentEntity)
        );
    }

    /**
     * Remove um comentário e retorna o registro excluído.
     */
    public Optional<CommentEntity> deletePrediction(Long id) {

        Optional<CommentEntity> optionalComment =
                commentRepository.findById(id);

        if (optionalComment.isEmpty()) {
            return Optional.empty();
        }

        CommentEntity comment = optionalComment.get();
        commentRepository.delete(comment);

        return Optional.of(comment);
    }

    /**
     * Processa um arquivo CSV contendo textos,
     * gera previsões de sentimento e persiste os dados.
     */
    public List<SentimentPrediction> processoUploadCsv(
            MultipartFile file) {

        List<SentimentPrediction> results = new ArrayList<>();

        try (Reader reader =
                     new InputStreamReader(file.getInputStream())) {

            // Configuração do parser considerando cabeçalho
            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : parser) {

                // Obtém o texto da coluna "text"
                String text = record.get("text");

                Map<String, String> body =
                        Map.of("text", text);

                // Chamada ao microserviço Python
                SentimentPrediction prediction =
                        restTemplate.postForObject(
                                pythonUrl,
                                body,
                                SentimentPrediction.class
                        );

                if (prediction != null) {

                    // Persiste a previsão
                    sentimentPredictionRepository.save(prediction);

                    // Cria e persiste o comentário
                    CommentEntity comment = new CommentEntity();
                    comment.setText(text);
                    comment.setPrevisao(prediction);
                    comment.setDataCriacao(LocalDateTime.now());

                    commentRepository.save(comment);

                    results.add(prediction);
                }
            }

        } catch (Exception e) {
            // Encapsula qualquer erro de IO ou parsing
            throw new RuntimeException("Erro ao processar csv", e);
        }

        return results;
    }

    /**
     * Cria um comentário individual,
     * gera a previsão e persiste os dados.
     */
    public SentimentPrediction createComment(String text) {

        // Gera a previsão
        SentimentPrediction prediction =
                predictSentiment(text);

        // Salva a previsão
        prediction =
                sentimentPredictionRepository.save(prediction);

        // Cria o comentário associado
        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setPrevisao(prediction);
        comment.setDataCriacao(LocalDateTime.now());

        commentRepository.save(comment);

        return prediction;
    }
}
