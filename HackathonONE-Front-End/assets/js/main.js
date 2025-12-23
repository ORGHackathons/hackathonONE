import { criarComentario, atualizarComentario, uploadCsv } from "./sentiment.js";
import { carregarStats } from "./stats.js";
import { buscarComentario, deletarComentario } from "./crud.js";
import  {iniciarTypewriter}  from "./typewriter.js";

iniciarTypewriter();

// Abrir caixa de comentário
document.getElementById("btn-comentario").onclick = () => {
    document.getElementById("box-comentario").style.display = "block";
};


// Criar comentário
document.getElementById("enviar-comentario").onclick = async () => {
    const text = document.getElementById("texto-comentario").value;
    const data = await criarComentario(text);

    document.getElementById("resultado-comentario").innerHTML = `
        <strong>Previsão:</strong> ${data.previsao}<br>
        <strong>Probabilidade:</strong> ${(data.probabilidade * 100).toFixed(2)}%
    `;
};

// Abrir seletor de arquivo
document.getElementById("btn-arquivo").addEventListener("click", () => {
    document.getElementById("arquivo-csv").click();
});

// Upload CSV
document.getElementById("arquivo-csv").addEventListener("change", async (e) => {
    const file = e.target.files[0];

    if (!file) return;

    const data = await uploadCsv(file);
    alert(`Arquivo processado: ${data.length} registros`);
});


// Estatísticas rápidas (10, 50, 100)
document.querySelectorAll(".btn-stats").forEach(btn => {
    btn.addEventListener("click", async () => {
        const qtd = btn.dataset.qtd;
        console.log("Buscando stats:", qtd);

        const data = await carregarStats(qtd);

        document.getElementById("resultado-stats").innerHTML = `
            <p>Positivo: ${data.positivo}%</p>
            <p>Negativo: ${data.negativo}%</p>
        `;
    });
});

// Estatística personalizada
document.getElementById("btn-stats-custom").addEventListener("click", async () => {
    const qtd = document.getElementById("stats-custom").value;

    if (!qtd || qtd <= 0) {
        alert("Digite uma quantidade válida");
        return;
    }

    console.log("Buscando stats custom:", qtd);

    const data = await carregarStats(qtd);

    document.getElementById("resultado-stats").innerHTML = `
        <p>Positivo: ${data.positivo}%</p>
        <p>Negativo: ${data.negativo}%</p>
    `;
});


// Buscar comentário
document.getElementById("btn-buscar").onclick = async () => {
    const id = document.getElementById("buscar-id").value;

    if (!id) {
        alert("Informe um ID válido");
        return;
    }

    const data = await buscarComentario(id);

   document.getElementById("resultado-busca").innerHTML = `
    <div class="resultado-busca-container">
        <div class="resultado-linha">
            <span class="resultado-label">Comentário</span>
            <p class="resultado-texto">${data.text}</p>
        </div>

        <div class="resultado-linha">
            <span class="resultado-label">Previsão</span>
            <span class="resultado-badge ${data.previsao.toLowerCase()}">
                ${data.previsao}
            </span>
        </div>

        <div class="resultado-linha">
            <span class="resultado-label">Probabilidade</span>
            <strong>${(data.probabilidade * 100).toFixed(2)}%</strong>
        </div>
    </div>
`;
};


// Deletar comentário
document.getElementById("btn-deletar").onclick = async () => {
    const id = document.getElementById("deletar-id").value;
    await deletarComentario(id);
    alert("Deletado");
};

// Atualizar comentário
document.getElementById("btn-atualizar").onclick = async () => {
    const id = document.getElementById("atualizar-id").value;
    const text = document.getElementById("atualizar-texto").value;

    const data = await atualizarComentario(id, text);

    document.getElementById("resultado-atualizacao").innerHTML = `
        <div class="resultado-busca-container">
            <div class="resultado-linha">
                <span class="resultado-label">Comentário Atualizado</span>
                <p class="resultado-texto">${data.text}</p>
            </div>

            <div class="resultado-linha">
                <span class="resultado-label">Previsão</span>
                <span class="resultado-badge ${data.previsao.toLowerCase()}">
                    ${data.previsao}
                </span>
            </div>

            <div class="resultado-linha">
                <span class="resultado-label">Probabilidade</span>
                <strong>${(data.probabilidade * 100).toFixed(2)}%</strong>
            </div>
        </div>
    `;
};
