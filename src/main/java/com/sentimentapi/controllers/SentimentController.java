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


@RestController
public class SentimentController {

    private final SentimentService sentimentService;

    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @GetMapping("/sentiment")
    public ResponseEntity<org.springframework.data.domain.Page<Map<String, Object>>> getAllSentiments(
            @org.springframework.data.web.PageableDefault(size = 5, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            org.springframework.data.domain.Pageable pageable) {

        // O seu Service precisará de um método que aceite Pageable: sentimentService.getAllComments(pageable)
        org.springframework.data.domain.Page<CommentEntity> commentsPage = sentimentService.getAllComments(pageable);

        org.springframework.data.domain.Page<Map<String, Object>> response = commentsPage.map(c -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getId());
            map.put("text", (c.getText() != null) ? c.getText() : "");
            map.put("previsao", (c.getPrevisao() != null) ? c.getPrevisao().getLabel() : "N/A");
            map.put("probabilidade", (c.getPrevisao() != null) ? c.getPrevisao().getProbability() : 0.0);
            return map;
        });

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sentiment")
    public ResponseEntity<Map<String, Object>> getSentiment(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.trim().length() < 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Texto muito curto ou inválido"));
        }

        SentimentPrediction prediction = sentimentService.createComment(text);

        return ResponseEntity.ok(Map.of(
                "previsao", prediction.getLabel(),
                "probabilidade", prediction.getProbability()
        ));
    }

    @PostMapping(value = "/sentiment/lote", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SentimentPrediction>> uploadCsv(@RequestParam("file") MultipartFile file) {
        List<SentimentPrediction> predictions = sentimentService.processoUploadCsv(file);
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> getSentimentById(@PathVariable Long id) {
        CommentEntity comment = sentimentService.getPredictionById(id);

        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "ID não encontrado"));
        }

        return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "text", comment.getText(),
                "previsao", comment.getPrevisao().getLabel(),
                "probabilidade", comment.getPrevisao().getProbability()
        ));
    }

    @GetMapping("/sentiment/stats/{quantidade}")
    public ResponseEntity<Map<String, Object>> stats(@PathVariable int quantidade) {
        if (quantidade <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantidade inválida"));
        }

        StatsDto stats = sentimentService.getStats(quantidade);

        return ResponseEntity.ok(Map.of(
                "positivo", stats.positivo(),
                "negativo", stats.negativo()
        ));
    }

    @PutMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> updateSentiment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newText = request.get("text");

        if (newText == null || newText.trim().length() < 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Texto inválido"));
        }

        Optional<CommentEntity> optionalComentario = sentimentService.updatePrediction(id, newText);

        if (optionalComentario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Comentário não encontrado"));
        }

        CommentEntity updated = optionalComentario.get();
        return ResponseEntity.ok(Map.of(
                "id", updated.getId(),
                "text", updated.getText(),
                "previsao", updated.getPrevisao().getLabel(),
                "probabilidade", updated.getPrevisao().getProbability()
        ));
    }

    @DeleteMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, String>> deleteSentiment(@PathVariable Long id) {
        Optional<CommentEntity> deleted = sentimentService.deletePrediction(id);

        if (deleted.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "ID inexistente"));
        }

        return ResponseEntity.ok(Map.of("message", "Excluído com sucesso"));
    }
}