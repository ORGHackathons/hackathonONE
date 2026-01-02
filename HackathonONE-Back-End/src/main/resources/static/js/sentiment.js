import { API_URL } from "./api.js";

// No seu arquivo de funções API
export async function criarComentario(text) {
    const res = await fetch(`${API_URL}/sentiment`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }, // Define que o corpo é JSON
        body: JSON.stringify({ text: text }) // Envia o texto dentro da chave "text"
    });

    if (!res.ok) throw new Error(`Erro na API: ${res.status}`);
    return res.json();
}

export async function atualizarComentario(id, text) {
    const res = await fetch(`${API_URL}/sentiment/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" }, // Deve ser JSON
        body: JSON.stringify({ text }) // Deve enviar o objeto com a chave "text"
    });

    if (!res.ok) throw new Error(`Erro ao atualizar: ${res.status}`);
    return res.json();
}

export async function uploadCsv(file) {
    const formData = new FormData();
    formData.append("file", file);

    // No seu Controller, a rota está mapeada como "/sentiment/lote"
    const res = await fetch(`${API_URL}/sentiment/lote`, {
        method: "POST",
        body: formData
    });

    if (!res.ok) throw new Error(`Erro no upload: ${res.status}`);
    return res.json();
}