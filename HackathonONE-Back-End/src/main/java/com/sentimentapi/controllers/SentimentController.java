package com.sentimentapi.controllers;

import com.sentimentapi.dtos.StatsDto;
import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.services.SentimentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// Controlador REST responsável por expor os endpoints
// relacionados à análise de sentimento
@RestController
public class SentimentController {

    // Camada de serviço onde está a lógica de negócio
    private final SentimentService sentimentService;

    // Injeção de dependência via construtor
    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    // Cria um novo comentário e gera a previsão de sentimento
    @PostMapping("/sentiment")
    public ResponseEntity<Map<String, Object>> getSentiment(
            @RequestBody Map<String, String> request) {

        // Extrai o texto enviado no corpo da requisição
        String text = request.get("text");

        // Validação defensiva:
        // - evita texto nulo
        // - evita textos muito curtos que prejudicam a previsão
        if (text == null || text.length() < 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Texto muito curto ou inválido"));
        }

        // Cria o comentário, chama o microserviço Python
        // e salva a previsão no banco de dados
        SentimentPrediction prediction =
                sentimentService.createComment(text);

        // Retorna apenas os dados necessários ao cliente
        return ResponseEntity.ok(Map.of(
                "previsao", prediction.getLabel(),
                "probabilidade", prediction.getProbability()
        ));
    }

    // Processa comentários em lote a partir de um arquivo CSV
    @PostMapping(
            value = "/sentiment/lote",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<SentimentPrediction> uploadCsv(
            @RequestParam("file") MultipartFile file) {

        // Toda a lógica de leitura e processamento do CSV
        // fica encapsulada no service
        return sentimentService.processoUploadCsv(file);
    }

    // Busca um comentário e sua previsão pelo ID
    @GetMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> getSentimentById(
            @PathVariable Long id) {

        // Busca o comentário no banco de dados
        CommentEntity comment =
                sentimentService.getPredictionById(id);

        // Caso não exista comentário para o ID informado,
        // retorna HTTP 404
        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Previsão não encontrada"));
        }

        // Retorna os dados do comentário e sua previsão
        return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "text", comment.getText(),
                "previsao", comment.getPrevisao().getLabel(),
                "probabilidade", comment.getPrevisao().getProbability()
        ));
    }

    // Retorna estatísticas de sentimento (percentual)
    // com base nos últimos N comentários
    @GetMapping("/sentiment/stats/{quantidade}")
    public ResponseEntity<Map<String, Object>> stats(
            @PathVariable int quantidade) {

        // Evita chamadas inválidas que poderiam
        // gerar divisão por zero no service
        if (quantidade <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A quantidade deve ser maior que zero"));
        }

        // Calcula os percentuais de sentimentos
        StatsDto stats =
                sentimentService.getStats(quantidade);

        // Retorna os valores já calculados
        return ResponseEntity.ok(Map.of(
                "positivo", stats.positivo(),
                "negativo", stats.negativo()
        ));
    }

    // Atualiza o texto de um comentário existente
    // e recalcula sua previsão de sentimento
    @PutMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> updateSentiment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        // Obtém o novo texto informado pelo usuário
        String newText = request.get("text");

        // Validação básica antes de processar
        if (newText == null || newText.length() < 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Texto muito curto ou inválido"));
        }

        // Atualiza o comentário e a previsão no banco
        Optional<CommentEntity> optionalComentario =
                sentimentService.updatePrediction(id, newText);

        // Caso o ID não exista, retorna 404
        if (optionalComentario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Previsão não encontrada para atualizar"));
        }

        CommentEntity updatedComment =
                optionalComentario.get();

        // Retorna o comentário já atualizado
        return ResponseEntity.ok(Map.of(
                "id", updatedComment.getId(),
                "text", updatedComment.getText(),
                "previsao", updatedComment.getPrevisao().getLabel(),
                "probabilidade", updatedComment.getPrevisao().getProbability()

        ));
    }

    // Remove um comentário e sua previsão do banco
    @DeleteMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> deleteSentiment(
            @PathVariable Long id) {

        // Tenta remover o comentário pelo ID
        Optional<CommentEntity> deleted =
                sentimentService.deletePrediction(id);

        // Se não existir, retorna 404
        if (deleted.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Comentário não encontrado"));
        }

        // Confirma a exclusão com sucesso
        return ResponseEntity.ok(
                Map.of("message", "Previsão excluída com sucesso")
        );
    }
}
