import streamlit as st
import requests
from langdetect import detect

# Configuração da página
st.set_page_config(page_title="Sentimento Multi-idioma", page_icon="📊")

# Função para detectar idioma
def detectar_idioma(texto):
    try:
        return detect(texto)
    except:
        return "unknown"

# Título e Interface
st.title("🔍 Analisador de Sentimento Multilíngue")

# Seletor de Idioma da Interface
idioma_ui = st.sidebar.selectbox("Idioma da Interface / Interface Language", ["Português", "Español"])

labels = {
    "Português": {"label": "Digite o comentário", "button": "Analisar", "lang_msg": "Idioma detectado"},
    "Español": {"label": "Escriba el comentario", "button": "Analizar", "lang_msg": "Idioma detectado"}
}

# Área de texto
text = st.text_area(labels[idioma_ui]["label"], "")

if st.button(labels[idioma_ui]["button"]):
    if text:
        # 1. Detectar Idioma
        sigla_idioma = detectar_idioma(text)
        nome_idioma = "Português" if sigla_idioma == 'pt' else "Español" if sigla_idioma == 'es' else "Outro/Other"

        st.info(f"{labels[idioma_ui]['lang_msg']}: {nome_idioma} ({sigla_idioma})")

        # 2. Chamar sua API Java na Oracle Cloud
        try:
            url_api = "http://137.131.172.156:8081/sentiment"
            payload = {"text": text}
            response = requests.post(url_api, json=payload)

            if response.status_code == 200:
                resultado = response.json()
                sentimento = resultado.get("previsao", "N/A")
                confianca = resultado.get("probabilidade", 0) * 100

                # Exibição Visual
                col1, col2 = st.columns(2)
                col1.metric("Sentimento", sentimento)
                col2.metric("Confiança", f"{confianca:.2f}%")

                if sentimento == "Positivo":
                    st.success("✅ O modelo identificou um sentimento positivo!")
                else:
                    st.error("😡 O modelo identificou um sentimento negativo.")
            else:
                st.warning("Erro ao conectar com a API Java.")
        except Exception as e:
            st.error(f"Erro de conexão: {e}")
    else:
        st.warning("Por favor, digite um texto.")