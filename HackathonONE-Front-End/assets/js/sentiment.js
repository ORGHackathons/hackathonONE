import { API_URL } from "./api.js";

export async function criarComentario(text) {
    const res = await fetch(`${API_URL}/sentiment`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text })
    });
    return res.json();
}

export async function atualizarComentario(id, text) {
    const res = await fetch(`${API_URL}/sentiment/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text })
    });
    return res.json();
}

export async function uploadCsv(file) {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch(`${API_URL}/sentiment/lote`, {
        method: "POST",
        body: formData
    });
    return res.json();
}
