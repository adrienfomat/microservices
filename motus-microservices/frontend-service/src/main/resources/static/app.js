// base urls of the three microservices
const API = {
    joueur: "http://localhost:8081",
    partie: "http://localhost:8082",
    score: "http://localhost:8083"
};
const SESSION_KEY = "motus.session";

const state = {
    joueur: null,        // { id, pseudo, email, admin }
    longueur: 6,
    partieNo: 0,
    game: null,
    timerId: null,
    seconds: 0
};

function el(id) { return document.getElementById(id); }

async function call(url, options) {
    const res = await fetch(url, options);
    if (!res.ok) {
        let message = "Erreur " + res.status;
        try {
            const body = await res.json();
            if (body && body.message) message = body.message;
        } catch (e) { /* pas de json */ }
        throw new Error(message);
    }
    if (res.status === 204) return null;
    return res.json();
}
function post(url, data) {
    return call(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(data) });
}

/* ================= Connexion / Inscription ================= */
el("tab-connexion").addEventListener("click", () => basculerAuth(true));
el("tab-inscription").addEventListener("click", () => basculerAuth(false));

function basculerAuth(connexion) {
    el("tab-connexion").classList.toggle("active", connexion);
    el("tab-inscription").classList.toggle("active", !connexion);
    el("form-connexion").hidden = !connexion;
    el("form-inscription").hidden = connexion;
    el("login-error").textContent = "";
}

el("btn-connexion").addEventListener("click", async () => {
    const identifiant = el("c-identifiant").value.trim();
    const motDePasse = el("c-motdepasse").value;
    if (!identifiant || !motDePasse) { el("login-error").textContent = "Remplissez les deux champs"; return; }
    try {
        const j = await post(API.joueur + "/joueurs/connexion", { identifiant, motDePasse });
        connecte(j);
    } catch (e) { el("login-error").textContent = e.message; }
});

el("btn-inscription").addEventListener("click", async () => {
    const pseudo = el("i-pseudo").value.trim();
    const email = el("i-email").value.trim();
    const motDePasse = el("i-motdepasse").value;
    const codeAdmin = el("i-code").value.trim();
    if (!pseudo || !email || !motDePasse) { el("login-error").textContent = "Pseudo, email et mot de passe obligatoires"; return; }
    try {
        const j = await post(API.joueur + "/joueurs/inscription", { pseudo, email, motDePasse, codeAdmin });
        connecte(j);
    } catch (e) { el("login-error").textContent = e.message; }
});

function connecte(joueur) {
    state.joueur = joueur;
    localStorage.setItem(SESSION_KEY, JSON.stringify(joueur));
    el("player-name").textContent = joueur.pseudo;
    el("admin-tag").hidden = !joueur.admin;
    el("btn-admin").hidden = !joueur.admin;
    el("overlay").hidden = true;
    el("app").hidden = false;
    construireMenuLongueur();
    chargerClassement();
    nouvelle(state.longueur);
}

el("btn-logout").addEventListener("click", () => {
    localStorage.removeItem(SESSION_KEY);
    location.reload();
});

// restore session on load
(function initSession() {
    const brut = localStorage.getItem(SESSION_KEY);
    if (brut) {
        try { connecte(JSON.parse(brut)); return; } catch (e) { /* session invalide */ }
    }
    el("overlay").hidden = false;
})();

/* ================= Choix de longueur ================= */
function construireMenuLongueur() {
    const menu = el("menu-len");
    menu.innerHTML = "";
    for (let n = 4; n <= 9; n++) {
        const b = document.createElement("button");
        b.textContent = "Mot de " + n + " lettres";
        b.addEventListener("click", () => {
            state.longueur = n;
            el("len-label").textContent = n;
            menu.hidden = true;
            nouvelle(n);
        });
        menu.appendChild(b);
    }
}
el("btn-mot").addEventListener("click", () => { el("menu-len").hidden = !el("menu-len").hidden; });
el("btn-refresh").addEventListener("click", () => nouvelle(state.longueur));

