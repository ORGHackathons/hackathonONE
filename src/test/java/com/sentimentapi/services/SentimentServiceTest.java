package com.sentimentapi.services;

import com.sentimentapi.dtos.StatsDto;
import com.sentimentapi.entities.CommentEntity;
import com.sentimentapi.entities.SentimentPrediction;
import com.sentimentapi.repositories.CommentRepository;
import com.sentimentapi.repositories.SentimentPredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

// A anotação @ExtendWith(MockitoExtension.class)
// habilita o uso do Mockito no JUnit 5
@ExtendWith(MockitoExtension.class)
class SentimentServiceTest {

    // Mock do repositório de previsões de sentimento
    // Nenhum acesso real ao banco será feito
    @Mock
    SentimentPredictionRepository sentimentPredictionRepository;

    // Mock do repositório de comentários
    @Mock
    CommentRepository commentRepository;

    // Mock do RestTemplate para evitar chamadas HTTP reais
    @Mock
    RestTemplate restTemplate;

    // A anotação @InjectMocks cria uma instância real do SentimentService
    // e injeta automaticamente os mocks acima nele
    @InjectMocks
    SentimentService sentimentService;

    // Método executado antes de cada teste
    @BeforeEach
    void setup() {

        // Como o campo pythonUrl é privado e anotado com @Value,
        // usamos ReflectionTestUtils para setar manualmente o valor
        // durante o teste
        ReflectionTestUtils.setField(
                sentimentService,
                "pythonUrl",
                "http://localhost:5000/predict"
        );
    }

    // Teste que valida o cálculo das porcentagens
    // de comentários positivos e negativos
    @Test
    void deverComputarAPorcentaemCorreta() {

        // Cria previsões de sentimento
        SentimentPrediction positivo = new SentimentPrediction("Positivo", 0.9);
        SentimentPrediction negativo = new SentimentPrediction("negativo", 0.8);

        // Cria comentários associados às previsões
        CommentEntity comment1 = new CommentEntity();
        comment1.setPrevisao(positivo);

        CommentEntity comment2 = new CommentEntity();
        comment2.setPrevisao(positivo);

        CommentEntity comment3 = new CommentEntity();
        comment3.setPrevisao(positivo);

        CommentEntity comment4 = new CommentEntity();
        comment4.setPrevisao(negativo);

        // Simula que o banco possui 4 comentários
        when(commentRepository.count()).thenReturn(4L);

        // Simula a busca dos últimos comentários
        when(commentRepository.buscarPorUltimos(any()))
                .thenReturn(List.of(comment1, comment2, comment3, comment4));

        // Executa o método real do service
        StatsDto stats = sentimentService.getStats(4);

        // Verifica se as porcentagens foram calculadas corretamente
        assertEquals(75.00, stats.positivo(), 0.01);
        assertEquals(25.00, stats.negativo(), 0.01);
    }

    // Teste que valida se a predição retornada pelo serviço Python
    // é repassada corretamente pelo service
    @Test
    void deveVirPredicaoNaResposta() {

        // Cria uma previsão simulada
        SentimentPrediction sentimentPrediction =
                new SentimentPrediction("Positivo", 0.95);

        // Simula a chamada HTTP para o serviço Python
        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(SentimentPrediction.class)
        )).thenReturn(sentimentPrediction);

        // Executa o método real
        SentimentPrediction result =
                sentimentService.predictSentiment("bom produto");

        // Verifica se os dados retornados estão corretos
        assertEquals("Positivo", result.getLabel());
        assertEquals(0.95, result.getProbability());
    }

    // Teste que valida o comportamento do service
    // quando o serviço Python falha e retorna null
    @Test
    void deveRetornarAlgoQuandoFalhar() {

        // Simula falha na chamada HTTP (retorno null)
        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(SentimentPrediction.class)
        )).thenReturn(null);

        // Executa o método real
        SentimentPrediction result =
                sentimentService.predictSentiment("teste");

        // Verifica se o fallback foi aplicado corretamente
        assertEquals("Indefinido", result.getLabel());
        assertEquals(0.0, result.getProbability());
    }

    // Teste que valida a atualização de um comentário
    // com uma nova predição de sentimento
    @Test
    void deveAtualizarComentarioEPredicao() {

        // Cria um comentário existente
        CommentEntity comment = new CommentEntity();
        comment.setId(1L);

        // Simula a busca do comentário no banco
        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        // Cria uma nova predição simulada
        SentimentPrediction prediction =
                new SentimentPrediction("positivo", 0.8);

        // Simula a chamada ao serviço Python
        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(SentimentPrediction.class)
        )).thenReturn(prediction);

        // Simula o salvamento da predição
        when(sentimentPredictionRepository.save(any()))
                .thenReturn(prediction);

        // Simula o salvamento do comentário atualizado
        when(commentRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // Executa o método real do service
        CommentEntity result =
                sentimentService.updatePrediction(1L, "novo texto").get();

        // Verifica se o comentário foi atualizado corretamente
        assertEquals("novo texto", result.getText());
        assertEquals("positivo", result.getPrevisao().getLabel());
    }
}
