# Etapa 1: Build com Maven (Java 21)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia apenas o pom.xml primeiro para baixar as dependências (otimiza cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e gera o JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final leve (JRE 21)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Define a porta do Spring Boot para 8081 (conforme seu front-end espera)
ENV SERVER_PORT=8081
EXPOSE 8081

# Comando para executar com otimização de memória para instâncias Cloud
ENTRYPOINT ["java", "-Xmx512M", "-jar", "app.jar"]