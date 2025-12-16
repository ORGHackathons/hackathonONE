package com.sentimentapi.services;

import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.repositories.CommentRepository;
import com.sentimentapi.repositories.SentimentPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;

// A anotação @Service indica que essa classe é um serviço. Em uma aplicação, um serviço é responsável por realizar
// a lógica de negócio, ou seja, ele processa dados e faz as operações principais da aplicação.
@Service
@RequiredArgsConstructor
public class SentimentService {

    // O RestTemplate é uma classe que facilita o envio de requisições HTTP para outros serviços

    private final RestTemplate restTemplate;

    private final CommentRepository commentRepository;

    private final SentimentPredictionRepository sentimentPredictionRepository;


    // A anotação @Value é usada para injetar um valor de configuração no código.
    // Aqui, ela pega a URL de um microserviço Python de previsão de sentimentos de um arquivo de configuração (application.properties).
    // Se esse valor não estiver configurado, será utilizado um valor padrão, que é 'http://localhost:5001/predict'.
    @Value("${sentiment.python.url:http://localhost:5000/predict}")
    private String pythonUrl;


    // O método 'predictSentiment' recebe um texto como entrada e retorna um objeto SentimentPrediction com o resultado.
    // Este é o método que faz a previsão do sentimento (positivo, negativo, etc).
    public SentimentPrediction predictSentiment(String text) {
        // Monta o corpo da requisição, que será enviado ao microserviço Python.
        // O corpo é um JSON simples com o texto a ser analisado.
        Map<String, String> body = Map.of("text", text);

        // Usando o RestTemplate para fazer uma requisição POST para o serviço Python.
        // O método 'postForObject' envia a requisição para a URL do microserviço (pythonUrl) com o corpo 'body' e
        // espera uma resposta no formato da classe SentimentPrediction (que contém a previsão do sentimento).
        SentimentPrediction prediction =
                restTemplate.postForObject(pythonUrl, body, SentimentPrediction.class);

        CommentEntity comment = new CommentEntity();
        comment.setText(text);
        comment.setPrevisao(prediction);
        sentimentPredictionRepository.save(prediction);
        commentRepository.save(comment);

        // Se a previsão vier 'null' (indicação de erro no serviço Python), cria uma previsão padrão com "Indefinido" e probabilidade 0.0
        if (prediction == null) {
            return new SentimentPrediction("Indefinido", 0.0);
        }

        // Se a previsão for válida, retorna o objeto SentimentPrediction com o resultado
        return prediction;
    }

    // Método GET para buscar uma previsão de sentimento com base no ID
    public SentimentPrediction getPredictionById(String id) {
        // Aqui, simula-se a busca de uma previsão pelo ID.
        // Em um cenário real, você buscaria as previsões em um banco de dados.
        Map<String, SentimentPrediction> database = new HashMap<>();
        // Exemplo fictício de previsões
        database.put("1", new SentimentPrediction("Positivo", 0.95));
        database.put("2", new SentimentPrediction("Negativo", 0.80));

        // Retorna a previsão correspondente ao ID ou null se não encontrar
        return database.get(id);
    }


    public SentimentPrediction getStats(String id) {

        Map<String, SentimentPrediction> database = new HashMap<>();
        database.put("1", new SentimentPrediction("Positivo", 0.95));
        database.put("2", new SentimentPrediction("Negativo", 0.80));

        return database.get(id);
    }

    // Método PUT para atualizar uma previsão de sentimento
    public SentimentPrediction updatePrediction(String id, String newText) {
        // Aqui, simula-se a atualização de uma previsão.
        // Em um cenário real, você atualizaria a previsão em um banco de dados.
        // Apenas simula-se a mudança do sentimento com base no novo texto.

        SentimentPrediction updatedPrediction;

        if (id.equals("1")) {
            updatedPrediction = new SentimentPrediction("Neutro", 0.60);
        } else {
            updatedPrediction = new SentimentPrediction("Positivo", 0.85);
        }

        return updatedPrediction; // Retorna a previsão atualizada
    }

    // Método DELETE para excluir uma previsão de sentimento
    public boolean deletePrediction(String id) {
        // Aqui, simula-se a exclusão de uma previsão pelo ID.
        // Em um cenário real, você excluiria a previsão de um banco de dados.

        // Simulação: Exclui a previsão com ID "1"
        if ("1".equals(id)) {
            // Suponha que a previsão foi deletada com sucesso
            return true;
        }

        // Se o ID não for encontrado ou não puder ser excluído, retorna falso
        return false;
    }

    public void  salvarTextEProvisao(){

    }
}
