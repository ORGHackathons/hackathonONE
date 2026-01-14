import { criarComentario, atualizarComentario, uploadCsv } from "./sentiment.js";
import { carregarStats } from "./stats.js";
import { buscarComentario, deletarComentario } from "./crud.js";
import { iniciarTypewriter } from "./typewriter.js";

// --- CONFIGURAÇÕES E ESTADO GLOBAL ---
// URL mapeada conforme o @GetMapping("/sentiment") do seu Controller Java
const API_BASE_URL = "http://137.131.172.156:8081/sentiment";
let paginaAtual = 0;

// Inicializa componentes ao carregar o DOM
document.addEventListener("DOMContentLoaded", () => {
    iniciarTypewriter();
    atualizarListaComentarios();
    configurarEventos();
});

// --- LISTAGEM DINÂMICA (Feed de Comentários) ---

async function atualizarListaComentarios(pagina = 0) {
    const listaContainer = document.getElementById("lista-comentarios");
    const pageInfo = document.getElementById("page-info");

    if (!listaContainer) return;

    try {
        // Chamada paginada: Spring Data espera page, size e sort
        const response = await fetch(`${API_BASE_URL}?page=${pagina}&size=5&sort=id,desc`);

        if (!response.ok) throw new Error(`Status: ${response.status}`);

        const data = await response.json();
        listaContainer.innerHTML = "";
        paginaAtual = pagina;

        // Spring Data JPA encapsula a lista na chave 'content'
        if (!data.content || data.content.length === 0) {
            listaContainer.innerHTML = "<p style='color: var(--text-secondary); padding: 20px;'>Nenhum registro encontrado no banco de dados.</p>";
            return;
        }

        data.content.forEach(item => {
            const div = document.createElement("div");
            div.className = "resultado-busca-container";
            div.style.marginBottom = "15px";
            div.style.padding = "15px";
            div.style.background = "rgba(255,255,255,0.03)";
            div.style.borderRadius = "8px";
            div.style.border = "1px solid #323238";

            // Tratamento de segurança para a badge
            const previsaoLabel = item.previsao || "Neutro";
            const previsaoClasse = previsaoLabel.toLowerCase();

            div.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 10px;">
                    <p class="resultado-texto" style="flex: 1; margin: 0; line-height: 1.5;">"${item.text}"</p>
                    <span class="resultado-badge ${previsaoClasse}">
                        ${previsaoLabel}
                    </span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-top: 10px; border-top: 1px solid #323238; padding-top: 8px;">
                     <small style="color: var(--text-secondary);">ID: #${item.id}</small>
                     <small style="color: var(--primary-color); font-weight: 600;">Confiança: ${(item.probabilidade * 100).toFixed(1)}%</small>
                </div>
            `;
            listaContainer.appendChild(div);
        });

        // Atualiza indicadores de paginação
        if (pageInfo) {
            pageInfo.innerText = `Página ${data.number + 1} de ${data.totalPages || 1}`;
        }

        const btnPrev = document.getElementById("prev-page");
        const btnNext = document.getElementById("next-page");

        if (btnPrev) btnPrev.disabled = data.first;
        if (btnNext) btnNext.disabled = data.last;

    } catch (error) {
        console.error("Erro na listagem:", error);
        listaContainer.innerHTML = `
            <p style='color: var(--danger-color); padding: 20px;'>
                Erro ao carregar dados. Verifique se o backend está ativo em: <br>
                <code style="color: white;">${API_BASE_URL}</code>
            </p>`;
    }
}

// --- VINCULAÇÃO DE EVENTOS ---

function configurarEventos() {
    const helper = (id, callback) => {
        const el = document.getElementById(id);
        if (el) el.onclick = callback;
    };

    // Navegação
    helper("next-page", () => atualizarListaComentarios(paginaAtual + 1));
    helper("prev-page", () => atualizarListaComentarios(paginaAtual - 1));

    // Interface (Toggle caixa de comentário)
    helper("btn-comentario", () => {
        const box = document.getElementById("box-comentario");
        if (box) box.style.display = box.style.display === "none" ? "block" : "none";
    });

    // Gatilho arquivo
    helper("btn-arquivo", () => document.getElementById("arquivo-csv")?.click());

    // Enviar Comentário
    helper("btn-enviar-comentario", async () => {
        const textarea = document.getElementById("texto-comentario");
        const resDiv = document.getElementById("resultado-comentario");
        if (!textarea || !textarea.value.trim()) return alert("Digite um comentário.");

        try {
            const data = await criarComentario(textarea.value);
            if (resDiv) {
                resDiv.innerHTML = `
                    <div class="result-box" style="margin-top:10px; flex-direction:column; height:auto; border-color: var(--success-color);">
                        <span class="resultado-badge ${(data.previsao || "").toLowerCase()}">${data.previsao}</span>
                        <small style="margin-top:5px; color: white;">Análise concluída com ${(data.probabilidade * 100).toFixed(2)}% de certeza.</small>
                    </div>
                `;
            }
            textarea.value = "";
            atualizarListaComentarios(0); // Recarrega a primeira página
        } catch (error) {
            alert("Erro ao analisar sentimento.");
        }
    });

    // Upload CSV
    const inputCsv = document.getElementById("arquivo-csv");
    if (inputCsv) {
        inputCsv.onchange = async (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const resLote = document.getElementById("resultado-lote");
            if (resLote) {
                resLote.style.display = "block";
                resLote.innerHTML = "Processando lote...";
            }

            try {
                await uploadCsv(file);
                if (resLote) resLote.innerHTML = "✅ Processamento concluído!";
                atualizarListaComentarios(0);
            } catch (error) {
                if (resLote) resLote.innerHTML = "❌ Erro no upload.";
            }
        };
    }

    // Estatísticas
    document.querySelectorAll(".btn-stats").forEach(btn => {
        btn.onclick = async () => {
            const data = await carregarStats(btn.dataset.qtd);
            atualizarDisplayStats(data);
        };
    });

    helper("btn-stats-custom", async () => {
        const qtd = document.getElementById("stats-custom")?.value;
        if (qtd > 0) {
            const data = await carregarStats(qtd);
            atualizarDisplayStats(data);
        }
    });

    // CRUD - Buscar
    helper("btn-buscar", async () => {
        const id = document.getElementById("buscar-id")?.value;
        if (!id) return;
        try {
            const data = await buscarComentario(id);
            const resBusca = document.getElementById("resultado-busca");
            if (resBusca) {
                resBusca.innerHTML = `
                    <div style="width:100%">
                        <span class="resultado-badge ${(data.previsao || "").toLowerCase()}">${data.previsao}</span>
                        <p style="font-size: 0.85rem; margin-top:8px; color: var(--text-primary)">${data.text}</p>
                    </div>
                `;
            }
        } catch (e) { alert("ID não encontrado."); }
    });

    // CRUD - Atualizar
    helper("btn-atualizar", async () => {
        const id = document.getElementById("atualizar-id")?.value;
        const text = document.getElementById("atualizar-texto")?.value;
        if (!id || !text) return alert("Preencha ID e texto.");

        await atualizarComentario(id, text);
        const resAtu = document.getElementById("resultado-atualizacao");
        if (resAtu) resAtu.innerText = "✅ Registro atualizado!";
        atualizarListaComentarios(paginaAtual);
    });

    // CRUD - Deletar
    helper("btn-deletar", async () => {
        const idInput = document.getElementById("deletar-id");
        if (!idInput || !idInput.value) return;

        if (confirm(`Deseja realmente excluir o registro #${idInput.value}?`)) {
            await deletarComentario(idInput.value);
            const msgDel = document.getElementById("mensagem-delete");
            if (msgDel) msgDel.innerText = "🗑️ Excluído com sucesso.";
            idInput.value = "";
            atualizarListaComentarios(paginaAtual);
        }
    });
}

function atualizarDisplayStats(data) {
    const resStats = document.getElementById("resultado-stats");
    if (resStats) {
        resStats.innerHTML = `
            <div style="display: flex; justify-content: space-around; width: 100%; padding: 10px 0;">
                <div><span class="resultado-badge positivo">Positivo</span> <strong style="display:block; text-align:center; margin-top:5px;">${data.positivo}%</strong></div>
                <div><span class="resultado-badge negativo">Negativo</span> <strong style="display:block; text-align:center; margin-top:5px;">${data.negativo}%</strong></div>
            </div>
        `;
    }
}