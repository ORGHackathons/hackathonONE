# HackathonONE (Oracle + Alura)

# 📊 SentimentoAPI - Análise de Sentimento com IA e Microserviços

> **Hackathon MVP**: Solução automatizada para classificação de feedbacks de clientes utilizando Processamento de Linguagem Natural (NLP).

## 🌐 Acesso Direto (Cloud)
O projeto está em produção e pode ser acessado publicamente:
👉 **URL:** [http://137.131.172.156:8081](http://137.131.172.156:8081)

---

> **Hackathon MVP**: Solução automatizada para classificação de feedbacks de clientes utilizando Processamento de Linguagem Natural (NLP).

## 💡 Sobre o Projeto

Empresas recebem milhares de comentários diariamente e não conseguem ler todos manualmente. A **SentimentoAPI** oferece uma solução automática para classificar mensagens e gerar informações acionáveis, permitindo:

* **Triagem Ágil**: Identificar rapidamente se o tom é positivo ou negativo.
* **Priorização**: Direcionar respostas imediatas a críticas severas.
* **Métricas de Qualidade**: Gerar indicadores de satisfação (CSAT/NPS) ao longo do tempo.

## 🎯 Setor de Negócio
Focado em **Atendimento ao Cliente, Marketing e Operações** que buscam entender a saúde da marca através de avaliações, redes sociais e pesquisas de satisfação em escala.

---


## 🚀 Tecnologias

<div>
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Python-3.9-blue?style=for-the-badge&logo=python&logoColor=white">
  <img src="https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white">
  <img src="https://img.shields.io/badge/Scikit--learn-FF9800?style=for-the-badge&logo=scikit-learn&logoColor=white">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/PostgreSQL-42.5.6-blue?style=for-the-badge&logo=postgresql&logoColor=white">
  <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white">
</div>

---

### Componentes de Infraestrutura:
* **Oracle Cloud (OCI)**: Hospedagem em instância VM Ubuntu, utilizando regras de segurança para liberação de portas (8081) e firewall (iptables/ufw).
* **Docker Hub**: Imagens versionadas e distribuídas publicamente para facilitar o deploy contínuo.
    * `marcosynky/hackaones:java-api`
    * `marcosynky/hackaones:python-ia`
* **Docker Compose**: Orquestração de 3 containers (Frontend/API Java, Microserviço Python e Banco Postgres).

---



## 📊 Estado do Projeto

![Progresso](https://img.shields.io/badge/Progresso-95%25-green?style=for-the-badge&labelColor=000000&color=#4c1ogo=github)

## 🏗️ Arquitetura Técnica

O projeto demonstra a integração entre **Data Science** e **Engenharia de Software** utilizando uma arquitetura de microserviços para superar as limitações de interoperabilidade entre Java e Python.



 ```mermaid
graph LR
    A[Usuário/Frontend] -->|POST /sentiment| B{API Spring Boot}
    B -->|RestTemplate| C[Microserviço Python]
    C -->|Modelo .pkl| D[Inferência de IA]
    D -->|Previsão| C
    C -->|JSON| B
    B -->|Persistência| E[(PostgreSQL)]
    B -->|Resposta| A
    
    A -.->|PUT/DELETE| B
    A -.->|GET /stats| B
    

 ```
### Data Science

* **Python 3.9**
* **Scikit-learn** (Modelo de Regressão Logística)
* **Pandas** (Manipulação de dados)
* **TF-IDF Vectorizer** (Processamento de texto)
* **Joblib** (Serialização do modelo)
* **Flask** (Exposição do modelo como API)

### Infraestrutura

* **Docker** & **Docker Compose**

-----

## 🚀 Como Executar

### Pré-requisito: Treinamento do Modelo

Antes de subir os containers, é necessário gerar o arquivo binário do modelo de IA.

1.  Acesse a pasta `ds-python`.
2.  Execute o script de treinamento (necessário Python instalado localmente ou executar dentro de um container isolado):
    ```bash
    python treinar_modelo.py
    ```
    *Isso criará o arquivo `sentiment_model.joblib`.*

### Opção 1: Rodando com Docker (Recomendado)

Na raiz do projeto (onde está o `docker-compose.yml`):

 ```bash
docker-compose up --build
```

Aguarde até ver as mensagens de log indicando que ambos os serviços iniciaram.

* **API Principal:** `http://localhost:8081`
* **Serviço de IA (Interno):** `http://localhost:5000`

### Opção 2: Rodando Manualmente

**1. Subir o Serviço de Data Science:**

```bash
cd ds-python
pip install -r requirements.txt
python app_python.py
# O serviço rodará na porta 5000
```

**2. Subir o Back-End Java:**

```bash
cd backend-java
./mvnw spring-boot:run
# O serviço rodará na porta 8080
```

-----

## 🔌 Documentação da API

### Endpoint: Classificar Sentimento

Analisa um texto e retorna a previsão do sentimento e a confiança do modelo.

* **URL:** `/sentiment`
* **Método:** `POST`
* **Content-Type:** `application/json`

#### Exemplo de Requisição (Body)

```json
{
  "text": "O produto chegou rápido e a qualidade é excelente!"
}
```

#### Exemplo de Resposta (Sucesso - 200 OK)

```json
{
  "previsao": "Positivo",
  "probabilidade": 0.92
}
```

#### Exemplo de Resposta (Negativo)

```json
{
  "text": "Péssimo atendimento, nunca mais compro."
}
```

**Saída:**

```json
{
  "previsao": "Negativo",
  "probabilidade": 0.88
}
```

#### Tratamento de Erros

* **400 Bad Request:** Se o campo `text` estiver vazio ou nulo.
* **500 Internal Server Error:** Caso o serviço de IA esteja indisponível.

-----

## 🧠 Detalhes do Modelo de Data Science

Para este MVP, optamos por uma abordagem clássica e eficiente de Machine Learning Supervisionado:

1.  **Pré-processamento:** Limpeza básica de texto.
2.  **Vetorização (TF-IDF):** Transformamos os textos em números baseados na frequência e importância das palavras no corpus.
3.  **Algoritmo (Regressão Logística):** Escolhido por ser rápido, interpretável e apresentar excelente desempenho para classificação binária de textos curtos.
4.  **Métricas:** O modelo retorna não apenas a classe (Pos/Neg), mas a probabilidade (`predict_proba`), permitindo definir *thresholds* de confiança.

-----

## 🗄️ Camada de Dados (Persistência)

O sistema utiliza o **Spring Data JPA** com **Hibernate** para gerenciar a persistência. O modelo de dados é composto por:

* **Tabela `comentario_tb`**: Armazena o texto bruto do feedback e a data de criação.
* **Tabela `sentiment_prediction_tb`**: Armazena o rótulo gerado pela IA (Positivo/Negativo) e a probabilidade (confiança) da análise.

### Configurações de Banco no Docker:
As credenciais são gerenciadas via variáveis de ambiente no `docker-compose.yml`, garantindo segurança e flexibilidade:
- **URL**: `jdbc:postgresql://db:5432/sentiment`
- **Dialeto**: `PostgreSQLDialect`
- **Destaque**: Configuração de `non_contextual_creation: true` para lidar com campos de texto longo (LOB).

*Desenvolvido pela Equipe para o Hackathon 2025.*

-----


# 📝 Descrição Geral do Projeto

## 🎯 Setor de Negócio
**Atendimento ao Cliente / Marketing / Operações** Empresas que coletam grandes volumes de opiniões (avaliações, redes sociais, pesquisas de satisfação) e precisam de agilidade para classificar sentimentos em escala.

## 💻 O Projeto
Desenvolvimento de um ecossistema de microserviços capaz de receber textos brutos e aplicar modelos de **Processamento de Linguagem Natural (NLP)** para classificação de sentimento. A solução retorna resultados em formato JSON, permitindo o consumo imediato por dashboards ou sistemas de suporte.

## 👤 Necessidade do Cliente
Clientes corporativos enfrentam o desafio de processar feedbacks manualmente. Este projeto oferece:
* **Triagem Ágil**: Identificar rapidamente se o tom é positivo, neutro ou negativo.
* **Priorização**: Direcionar respostas imediatas a críticas severas (comentários negativos).
* **Análise Temporal**: Medir a evolução da satisfação do cliente ao longo do tempo.

## 📈 Validação de Mercado
A análise de sentimento é uma ferramenta estratégica para:
* Acelerar o atendimento (identificação de urgências).
* Monitorar o impacto de campanhas de marketing.
* Fornecer métricas acionáveis para PMEs que não possuem equipes de dados dedicadas.

---

## 🏗️ Arquitetura e Entregáveis (Hackathon MVP)

O projeto demonstra a integração real entre **Data Science** e **Engenharia de Software** utilizando uma arquitetura de microserviços para superar as limitações de interoperabilidade entre Java e Python.



### 1. Data Science (Python)
Focado no treinamento e exposição do modelo de IA:
* **Notebook Jupyter**: EDA (Exploração de Dados), limpeza e treinamento.
* **Modelo**: Pipeline utilizando **TF-IDF** + **Regressão Logística**.
* **API de Inferência**: Flask/FastAPI carregando o modelo serializado (`.joblib`) e expondo o endpoint de predição.

### 2. Back-End (Java Spring Boot)
Atua como o Gateway da aplicação e orquestrador de dados:
* **Endpoint REST**: `/sentiment` para recepção de requisições.
* **Integração**: Consumo do microserviço Python via `RestTemplate`.
* **Persistência**: Armazenamento de históricos em banco de dados (H2/PostgreSQL).
* **Dashboard**: Cálculo de estatísticas de sentimento (positivo/negativo).

---

## 🛠️ Funcionalidades do MVP

| Funcionalidade | Descrição |
| :--- | :--- |
| **Predição Online** | Endpoint `POST` que recebe texto e retorna label + probabilidade. |
| **Persistência** | Gravação automática de todas as consultas para auditoria e métricas. |
| **Métricas (`/stats`)** | Resumo percentual de satisfação dos últimos registros. |
| **Processamento em Lote** | Suporte a upload de arquivos CSV para análise massiva. |
| **Validação** | Tratamento de erros para textos vazios ou indisponibilidade da IA. |

---

## 🌟 Funcionalidades Extras (Diferenciais)
- **Detecção Automática de Idioma**: Suporte a Português (PT) e Espanhol (ES) via `langdetect`.
- **Interface de Teste Streamlit**: Playground interativo para validação rápida de hipóteses.
- **Arquitetura Híbrida**: Frontend Streamlit consumindo API Java em Nuvem.
 👉 **URL:** [http://137.131.172.156:8501](http://137.131.172.156:8501)


## 🧠 Conceito Técnico: Vetorização TF-IDF
Para converter palavras em números compreensíveis pela máquina, utilizamos a técnica **TF-IDF** ($Term Frequency - Inverse Document Frequency$):
$$w_{i,j} = tf_{i,j} \times \log(\frac{N}{df_i})$$

Isso garante que palavras irrelevantes (como "o", "a", "de") tenham menos peso, enquanto palavras carregadas de sentimento (como "péssimo", "excelente") sejam as protagonistas na decisão do modelo.

---
*Desenvolvido pela Equipe HackaOnes para o Hackathon 2025.*
