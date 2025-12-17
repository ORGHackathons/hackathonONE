package com.sentimentapi.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SentimentDadosDTO {


        private String text;
        private String previsao;
        private Double probabilidade;

}
