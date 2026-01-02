# Usa uma imagem leve com Python
FROM python:3.9-slim

# Define o diretório de trabalho
WORKDIR /app

# [MELHORIA] Instala dependências do sistema necessárias para Scikit-learn/Pandas
# O 'rm -rf' no final limpa o cache do apt para manter a imagem pequena
RUN apt-get update && apt-get install -y \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# [MELHORIA] Copia APENAS o requirements primeiro para aproveitar o cache do Docker
COPY requirements.txt .

# Instala as bibliotecas
RUN pip install --no-cache-dir -r requirements.txt

# Copia o restante dos arquivos (código, modelo .pkl, etc)
COPY . .

# Expõe a porta
EXPOSE 5000

# Comando para rodar (Verifique se o nome do arquivo é este mesmo)
CMD ["python", "app_python.py"]