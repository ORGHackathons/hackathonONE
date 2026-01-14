package com.sentimentapi.controllers;

import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.repositories.CommentRepository;
import com.sentimentapi.repositories.SentimentPredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// A anotação @SpringBootTest indica que o Spring deve subir
// TODO o contexto da aplicação (controllers, services, repositories, etc.)
@SpringBootTest

// A anotação @AutoConfigureMockMvc configura automaticamente o MockMvc,
// permitindo simular requisições HTTP sem subir um servidor real
@AutoConfigureMockMvc
public class SentimentControllerTestIntegrcao {

    // O MockMvc é usado para simular chamadas HTTP (GET, POST, PUT, DELETE)
    // diretamente nos controllers da aplicação
    @Autowired
    private MockMvc mockMvc;

    // Repositório real de comentários, usado para preparar os dados no banco
    @Autowired
    private CommentRepository commentRepository;

    // Repositório real de previsões de sentimento
    @Autowired
    private SentimentPredictionRepository sentimentPredictionRepository;

    // A anotação @MockBean substitui o bean real do RestTemplate
    // por um mock dentro do contexto do Spring
    //
    // Isso evita que o teste faça chamadas reais ao microserviço Python
    @MockBean
    private RestTemplate restTemplate;

    // O método anotado com @BeforeEach é executado antes de cada teste
    // Ele garante que o banco esteja em um estado conhecido
    @BeforeEach
    void setup() {

        // Remove todos os comentários do banco para evitar
        // interferência entre testes
        commentRepository.deleteAll();

        // Cria e salva uma previsão de sentimento positivo
        SentimentPrediction positivo =
                sentimentPredictionRepository.save(
                        new SentimentPrediction("Positivo", 0.9)
                );

        // Cria e salva uma previsão de sentimento negativo
        SentimentPrediction negativo =
                sentimentPredictionRepository.save(
                        new SentimentPrediction("Negativo", 0.8)
                );

        // Cria o primeiro comentário com previsão positiva
        CommentEntity comment1 = new CommentEntity();
        comment1.setPrevisao(positivo);

        // Cria o segundo comentário com previsão positiva
        CommentEntity comment2 = new CommentEntity();
        comment2.setPrevisao(positivo);

        // Cria o terceiro comentário com previsão positiva
        CommentEntity comment3 = new CommentEntity();
        comment3.setPrevisao(positivo);

        // Cria o quarto comentário com previsão negativa
        CommentEntity comment4 = new CommentEntity();
        comment4.setPrevisao(negativo);

        // Salva todos os comentários no banco de dados de teste
        // Resultado final:
        // - 3 comentários positivos
        // - 1 comentário negativo
        commentRepository.saveAll(
                List.of(comment1, comment2, comment3, comment4)
        );
    }

    // Teste de integração que valida o endpoint
    // GET /sentiment/stats/{quantidade}
    @Test
    void deveRetonarPorcentagemCorretas() throws Exception {

        // Simula uma requisição HTTP GET para o endpoint
        // /sentiment/stats/4
        mockMvc.perform(
                        get("/sentiment/stats/4")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                // Verifica se o status HTTP retornado é 200 (OK)
                .andExpect(status().isOk())

                // Verifica se o campo "positivo" no JSON retornado é 75.0
                .andExpect(jsonPath("$.positivo").value(75.0))

                // Verifica se o campo "negativo" no JSON retornado é 25.0
                .andExpect(jsonPath("$.negativo").value(25.0));
    }
}