/* ================= Nouvelle partie ================= */
async function nouvelle(longueur) {
    if (!state.joueur) return;
    stopTimer();
    try {
        const p = await post(API.partie + "/parties", { joueurId: state.joueur.id, longueur });
        state.partieNo++;
        state.game = {
            id: p.partieId, longueur: p.longueur, essaisMax: p.essaisMax,
            premiere: p.premiereLettre.toUpperCase(), ligne: 0, col: 0,
            cells: [], locked: [], colors: [], found: new Array(p.longueur).fill(null), fini: false
        };
        el("partie-no").textContent = "Partie n" + state.partieNo;
        el("badge-len").textContent = "en " + p.longueur + " lettres";
        el("points").textContent = "0 Pts";
        el("message").textContent = "Le mot commence par " + state.game.premiere + ".";
        el("banner").hidden = true;
        construireGrille(p.essaisMax, p.longueur);
        demarrerLigne(0);
        startTimer();
    } catch (e) { el("message").textContent = e.message; }
}

function construireGrille(lignes, colonnes) {
    const board = el("board");
    board.innerHTML = "";
    const g = state.game;
    for (let r = 0; r < lignes; r++) {
        g.cells[r] = new Array(colonnes).fill("");
        g.locked[r] = new Array(colonnes).fill(false);
        g.colors[r] = new Array(colonnes).fill(null);
        const row = document.createElement("div");
        row.className = "board-row";
        for (let c = 0; c < colonnes; c++) {
            const cell = document.createElement("div");
            cell.className = "cell";
            row.appendChild(cell);
        }
        board.appendChild(row);
    }
}
function cellNode(r, c) { return el("board").children[r].children[c]; }

function demarrerLigne(r) {
    const g = state.game;
    for (let c = 0; c < g.longueur; c++) {
        if (c === 0) { g.cells[r][c] = g.premiere; g.locked[r][c] = true; }
        else if (g.found[c] != null) { g.cells[r][c] = g.found[c]; g.locked[r][c] = true; }
        else { g.cells[r][c] = ""; g.locked[r][c] = false; }
    }
    g.col = prochaineCase(r, 0);
    for (let c = 0; c < g.longueur; c++) dessinerCase(r, c);
}
function prochaineCase(r, depuis) {
    const g = state.game;
    let c = depuis;
    while (c < g.longueur && (g.locked[r][c] || g.cells[r][c] !== "")) c++;
    return c;
}
function dessinerCase(r, c) {
    const g = state.game;
    const node = cellNode(r, c);
    const val = g.cells[r][c];
    const color = g.colors[r][c];
    node.className = "cell";
    if (color === "BIEN_PLACE") { node.classList.add("bien"); node.textContent = val; return; }
    if (color === "MAL_PLACE") { node.classList.add("mal"); node.innerHTML = '<span class="disc">' + val + '</span>'; return; }
    if (color === "ABSENT") { node.classList.add("abs"); node.textContent = val; return; }
    if (val) node.textContent = val;
    else if (r === g.ligne) node.innerHTML = '<span class="dot"></span>';
    else node.textContent = "";
    if (r === g.ligne && !g.locked[r][c]) node.classList.add("active");
}

/* ================= Saisie clavier ================= */
document.addEventListener("keydown", e => {
    const g = state.game;
    const modaleOuverte = !el("modal-rules").hidden || !el("modal-stats").hidden || !el("modal-admin").hidden;
    if (!g || g.fini || !el("overlay").hidden || modaleOuverte) return;
    if (e.key === "Enter") { valider(); return; }
    if (e.key === "Backspace") { e.preventDefault(); effacer(); return; }
    if (/^[a-zA-Z]$/.test(e.key)) taper(e.key.toUpperCase());
});
function taper(lettre) {
    const g = state.game, r = g.ligne;
    let c = g.col;
    while (c < g.longueur && g.locked[r][c]) c++;
    if (c >= g.longueur) return;
    g.cells[r][c] = lettre;
    dessinerCase(r, c);
    g.col = prochaineCase(r, c + 1);
}
function effacer() {
    const g = state.game, r = g.ligne;
    let c = g.col - 1;
    while (c >= 0 && g.locked[r][c]) c--;
    if (c < 0) return;
    g.cells[r][c] = "";
    dessinerCase(r, c);
    g.col = c;
}
function ligneComplete(r) { return state.game.cells[r].every(v => v !== ""); }

