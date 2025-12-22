package com.sentimentapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// Classe de configuração do Jackson
// Define um ObjectMapper como Bean para ser usado
// automaticamente na serialização e desserialização JSON
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
