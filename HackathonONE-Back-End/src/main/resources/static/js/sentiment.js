import { API_URL } from "./api.js";

/**
 * Envia um texto individual para análise de sentimento e salva no banco.
 */
export async function criarComentario(text) {
    // Adicionamos uma validação simples antes do fetch
    if (!text || text.trim().length === 0) {
        throw new Error("O texto do comentário não pode estar vazio.");
    }

    const res = await fetch(`${API_URL}/sentiment`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: text })
    });

    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || `Erro na API: ${res.status}`);
    }
    return res.json();
}

/**
 * Atualiza o texto de um registro existente por ID.
 */
export async function atualizarComentario(id, text) {
    const res = await fetch(`${API_URL}/sentiment/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: text }) // Garantindo o par chave:valor
    });

    if (!res.ok) throw new Error(`Erro ao atualizar registro #${id}: ${res.status}`);
    return res.json();
}

/**
 * Envia um arquivo CSV para processamento em lote.
 */
export async function uploadCsv(file) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch(`${API_URL}/sentiment/lote`, {
        method: "POST",
        // Importante: Não definimos Content-Type aqui, o navegador faz isso automaticamente para FormData
        body: formData
    });

    if (!res.ok) throw new Error(`Erro no processamento do lote: ${res.status}`);
    return res.json();
}