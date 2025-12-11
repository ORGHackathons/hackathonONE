// Este código pertence ao pacote com.sentimentapi
package com.sentimentapi;

// Importa a classe 'Bean' que é usada para marcar métodos como "produtores" de objetos
import org.springframework.context.annotation.Bean;
// Importa a anotação 'Configuration', que indica que esta classe configura o comportamento da aplicação
import org.springframework.context.annotation.Configuration;
// Importa a classe 'RestTemplate', que é usada para fazer requisições HTTP (como buscar dados de outros sistemas)
import org.springframework.web.client.RestTemplate;

// A anotação @Configuration diz que essa classe vai ser responsável por configurar algumas funcionalidades da aplicação
@Configuration
public class RestConfig {

    // A anotação @Bean marca o método como um "produtor" de um objeto que o Spring irá gerenciar
    @Bean
    // O método 'restTemplate' cria e retorna um objeto do tipo RestTemplate
    public RestTemplate restTemplate() {
        // Aqui estamos criando e retornando uma nova instância de RestTemplate
        return new RestTemplate();
    }
}
