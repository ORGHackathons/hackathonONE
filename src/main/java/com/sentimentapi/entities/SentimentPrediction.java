// Pacote onde a aplicação está localizada
package com.sentimentapi.entities;

// Importação das anotações do Lombok, que ajudam a reduzir a quantidade de código repetitivo
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter; // Gera o getter automaticamente para as variáveis
import lombok.Setter; // Gera o setter automaticamente para as variáveis

// A anotação @Getter e @Setter do Lombok geram automaticamente os métodos de acesso (getters e setters) para os campos da classe.
// O getter serve para "pegar" o valor de um campo, e o setter serve para "definir" o valor de um campo.
// Essas anotações evitam que você tenha que escrever esses métodos manualmente.

@Entity
@Table(name = "sentiment_prediction_tb")
@Setter
@Getter
public class SentimentPrediction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos que armazenam o rótulo (label) do sentimento e a probabilidade associada a ele.
    @JsonProperty("previsao")
    private String label; // Rótulo do sentimento, como "Positivo" ou "Negativo"

    @JsonProperty("probabilidade")
    private double probability; // Probabilidade do sentimento ser o que foi previsto, como 0.85 (85% de chance)

    // Construtor da classe. O construtor é um método especial que é chamado quando a classe é criada.
    // Ele inicializa os campos da classe com os valores passados como parâmetros (label e probability).
    public SentimentPrediction(String label, double probability) {
        this.label = label; // Define o valor do atributo 'label' com o valor passado no construtor
        this.probability = probability; // Define o valor do atributo 'probability' com o valor passado no construtor
    }

    public SentimentPrediction() {

    }
}
