import { API_URL } from "./api.js";

export async function buscarComentario(id) {
    const res = await fetch(`${API_URL}/sentiment/${id}`);
    return res.json();
}

export async function deletarComentario(id) {
    await fetch(`${API_URL}/sentiment/${id}`, {
        method: "DELETE"
    });
}
