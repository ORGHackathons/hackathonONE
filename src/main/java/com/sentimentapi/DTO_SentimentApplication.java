// Pacote onde a aplicação está localizada
package com.sentimentapi;

// Importação das classes necessárias do Spring Boot
// 'SpringApplication' é usado para iniciar a aplicação Spring Boot
// 'SpringBootApplication' é a anotação que marca a classe principal da aplicação
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// A anotação @SpringBootApplication indica que esta classe é a configuração principal
// da aplicação Spring Boot. Ela habilita a configuração automática, o scan de componentes,
// e outras funcionalidades essenciais do Spring Boot.
@SpringBootApplication
public class DTO_SentimentApplication {

    // Método principal - ponto de entrada da aplicação
    public static void main(String[] args) {

        // O SpringApplication.run() inicializa o contexto da aplicação Spring Boot.
        // Ele configura a aplicação, inicia o servidor (geralmente embutido, como o Tomcat),
        // e começa a processar as requisições HTTP.
        SpringApplication.run(DTO_SentimentApplication.class, args);
    }

}