async function valider() {
    const g = state.game, r = g.ligne;
    if (!ligneComplete(r)) { el("message").textContent = "Completez le mot"; return; }
    const mot = g.cells[r].join("");
    try {
        const rep = await post(API.partie + "/parties/" + g.id + "/essais", { mot });
        for (let c = 0; c < g.longueur; c++) {
            g.colors[r][c] = rep.resultat[c];
            if (rep.resultat[c] === "BIEN_PLACE") g.found[c] = g.cells[r][c];
            dessinerCase(r, c);
        }
        el("message").textContent = "";
        if (rep.statut !== "EN_COURS") terminer(rep);
        else { g.ligne++; demarrerLigne(g.ligne); }
    } catch (e) { el("message").textContent = e.message; }
}

function terminer(rep) {
    const g = state.game;
    g.fini = true;
    stopTimer();
    const banner = el("banner");
    banner.hidden = false;
    if (rep.statut === "GAGNEE") {
        const points = (rep.essaisRestants + 1) * 20 + g.longueur * 5;
        el("points").textContent = points + " Pts";
        banner.className = "banner win";
        banner.textContent = "Gagne ! Le mot etait " + rep.motMystere;
    } else {
        banner.className = "banner lose";
        banner.textContent = "Perdu. Le mot etait " + rep.motMystere;
    }
    chargerClassement();
}

/* ================= Minuteur ================= */
function startTimer() {
    state.seconds = 0;
    el("timer").textContent = "00:00";
    state.timerId = setInterval(() => {
        state.seconds++;
        const m = String(Math.floor(state.seconds / 60)).padStart(2, "0");
        const s = String(state.seconds % 60).padStart(2, "0");
        el("timer").textContent = m + ":" + s;
    }, 1000);
}
function stopTimer() { if (state.timerId) { clearInterval(state.timerId); state.timerId = null; } }

/* ================= Classement (auto, en bas) ================= */
async function chargerClassement() {
    const zone = el("lb-content");
    try {
        const lignes = await call(API.score + "/classement");
        if (!lignes.length) { zone.innerHTML = "<div class='lb-empty'>Aucun joueur classe pour l'instant.</div>"; return; }
        const medailles = ["\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49"];
        zone.innerHTML = lignes.map((l, i) => {
            const rang = i < 3
                ? "<div class='lb-rank medal'>" + medailles[i] + "</div>"
                : "<div class='lb-rank'>#" + (i + 1) + "</div>";
            const initiale = (l.pseudo || "?").charAt(0).toUpperCase();
            return "<div class='lb-row'>" + rang
                + "<div class='lb-ava'>" + initiale + "</div>"
                + "<div><div class='lb-name'>" + l.pseudo + "</div>"
                + "<div class='lb-sub'>" + l.partiesJouees + " parties &middot; ~" + l.pointsParPartie + " pts/partie</div></div>"
                + "<div class='lb-pts'><b>" + l.points + "</b> pts</div></div>";
        }).join("");
    } catch (e) { zone.innerHTML = "<div class='lb-empty'>" + e.message + "</div>"; }
}

/* ================= Modale stats perso ================= */
let statsData = { resultats: [], mots: [] };

el("btn-stats").addEventListener("click", ouvrirStats);
el("close-stats").addEventListener("click", () => { el("modal-stats").hidden = true; });
document.querySelectorAll(".stats-tab").forEach(t => t.addEventListener("click", () => {
    document.querySelectorAll(".stats-tab").forEach(x => x.classList.remove("active"));
    t.classList.add("active");
    afficherStats(t.dataset.stat);
}));

function dateDuJour() {
    const d = new Date();
    return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") + "-" + String(d.getDate()).padStart(2, "0");
}

