import { API_URL } from "./api.js";

export async function carregarStats(qtd) {
    const res = await fetch(`${API_URL}/sentiment/stats/${qtd}`);
    return res.json();
}
