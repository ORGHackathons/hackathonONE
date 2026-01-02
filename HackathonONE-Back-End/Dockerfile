# Etapa 1: build com Maven (Atualizado para Java 21)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Define o diretório de trabalho
WORKDIR /app

# Copia arquivos do projeto para dentro da imagem
COPY pom.xml .
COPY src ./src

# Resolve dependências e compila o projeto
RUN mvn clean package -DskipTests

# Etapa 2: imagem final com o JAR gerado (Atualizado para Java 21)
FROM eclipse-temurin:21-jdk-alpine

# Diretório da aplicação
WORKDIR /app

# Copia o JAR gerado na etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot (Se sua aplicação roda na 8081, talvez precise ajustar aqui ou no docker-compose)
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]