async function ouvrirStats() {
    el("modal-stats").hidden = false;
    el("stats-body").innerHTML = "Chargement...";
    try {
        const resultats = await call(API.score + "/resultats?joueurId=" + state.joueur.id);
        const parties = await call(API.partie + "/parties?joueurId=" + state.joueur.id);
        const gagneesJour = resultats.filter(r => r.gagnee && (r.date || "").substring(0, 10) === dateDuJour()).length;
        el("stats-title").textContent = "\uD83D\uDCCA Vos " + gagneesJour + " parties gagnees ce jour";
        statsData.resultats = resultats;
        statsData.mots = parties.filter(p => p.statut === "GAGNEE" && p.motMystere).map(p => p.motMystere);
        document.querySelector(".stats-tab.active")
            ? afficherStats(document.querySelector(".stats-tab.active").dataset.stat)
            : afficherStats("scores");
    } catch (e) { el("stats-body").innerHTML = "<p>" + e.message + "</p>"; }
}

function afficherStats(onglet) {
    const body = el("stats-body");
    if (onglet === "mots") {
        if (!statsData.mots.length) { body.innerHTML = "<p>Aucun mot trouve pour l'instant.</p>"; return; }
        body.innerHTML = "<table><tr><th>#</th><th>Mot trouve</th></tr>"
            + statsData.mots.map((m, i) => "<tr><td>" + (i + 1) + "</td><td>" + m + "</td></tr>").join("") + "</table>";
    } else {
        if (!statsData.resultats.length) { body.innerHTML = "<p>Aucune partie jouee pour l'instant.</p>"; return; }
        body.innerHTML = "<table><tr><th>Date</th><th>Resultat</th><th>Essais</th><th>Points</th></tr>"
            + statsData.resultats.map(r => {
                const d = (r.date || "").replace("T", " ").substring(0, 16);
                const tag = r.gagnee ? "<span class='tag GAGNEE'>gagnee</span>" : "<span class='tag PERDUE'>perdue</span>";
                return "<tr><td>" + d + "</td><td>" + tag + "</td><td>" + r.nombreEssais + "</td><td>" + r.points + "</td></tr>";
            }).join("") + "</table>";
    }
}

/* ================= Modale administration (admin) ================= */
el("btn-admin").addEventListener("click", () => { el("modal-admin").hidden = false; chargerAdmin(); });
el("close-admin").addEventListener("click", () => { el("modal-admin").hidden = true; });
el("btn-search").addEventListener("click", chargerAdmin);

async function chargerAdmin() {
    const zone = el("admin-content");
    const params = [];
    if (el("f-joueur").value) params.push("joueurId=" + el("f-joueur").value);
    if (el("f-statut").value) params.push("statut=" + el("f-statut").value);
    if (el("f-du").value) params.push("du=" + el("f-du").value);
    if (el("f-au").value) params.push("au=" + el("f-au").value);
    const url = API.partie + "/parties" + (params.length ? "?" + params.join("&") : "");
    try {
        const parties = await call(url);
        if (!parties.length) { zone.innerHTML = "<p>Aucune partie.</p>"; return; }
        zone.innerHTML = "<table><tr><th>Partie</th><th>Joueur</th><th>Statut</th><th>Essais</th><th>Mot</th><th>Debut</th></tr>"
            + parties.map(p => {
                const mot = p.motMystere ? p.motMystere : "-";
                const debut = p.dateDebut ? p.dateDebut.replace("T", " ").substring(0, 16) : "";
                return "<tr><td>#" + p.id + "</td><td>#" + p.joueurId + "</td><td><span class='tag " + p.statut + "'>"
                    + p.statut + "</span></td><td>" + p.essaisUtilises + "/" + p.essaisMax + "</td><td>" + mot + "</td><td>" + debut + "</td></tr>";
            }).join("") + "</table>";
    } catch (e) { zone.innerHTML = "<p>" + e.message + "</p>"; }
}

/* ================= Regles ================= */
el("btn-help").addEventListener("click", () => { el("modal-rules").hidden = false; });
el("close-rules").addEventListener("click", () => { el("modal-rules").hidden = true; });

// close any modal by clicking the dark backdrop or pressing Escape
document.querySelectorAll(".modal").forEach(m => m.addEventListener("click", e => { if (e.target === m) m.hidden = true; }));
document.addEventListener("keydown", e => {
    if (e.key === "Escape") document.querySelectorAll(".modal").forEach(m => m.hidden = true);
});
