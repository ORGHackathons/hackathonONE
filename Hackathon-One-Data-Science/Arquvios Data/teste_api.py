import requests
import json

# URL da API rodando no Docker
url = "http://localhost:5000/predict"

# Lista de frases para testar
frases_teste = [
    "O produto chegou muito rápido e a qualidade é incrível!",
    "Péssimo atendimento, o produto veio quebrado e não me respondem.",
    "É um produto ok, cumpre o que promete mas nada demais.",
    "A entrega atrasou um pouco."
]

print(f"--- Testando API em {url} ---\n")

for frase in frases_teste:
    payload = {"text": frase}

    try:
        # Envia a requisição POST
        response = requests.post(url, json=payload)

        # Verifica se deu certo (Código 200)
        if response.status_code == 200:
            resultado = response.json()
            print(f"📝 Texto: '{frase}'")
            print(f"🔍 Previsão: {resultado.get('previsao')} | Confiança: {resultado.get('probabilidade')}")
            print("-" * 30)
        else:
            print(f"❌ Erro ao processar '{frase}': {response.text}")

    except requests.exceptions.ConnectionError:
        print("❌ Erro de Conexão: O Docker está rodando? Verifique se o comando 'docker run' está ativo.")
        break
    except Exception as e:
        print(f"❌ Erro inesperado: {e}")