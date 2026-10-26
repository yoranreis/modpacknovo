import json
import os

base_dir = r"c:\Users\yurim\OneDrive\Desktop\caosmodpack\visual_studies"
json_path = os.path.join(base_dir, "CATALOGO_ASSETS.json")
html_path = os.path.join(base_dir, "index.html")

with open(json_path, "r", encoding="utf-8") as f:
    catalog_data = json.load(f)

assets_json = json.dumps(catalog_data["assets"], ensure_ascii=False)

html_content = f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Caos Arcana — Galeria Visual de Estudos (Server do Caos)</title>
  <style>
    :root {{
      --bg-dark: #121316;
      --bg-panel: #1a1c22;
      --bg-card: #22252e;
      --border-color: #383c48;
      --accent-gold: #e5b95c;
      --accent-gold-hover: #ffd700;
      --text-main: #e2e4e9;
      --text-muted: #9aa0ae;
      --badge-decidido: #2e7d32;
      --badge-proposta: #d97706;
      --badge-referencia: #0284c7;
      --badge-pendente: #dc2626;
      --badge-concluido: #15803d;
      --badge-cota: #b45309;
    }}
    * {{
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }}
    body {{
      background-color: var(--bg-dark);
      color: var(--text-main);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      line-height: 1.5;
      padding: 20px;
    }}
    header {{
      background: linear-gradient(180deg, #252834 0%, #1a1c22 100%);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 24px 32px;
      margin-bottom: 24px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.4);
    }}
    .header-title {{
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }}
    h1 {{
      color: var(--accent-gold);
      font-size: 26px;
      letter-spacing: 0.5px;
      text-transform: uppercase;
      text-shadow: 0 2px 4px rgba(0,0,0,0.5);
    }}
    .subtitle {{
      color: var(--text-muted);
      font-size: 14px;
      margin-top: 4px;
    }}
    .metrics-bar {{
      display: flex;
      gap: 16px;
      margin-top: 18px;
      flex-wrap: wrap;
    }}
    .metric-badge {{
      background-color: #262934;
      border: 1px solid #3d4252;
      border-radius: 6px;
      padding: 8px 14px;
      font-size: 13px;
    }}
    .metric-badge strong {{
      color: var(--accent-gold);
      font-size: 16px;
    }}
    .alert-cota {{
      margin-top: 16px;
      background: rgba(180, 83, 9, 0.2);
      border-left: 4px solid var(--badge-cota);
      padding: 10px 16px;
      border-radius: 4px;
      font-size: 13px;
      color: #fed7aa;
    }}

    /* Control Panel */
    .controls {{
      background-color: var(--bg-panel);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 18px 24px;
      margin-bottom: 24px;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }}
    .search-row {{
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }}
    .search-box {{
      flex: 1;
      min-width: 250px;
      padding: 10px 14px;
      background: #111215;
      border: 1px solid var(--border-color);
      border-radius: 6px;
      color: #fff;
      font-size: 14px;
    }}
    .search-box:focus {{
      outline: 1px solid var(--accent-gold);
      border-color: var(--accent-gold);
    }}
    .filter-group {{
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
    }}
    .filter-label {{
      font-size: 12px;
      text-transform: uppercase;
      color: var(--text-muted);
      font-weight: bold;
      margin-right: 6px;
    }}
    .filter-btn {{
      background: #252834;
      border: 1px solid #3d4252;
      color: var(--text-main);
      padding: 6px 12px;
      border-radius: 4px;
      font-size: 12px;
      cursor: pointer;
      transition: all 0.2s;
    }}
    .filter-btn:hover {{
      border-color: var(--accent-gold);
      color: #fff;
    }}
    .filter-btn.active {{
      background-color: var(--accent-gold);
      color: #000;
      font-weight: bold;
      border-color: var(--accent-gold);
    }}

    /* Grid */
    .gallery-grid {{
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 20px;
    }}
    .card {{
      background-color: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
      display: flex;
      flex-direction: column;
    }}
    .card:hover {{
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0,0,0,0.6);
      border-color: var(--accent-gold);
    }}
    .card-thumb-container {{
      width: 100%;
      height: 220px;
      background-color: #121316;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
    }}
    .card-thumb {{
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      image-rendering: auto;
    }}
    .card-thumb.pixel-art {{
      image-rendering: pixelated;
    }}
    .card-body {{
      padding: 14px 16px;
      display: flex;
      flex-direction: column;
      flex: 1;
      gap: 8px;
    }}
    .card-category {{
      font-size: 11px;
      text-transform: uppercase;
      color: var(--text-muted);
      letter-spacing: 0.5px;
    }}
    .card-title {{
      font-size: 15px;
      font-weight: 600;
      color: #fff;
      line-height: 1.3;
    }}
    .card-meta {{
      font-size: 12px;
      color: var(--text-muted);
      display: flex;
      justify-content: space-between;
      margin-top: auto;
      padding-top: 8px;
      border-top: 1px solid #2e3340;
    }}
    .badge {{
      display: inline-block;
      padding: 3px 8px;
      border-radius: 4px;
      font-size: 10px;
      font-weight: bold;
      text-transform: uppercase;
    }}
    .badge-decidido {{ background-color: var(--badge-decidido); color: #fff; }}
    .badge-proposta {{ background-color: var(--badge-proposta); color: #fff; }}
    .badge-referencia {{ background-color: var(--badge-referencia); color: #fff; }}
    .badge-concluido {{ background-color: var(--badge-concluido); color: #fff; }}
    .badge-cota {{ background-color: var(--badge-cota); color: #fff; }}

    /* Modal */
    .modal-overlay {{
      display: none;
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background-color: rgba(0, 0, 0, 0.85);
      backdrop-filter: blur(4px);
      z-index: 1000;
      justify-content: center;
      align-items: center;
      padding: 20px;
    }}
    .modal-content {{
      background-color: var(--bg-panel);
      border: 1px solid var(--accent-gold);
      border-radius: 10px;
      max-width: 950px;
      width: 100%;
      max-height: 90vh;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      box-shadow: 0 10px 40px rgba(0,0,0,0.8);
      position: relative;
    }}
    .modal-close {{
      position: absolute;
      top: 14px;
      right: 18px;
      background: none;
      border: none;
      color: #fff;
      font-size: 26px;
      cursor: pointer;
      line-height: 1;
    }}
    .modal-close:hover {{ color: var(--accent-gold); }}
    .modal-layout {{
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
      padding: 28px;
    }}
    @media (max-width: 768px) {{
      .modal-layout {{ grid-template-columns: 1fr; }}
    }}
    .modal-image-col {{
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #111215;
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 16px;
    }}
    .modal-img {{
      max-width: 100%;
      max-height: 480px;
      object-fit: contain;
    }}
    .modal-btn {{
      margin-top: 14px;
      padding: 8px 18px;
      background-color: var(--accent-gold);
      color: #000;
      font-weight: bold;
      border-radius: 4px;
      text-decoration: none;
      font-size: 13px;
    }}
    .modal-btn:hover {{ background-color: var(--accent-gold-hover); }}
    .modal-info-col {{
      display: flex;
      flex-direction: column;
      gap: 12px;
    }}
    .modal-title {{
      font-size: 20px;
      color: var(--accent-gold);
    }}
    .modal-field {{
      font-size: 13px;
      border-bottom: 1px solid #2a2e3a;
      padding-bottom: 6px;
    }}
    .modal-field strong {{
      color: var(--text-muted);
      display: block;
      font-size: 11px;
      text-transform: uppercase;
      margin-bottom: 2px;
    }}
  </style>
</head>
<body>

  <header>
    <div class="header-title">
      <div>
        <h1>Caos Arcana — Biblioteca Visual de Estudos</h1>
        <div class="subtitle">Server do Caos • RPG Mágico Medieval inspirado em Skyrim no Minecraft</div>
      </div>
    </div>
    <div class="metrics-bar">
      <div class="metric-badge">Total Ativos Catalogados: <strong id="metric-total">0</strong></div>
      <div class="metric-badge">Assets Concluídos (PNG/Voxel/Pixel Art): <strong id="metric-concluidos">0</strong></div>
      <div class="metric-badge">Pendentes de Cota (~4h): <strong id="metric-pendentes">0</strong></div>
    </div>
    <div class="alert-cota">
      <strong>Registro de Limite de Cota (Checkpoint):</strong> O modelo neural de concept art atingiu o limite temporário de requisições (HTTP 429 - RESOURCE_EXHAUSTED). Todos os 12 conceitos gerados foram convertidos para PNG em alta resolução e integrados aos estudos espaciais, diagramas táticos, mockups de interface (GUI) e ao conjunto de texturas e ícones de inventário 32x32 em pixel art genuíno. Os itens pendentes estão plenamente documentados com especificações técnicas e aguardam o término do período de cooldown para geração dos conceitos complementares.
    </div>
  </header>

  <div class="controls">
    <div class="search-row">
      <input type="text" id="searchInput" class="search-box" placeholder="Buscar por nome, id, raça, escola ou palavra-chave...">
    </div>
    <div class="filter-group" id="categoryFilters">
      <span class="filter-label">Categorias:</span>
      <button class="filter-btn active" data-category="all">Todas</button>
    </div>
    <div class="filter-group" id="statusFilters">
      <span class="filter-label">Estado de Decisão:</span>
      <button class="filter-btn active" data-status="all">Todos</button>
      <button class="filter-btn" data-status="Decidido">Decidido</button>
      <button class="filter-btn" data-status="Base proposta">Base Proposta</button>
      <button class="filter-btn" data-status="Referencia visual proposta">Referência Proposta</button>
    </div>
    <div class="filter-group" id="prodFilters">
      <span class="filter-label">Produção:</span>
      <button class="filter-btn active" data-prod="all">Todos</button>
      <button class="filter-btn" data-prod="Concluido">Concluídos</button>
      <button class="filter-btn" data-prod="Pendente">Pendentes de Cota</button>
    </div>
  </div>

  <div class="gallery-grid" id="galleryGrid"></div>

  <!-- Modal Details -->
  <div class="modal-overlay" id="modalOverlay">
    <div class="modal-content">
      <button class="modal-close" id="modalClose">&times;</button>
      <div class="modal-layout">
        <div class="modal-image-col">
          <img src="" alt="" class="modal-img" id="modalImg">
          <a href="" target="_blank" class="modal-btn" id="modalBtn">Abrir Arquivo em Tamanho Real</a>
        </div>
        <div class="modal-info-col">
          <div class="modal-title" id="modalTitle"></div>
          <div class="modal-field">
            <strong>ID e Categoria</strong>
            <span id="modalIdCategory"></span>
          </div>
          <div class="modal-field">
            <strong>Raça / Escola Associada</strong>
            <span id="modalRaceSchool"></span>
          </div>
          <div class="modal-field">
            <strong>Status de Decisão & Produção</strong>
            <span id="modalStatus"></span>
          </div>
          <div class="modal-field">
            <strong>Seção de Origem na Documentação</strong>
            <span id="modalSection"></span>
          </div>
          <div class="modal-field">
            <strong>Resolução e Tipo de Entrega</strong>
            <span id="modalResType"></span>
          </div>
          <div class="modal-field">
            <strong>Descrição do Estudo</strong>
            <span id="modalDesc"></span>
          </div>
          <div class="modal-field">
            <strong>Dependências Técnicas (Minecraft / Fabric)</strong>
            <span id="modalDeps"></span>
          </div>
          <div class="modal-field">
            <strong>Observações de Design e Contrajogo</strong>
            <span id="modalObs"></span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <script>
    const assetsData = {assets_json};

    let currentCategory = 'all';
    let currentStatus = 'all';
    let currentProd = 'all';
    let searchQuery = '';

    // Initialize metrics
    document.getElementById('metric-total').textContent = assetsData.length;
    document.getElementById('metric-concluidos').textContent = assetsData.filter(a => a.estado_producao === 'Concluido').length;
    document.getElementById('metric-pendentes').textContent = assetsData.filter(a => a.estado_producao.includes('Pendente')).length;

    // Build category filter buttons
    const categories = ['all', ...new Set(assetsData.map(a => a.categoria))];
    const catContainer = document.getElementById('categoryFilters');
    catContainer.innerHTML = '<span class="filter-label">Categorias:</span>';
    categories.forEach(cat => {{
      const btn = document.createElement('button');
      btn.className = `filter-btn ${{cat === 'all' ? 'active' : ''}}`;
      btn.dataset.category = cat;
      btn.textContent = cat === 'all' ? 'Todas' : cat;
      btn.addEventListener('click', () => {{
        catContainer.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentCategory = cat;
        renderGrid();
      }});
      catContainer.appendChild(btn);
    }});

    // Status filter buttons
    document.querySelectorAll('#statusFilters .filter-btn').forEach(btn => {{
      btn.addEventListener('click', () => {{
        document.querySelectorAll('#statusFilters .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentStatus = btn.dataset.status;
        renderGrid();
      }});
    }});

    // Prod filter buttons
    document.querySelectorAll('#prodFilters .filter-btn').forEach(btn => {{
      btn.addEventListener('click', () => {{
        document.querySelectorAll('#prodFilters .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentProd = btn.dataset.prod;
        renderGrid();
      }});
    }});

    // Search input
    document.getElementById('searchInput').addEventListener('input', (e) => {{
      searchQuery = e.target.value.toLowerCase();
      renderGrid();
    }});

    function renderGrid() {{
      const grid = document.getElementById('galleryGrid');
      grid.innerHTML = '';

      const filtered = assetsData.filter(item => {{
        const matchCat = currentCategory === 'all' || item.categoria === currentCategory;
        const matchStatus = currentStatus === 'all' || item.estado_decisao === currentStatus;
        const matchProd = currentProd === 'all' || 
          (currentProd === 'Concluido' && item.estado_producao === 'Concluido') ||
          (currentProd === 'Pendente' && item.estado_producao.includes('Pendente'));
        
        const q = searchQuery;
        const matchSearch = !q || 
          item.nome.toLowerCase().includes(q) ||
          item.id.toLowerCase().includes(q) ||
          item.raca_escola.toLowerCase().includes(q) ||
          item.categoria.toLowerCase().includes(q) ||
          item.descricao.toLowerCase().includes(q);

        return matchCat && matchStatus && matchProd && matchSearch;
      }});

      filtered.forEach(asset => {{
        const card = document.createElement('div');
        card.className = 'card';
        
        const isPixel = asset.resolucao === '32x32';
        const thumbSrc = asset.caminho_thumbnail;
        const isPending = asset.estado_producao.includes('Pendente');

        let statusClass = 'badge-decidido';
        if (asset.estado_decisao === 'Base proposta') statusClass = 'badge-proposta';
        if (asset.estado_decisao === 'Referencia visual proposta') statusClass = 'badge-referencia';

        card.innerHTML = `
          <div class="card-thumb-container">
            <img src="${{thumbSrc}}" alt="${{asset.nome}}" class="card-thumb ${{isPixel ? 'pixel-art' : ''}}">
          </div>
          <div class="card-body">
            <div class="card-category">${{asset.categoria}}</div>
            <div class="card-title">${{asset.nome}}</div>
            <div>
              <span class="badge ${{statusClass}}">${{asset.estado_decisao}}</span>
              <span class="badge ${{isPending ? 'badge-cota' : 'badge-concluido'}}">${{isPending ? 'Pendente Cota' : 'Concluído'}}</span>
            </div>
            <div class="card-meta">
              <span>${{asset.tipo_entrega}}</span>
              <span>${{asset.resolucao}}</span>
            </div>
          </div>
        `;
        card.addEventListener('click', () => openModal(asset));
        grid.appendChild(card);
      }});
    }}

    // Modal
    const modal = document.getElementById('modalOverlay');
    document.getElementById('modalClose').addEventListener('click', () => modal.style.display = 'none');
    modal.addEventListener('click', (e) => {{
      if (e.target === modal) modal.style.display = 'none';
    }});

    function openModal(asset) {{
      const isPending = asset.estado_producao.includes('Pendente');
      const imgSrc = isPending ? asset.caminho_thumbnail : asset.caminho_relativo;
      const modalImg = document.getElementById('modalImg');
      modalImg.src = imgSrc;
      modalImg.className = 'modal-img ' + (asset.resolucao === '32x32' ? 'pixel-art' : '');
      
      const modalBtn = document.getElementById('modalBtn');
      if (isPending) {{
        modalBtn.style.display = 'none';
      }} else {{
        modalBtn.style.display = 'inline-block';
        modalBtn.href = asset.caminho_relativo;
      }}

      document.getElementById('modalTitle').textContent = asset.nome;
      document.getElementById('modalIdCategory').textContent = `${{asset.id}} • [${{asset.categoria}}]`;
      document.getElementById('modalRaceSchool').textContent = asset.raca_escola;
      document.getElementById('modalStatus').innerHTML = `
        <span class="badge">${{asset.estado_decisao}}</span> 
        <span class="badge">${{asset.estado_producao}}</span>
      `;
      document.getElementById('modalSection').textContent = asset.secao_origem;
      document.getElementById('modalResType').textContent = `${{asset.tipo_entrega}} • Resolução: ${{asset.resolucao}}`;
      document.getElementById('modalDesc').textContent = asset.descricao;
      document.getElementById('modalDeps').textContent = asset.dependencias_tecnicas;
      document.getElementById('modalObs').textContent = asset.observacoes;

      modal.style.display = 'flex';
    }}

    renderGrid();
  </script>
</body>
</html>
"""

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Gallery HTML generated successfully: {html_path}")
