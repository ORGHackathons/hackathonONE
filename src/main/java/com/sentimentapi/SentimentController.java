// Pacote onde a aplicação está localizada
package com.sentimentapi;

// Importação de classes necessárias para a criação de endpoints HTTP e manipulação de dados
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// A anotação @RestController indica que essa classe vai gerenciar requisições HTTP e retornar respostas diretamente
// (sem necessidade de criar páginas HTML, como em aplicações tradicionais).
@RestController
public class SentimentController {

    // A classe precisa de uma instância do SentimentService, que é responsável por fazer o trabalho de análise de sentimento
    private final SentimentService sentimentService;

    // O construtor abaixo recebe uma instância do SentimentService e a armazena na variável sentimentService
    // Isso garante que a classe SentimentController possa acessar os métodos do SentimentService.
    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    // A anotação @PostMapping define que esse método vai responder a requisições HTTP do tipo POST para o caminho "/sentiment"
    @PostMapping("/sentiment")
    // O método recebe um corpo da requisição (o texto que será analisado) e retorna uma resposta HTTP
    // A resposta é um mapa (uma coleção de chave/valor) que será convertido automaticamente em JSON
    public ResponseEntity<Map<String, Object>> getSentiment(@RequestBody Map<String, String> request) {
        // Aqui, pegamos o texto enviado na requisição, que está mapeado pela chave "text"
        String text = request.get("text");

        // Se o texto não for fornecido ou for muito curto (menos de 5 caracteres), retornamos um erro
        if (text == null || text.length() < 5) {
            // ResponseEntity.badRequest() retorna uma resposta HTTP de erro (código 400) com uma mensagem de erro
            return ResponseEntity.badRequest().body(Map.of("error", "Texto muito curto ou inválido"));
        }

        // Se o texto for válido, chamamos o serviço SentimentService para prever o sentimento do texto
        SentimentPrediction prediction = sentimentService.predictSentiment(text);

        // Aqui, retornamos uma resposta HTTP 200 (sucesso) com a previsão de sentimento e a probabilidade
        return ResponseEntity.ok(Map.of(
                "previsao", prediction.getLabel(),  // O rótulo (label) é a previsão do sentimento (ex: "positivo" ou "negativo")
                "probabilidade", prediction.getProbability()  // A probabilidade associada à previsão (ex: 0.85 significa 85% de chance)
        ));
    }

    // Novo método GET para buscar informações de previsão de sentimento com base em um "id"
    @GetMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> getSentimentById(@PathVariable String id) {
        // Aqui você pode buscar a previsão de sentimento com base em um ID (no caso, um valor fictício)
        // Para fins de exemplo, vamos simular que encontramos a previsão com esse ID.
        SentimentPrediction prediction = sentimentService.getPredictionById(id);

        // Se não encontrar a previsão, retornamos um erro 404 (não encontrado)
        if (prediction == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Previsão não encontrada"));
        }

        // Caso contrário, retornamos a previsão encontrada
        return ResponseEntity.ok(Map.of(
                "previsao", prediction.getLabel(),
                "probabilidade", prediction.getProbability()
        ));
    }

    // Novo método PUT para atualizar uma previsão de sentimento
    @PutMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> updateSentiment(@PathVariable String id, @RequestBody Map<String, String> request) {
        // Recebe o novo texto e ID para atualizar a previsão
        String newText = request.get("text");

        // Verifica se o novo texto é válido
        if (newText == null || newText.length() < 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Texto muito curto ou inválido"));
        }

        // Atualiza a previsão chamando um método do SentimentService
        SentimentPrediction updatedPrediction = sentimentService.updatePrediction(id, newText);

        // Se não encontrar o ID ou algo der errado, retorna um erro 404
        if (updatedPrediction == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Previsão não encontrada para atualizar"));
        }

        // Retorna a previsão atualizada
        return ResponseEntity.ok(Map.of(
                "previsao", updatedPrediction.getLabel(),
                "probabilidade", updatedPrediction.getProbability()
        ));
    }

    // Novo método DELETE para excluir uma previsão de sentimento
    @DeleteMapping("/sentiment/{id}")
    public ResponseEntity<Map<String, Object>> deleteSentiment(@PathVariable String id) {
        // Chama o método do SentimentService para tentar excluir a previsão com o ID fornecido
        boolean isDeleted = sentimentService.deletePrediction(id);

        // Se a previsão não for encontrada ou não puder ser excluída, retorna um erro 404
        if (!isDeleted) {
            return ResponseEntity.status(404).body(Map.of("error", "Previsão não encontrada para excluir"));
        }

        // Retorna uma resposta 200 (sucesso) indicando que a previsão foi excluída
        return ResponseEntity.ok(Map.of("message", "Previsão excluída com sucesso"));
    }
}
