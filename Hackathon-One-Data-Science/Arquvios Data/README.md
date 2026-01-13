## Projeto de Análise de Sentimento em Português

Este projeto implementa um modelo de classificação de sentimento para textos em português. Inclui pré-processamento de texto, treinamento de um modelo de Regressão Logística, e serialização para uso em aplicações Back-End.

### 1. Visão Geral e Principais Descobertas
O objetivo foi construir um classificador de sentimento para avaliações de produtos em português. O dataset `utlc_apps.csv` foi utilizado, onde ratings de 1 a 5 foram categorizados em `Negativo` (1, 2), `Neutro` (3) e `Positivo` (4, 5). Uma função de pré-processamento robusta foi desenvolvida, garantindo que palavras de negação (como 'não') fossem mantidas para uma análise mais precisa. Textos foram transformados em vetores TF-IDF e um modelo de Regressão Logística foi treinado.

### 2. Pré-processamento de Texto
Uma função robusta de pré-processamento de texto foi desenvolvida e aplicada à coluna `review_text`, criando a nova coluna `review_text_cleaned`. Este processo incluiu:
*   Conversão para minúsculas.
*   Remoção de URLs, menções e hashtags.
*   Remoção de caracteres especiais, números e pontuações, mantendo apenas letras acentuadas.
*   Tokenização.
*   Remoção de stopwords em português (com a correção para manter 'não').
*   Aplicação de stemming para reduzir as palavras às suas raízes.
Recursos NLTK (`stopwords`, `punkt`, `rslp`) foram garantidos para suporte ao pré-processamento.

### 3. Re-definição e Categorização dos Rótulos de Sentimento
A coluna `rating` original (escala 1-5) foi redefinida para três categorias de sentimento mais abrangentes: `Negativo`, `Neutro` e `Positivo`.
*   **Negativo**: Ratings 1, 2.
*   **Neutro**: Rating 3.
*   **Positivo**: Rating 4, 5.
Esta nova categorização (`sentimento_categorizado`) foi utilizada como alvo para o treinamento do modelo revisado, com a distribuição das classes sendo predominantemente Neutra e Positiva.

### 4. Transformação TF-IDF (com N-grams)
Os textos pré-processados (`review_text_cleaned`) e os novos rótulos de sentimento (`sentimento_categorizado`) foram divididos em conjuntos de treinamento e teste (80/20, estratificados). Um `TfidfVectorizer` foi inicializado e ajustado aos dados de treinamento, configurado para incluir unigramas e bigramas (`ngram_range=(1, 2)`) e com `min_df=1` para tentar garantir a inclusão de termos raros.
*   **Dimensões dos Vetores TF-IDF**: `X_train_tfidf` (67992, ~599261) e `X_test_tfidf` (16999, ~599261) após a inclusão de n-grams.

### 5. Treinamento e Avaliação do Modelo de Sentimento (Versão Final Pós-N-grams)
Um modelo de Regressão Logística (`LogisticRegression`) foi re-treinado utilizando os vetores TF-IDF atualizados e os rótulos de sentimento categorizados.

**Desempenho do Modelo (Versão Final Pós-N-grams)**:
*   **Acurácia**: Aproximadamente **63.96%** no conjunto de teste, demonstrando uma melhora significativa em relação às versões anteriores sem N-grams.
*   **Relatório de Classificação**:
    *   **Positivo**: Continua com a melhor performance, com 69% de precisão, 68% de recall e F1-score de 69%.
    *   **Negativo**: Apresentou 70% de precisão, mas um recall de 52%, resultando em F1-score de 60%.
    *   **Neutro**: Obteve 57% de precisão, 66% de recall e F1-score de 61%.
*   **Matriz de Confusão**: Visualizada para detalhar as previsões, mostrando que o modelo ainda tem desafios em diferenciar classes adjacentes, mas com uma melhoria geral.
![Matriz de Confusão do Modelo Final](images/matriz_confusao.png)

### 6. Serialização do Modelo e Vetorizador
Ambos o modelo de Regressão Logística re-treinado e o `TfidfVectorizer` foram serializados utilizando `joblib` para uso futuro em aplicações de Back-End.
*   **Modelo Serializado**: `modelo_sentimento_revisado.joblib`
*   **Vetorizador Serializado**: `tfidf_vectorizer_revisado.joblib`

### 7. Validação e Teste do Modelo Serializado
Os arquivos serializados foram carregados com sucesso e seus atributos inspecionados (vocabulário do TF-IDF, classes e coeficientes do modelo), confirmando a persistência correta dos objetos.

### 8. Desafios e Soluções (Negações e Termos Raros)
Um desafio persistente foi a misclassificação de frases com negação, como "O pacote foi extraviado e ainda não recebi meu reembolso.", inicialmente prevista como "Positivo". Após ajustes no pré-processamento para manter a palavra 'não' e a inclusão de N-grams (`ngram_range=(1,2)`) no `TfidfVectorizer` com `min_df=1`, a previsão para esta frase específica foi corrigida para "Negativo" (probabilidades: 'Negativo': 0.8179, 'Neutro': 0.1690, 'Positivo': 0.0131), confirmando a eficácia das correções. Uma análise detalhada dos coeficientes do modelo para a frase problemática confirmou que termos como 'não receb' contribuem significativamente para a previsão negativa.

