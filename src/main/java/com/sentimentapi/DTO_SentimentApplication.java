// Pacote onde a aplicação está localizada
package com.sentimentapi;

// Importação das classes necessárias do Spring Boot
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// A anotação @SpringBootApplication indica a classe principal da aplicação
@SpringBootApplication
public class DTO_SentimentApplication {

    // Método principal - ponto de entrada da aplicação
    public static void main(String[] args) {
        // Inicializa a aplicação Spring Boot
        SpringApplication.run(DTO_SentimentApplication.class, args);
    }

}
