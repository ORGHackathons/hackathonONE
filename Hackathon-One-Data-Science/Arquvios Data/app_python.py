# -*- coding: utf-8 -*-

import re
import unicodedata
from flask import Flask, request, jsonify
import joblib
import numpy as np

# --- 1. FUNÇÃO DE LIMPEZA ---
def limpar_texto(texto):
    if not texto:
        return ""
    # Converte para minúsculas
    texto = texto.lower()
    # Remove acentos (Ex: "Atenção" -> "Atencao")
    texto = unicodedata.normalize('NFD', texto)
    texto = texto.encode('ascii', 'ignore').decode("utf-8")
    # Remove caracteres especiais e números
    texto = re.sub(r'[^a-z\s]', '', texto)
    # Remove espaços extras
    texto = " ".join(texto.split())
    return texto

app = Flask(__name__)

# --- Carregamento do Modelo ---
try:
    data = joblib.load('modelo_utlc_apps_sentimento.pkl')
    model = data['model']
    vectorizer = data['vectorizer']
    print(">>> Modelo carregado com sucesso!")
except Exception as e:
    print(f">>> Erro ao carregar modelo: {e}")
    model = None
    vectorizer = None

LABEL_MAP = {
    "negative": "Negativo",
    "neutral": "Neutro",
    "positive": "Positivo"
}

@app.route('/predict', methods=['POST'])
def predict():
    try:
        if not model or not vectorizer:
            return jsonify({"erro": "Modelo não carregado."}), 500

        dados = request.get_json()
        texto_original = dados.get('text') if dados else None

        if not texto_original or len(texto_original.strip()) < 5:
            return jsonify({"erro": "Texto inválido ou curto."}), 400

        # --- 2. APLICAÇÃO DA LIMPEZA ANTES DA PREDIÇÃO ---
        texto_para_modelo = limpar_texto(texto_original)

        # 1. Transformar o texto processado
        X = vectorizer.transform([texto_para_modelo])

        # 2. Fazer a previsão
        prediction_label = model.predict(X)[0]
        proba = float(model.predict_proba(X).max())

        # 3. Análise de pesos (Explicabilidade)
        palavras_relevantes = []
        if hasattr(model, 'coef_'):
            feature_names = vectorizer.get_feature_names_out()
            indices = X.nonzero()[1]
            classe_idx = np.where(model.classes_ == prediction_label)[0][0]
            
            if model.coef_.ndim > 1:
                pesos_classe = model.coef_[classe_idx]
            else:
                pesos_classe = model.coef_[0] if classe_idx == 1 else -model.coef_[0]

            for idx in indices:
                palavras_relevantes.append({
                    "palavra": feature_names[idx],
                    "peso": round(float(pesos_classe[idx]), 4)
                })
            
            palavras_relevantes = sorted(palavras_relevantes, key=lambda x: x['peso'], reverse=True)[:5]

        return jsonify({
            "previsao": LABEL_MAP.get(prediction_label, "Desconhecido"),
            "probabilidade": round(proba, 2),
            "texto_limpo_usado": texto_para_modelo, # Para você conferir o filtro
            "analise_pesos": palavras_relevantes
        })

    except Exception as e:
        return jsonify({"erro": str(e)}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)