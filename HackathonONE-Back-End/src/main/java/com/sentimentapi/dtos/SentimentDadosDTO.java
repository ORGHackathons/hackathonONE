package com.sentimentapi.dtos;

import lombok.*;


// DTO usado para transportar dados de sentimento
// Contém o texto analisado, a previsão gerada
// e a probabilidade associada ao sentimento
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SentimentDadosDTO {

        private String text;
        private String previsao;
        private Double probabilidade;

}