### 9. Estrutura do Projeto para Deploy
O projeto foi estruturado com foco em deploy fácil, incluindo:
*   `requirements.txt`: Lista de dependências Python.
*   `download_nltk_resources.py`: Script para automação do download de recursos NLTK.
*   `utils/preprocessing.py`: Módulo dedicado às funções de pré-processamento.
*   `models/`: Diretório para armazenar os modelos serializados.
*   `backend_sentiment_api.py`: Um script de exemplo para ilustrar como o modelo pode ser carregado e utilizado em um serviço de Back-End.
*   `images/`: Diretório para os gráficos gerados para este README.
*   `buscape.csv`: O dataset original.

### Conclusão e Próximos Passos
O projeto resultou em um modelo funcional e robusto de análise de sentimento para português, com tratamento aprimorado de negações. Os modelos serializados estão prontos para serem integrados em uma API. Futuras melhorias podem focar em:
*   **Implementação e Validação de Suporte Multilíngue**: Desenvolver e testar robustamente a capacidade de analisar textos em outros idiomas através da tradução automática, garantindo que o pré-processamento e o modelo se comportem de forma eficaz com textos traduzidos.
*   **Reavaliar a Inclusão de Termos Raros**: Investigar por que termos como 'extravi' não estão sendo incluídos no vocabulário do TF-IDF, mesmo com `min_df=1`. Pode ser necessário um `CountVectorizer` seguido de `TfidfTransformer` com um ajuste manual do vocabulário, ou uma análise mais profunda da distribuição de termos no corpus para entender a raridade e a representatividade.
*   **Técnicas de Balanceamento de Classes**: Para melhorar o desempenho das categorias com menor recall (ex: 'Negativo'), considerando que esta é a classe com menos amostras.
*   **Modelos Mais Complexos**: Explorar abordagens de Deep Learning (LSTMs, Transformers) ou SVMs, que podem capturar nuances contextuais e semânticas de negações de forma mais robusta e lidar melhor com a estrutura sequencial do texto.
*   **Aumento de Dados**: Adicionar mais exemplos de treinamento que contenham esses termos raros ou negações complexas para fortalecer o aprendizado do modelo.

## Como Executar o Projeto
Consulte o `README.md` completo e os scripts auxiliares para instruções detalhadas sobre a configuração do ambiente, preparação dos recursos NLTK e execução do exemplo da API.



### Projeto de Análise de Sentimento: UTLC Apps
Este projeto foca no desenvolvimento de um modelo de classificação de sentimentos em português, utilizando o dataset utlc_apps.csv. O objetivo é categorizar avaliações de aplicativos em três classes de sentimento: Negativo, Neutro e Positivo.

### 1. Visão Geral do Dataset
O conjunto de dados contém mais de 1 milhão de registros, apresentando as seguintes características:

Total de instâncias: 1.039.535.

Colunas principais: * review_text: Texto original da avaliação.

review_text_processed: Texto após limpeza e pré-processamento.

rating: Nota numérica atribuída pelo usuário (1 a 5).

Distribuição: A nota média é de aproximadamente 3.95, indicando uma tendência predominante para avaliações positivas no corpus original.

### 2. Engenharia de Labels (Categorização)
Para simplificar o problema de classificação e aumentar a robustez do modelo, as avaliações originais (escala 1-5) foram mapeadas em categorias semânticas:

Negativo: Notas 1 e 2.

Neutro: Nota 3.

Positivo: Notas 4 e 5.

### 3. Pipeline de Processamento e Modelagem
O fluxo de trabalho seguiu as etapas clássicas de Processamento de Linguagem Natural (NLP) e Machine Learning:

Vetorização: Utilizou-se o TfidfVectorizer para transformar o texto processado em representações numéricas, ponderando a importância das palavras com base em sua frequência no documento versus sua raridade no corpus (TF-IDF).

Modelagem: Foi treinado um modelo de Regressão Logística (LogisticRegression), selecionado por sua excelente eficiência computacional e alta interpretabilidade em tarefas de classificação binária e multiclasse de texto.

Métricas de Avaliação: O desempenho do modelo foi validado através de:

Acurácia Global.

F1-Score (para equilibrar precisão e recall).

Matriz de Confusão.

### 4. Resultados e Exportação
O modelo final foi consolidado em um objeto de pipeline para garantir que o vetorizador e o classificador permaneçam sincronizados durante o deploy:

Serialização: O modelo treinado e o vetorizador TF-IDF foram salvos em um único arquivo chamado modelo_utlc_apps_sentimento.pkl utilizando a biblioteca joblib.

Metadados do Pipeline: Além dos pesos do modelo, o arquivo exportado contém o mapa de sentimentos e a regra de conversão de rating utilizada (1-2=negative, 3=neutral, 4-5=positive), facilitando a integração com sistemas de Back-End.
