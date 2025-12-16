package com.sentimentapi.dtos;

import lombok.Data;

@Data
public class SentimentDadosDTO {


        private String text;
        private String previsao;
        private Double probabilidade;

}
