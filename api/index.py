from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from nba_api.stats.static import players, teams
from nba_api.stats.endpoints import playergamelog, commonteamroster, leaguedashplayerstats
import pandas as pd

app = FastAPI(title="NBA Stats API")

SEASON = "2026-27"

def clean(value):
    if pd.isna(value):
        return None
    if hasattr(value, "item"):
        try: return value.item()
        except Exception: pass
    return value

def records(df):
    return [{k: clean(v) for k, v in row.items()} for row in df.to_dict(orient="records")]

@app.get("/")
def home():
    return HTMLResponse(content=`<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>NBA Stats</title><style>body{font-family:Arial,sans-serif;background:#0b1020;color:#fff;margin:0;padding:24px}main{max-width:900px;margin:auto}.search{display:flex;gap:8px}input{flex:1;padding:14px;border-radius:8px;border:0;font-size:16px}button{padding:14px 20px;border:0;border-radius:8px;background:#e85d04;color:#fff;font-weight:bold}#results{margin-top:24px;display:grid;gap:12px}.card{background:#151c33;padding:16px;border-radius:12px;cursor:pointer}.tag{color:#9fb3ff;font-size:12px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:10px}.stat{background:#151c33;padding:12px;border-radius:10px}</style></head><body><main><h1>🏀 NBA Stats</h1><p>Pesquise um jogador ou time da NBA.</p><div class="search"><input id="q" placeholder="Ex.: Lakers ou Stephen Curry"><button onclick="go()">Buscar</button></div><div id="results"></div></main><script>
const r=document.querySelector('#results');
async function api(url){
  const res=await fetch(url);
  const data=await res.json().catch(()=>({}));
  if(!res.ok) throw new Error(data.detail || 'Erro ao buscar dados');
  return data;
}
async function go(){
  const q=document.querySelector('#q').value.trim();
  if(!q)return;
  r.innerHTML='Buscando...';
  try{
    const d=await api('/api/search?q='+encodeURIComponent(q));
    r.innerHTML='';
    (d.players||[]).forEach(p=>r.innerHTML+=\`<div class="card" onclick="player(${p.id})"><span class="tag">JOGADOR</span><h3>${p.full_name}</h3></div>\`);
    (d.teams||[]).forEach(t=>r.innerHTML+=\`<div class="card" onclick="team(${t.id})"><span class="tag">TIME</span><h3>${t.full_name}</h3></div>\`);
    if(!r.innerHTML)r.innerHTML='Nenhum resultado encontrado.';
  }catch(e){r.innerHTML='<div class="card">❌ '+e.message+'</div>';}
}
async function player(id){
  r.innerHTML='Carregando jogador...';
  try{
    const d=await api('/api/player/'+id);
    let h='<h2>Últimos 5 jogos</h2><div class="grid">';
    for(const[k,v]of Object.entries(d.last5_average||{}))h+=\`<div class="stat"><b>${k}</b><br>${v}</div>\`;
    h+='</div>';r.innerHTML=h;
  }catch(e){r.innerHTML='<div class="card">❌ '+e.message+'</div>';}
}
async function team(id){
  r.innerHTML='Carregando time...';
  try{
    const d=await api('/api/team/'+id);
    let h='<h2>🏆 Líderes do time</h2>';
    for(const[k,list]of Object.entries(d.leaders||{})){
      h+=\`<div class="card"><h3>${k}</h3>\`+list.map(x=>\`<div>${x.PLAYER_NAME} — ${x[Object.keys(x).slice(-1)[0]]}</div>\`).join('')+'</div>';
    }
    r.innerHTML=h;
  }catch(e){r.innerHTML='<div class="card">❌ '+e.message+'</div>';}
}
</script></body></html>`)

@app.get("/api/search")
def search(q: str):
    query=q.strip().lower()
    ps=[p for p in players.get_players() if query in p["full_name"].lower()][:10]
    ts=[t for t in teams.get_teams() if query in t["full_name"].lower() or query in t["nickname"].lower()][:10]
    return {"players":ps,"teams":ts}

@app.get("/api/player/{player_id}")
def player(player_id: int):
    try:
        log=playergamelog.PlayerGameLog(player_id=player_id,season=SEASON).get_data_frames()[0]
        if log.empty: raise HTTPException(404,"Sem jogos nesta temporada")
        last5=log.head(5)
        stats={}
        for c in ["PTS","REB","AST","STL","BLK","FG3M","PF","TOV","MIN"]:
            if c in last5: stats[c]=round(float(last5[c].mean()),2)
        return {"player_id":player_id,"season":SEASON,"last5_average":stats,"games":records(last5)}
    except HTTPException: raise
    except Exception as e: raise HTTPException(502,f"Erro ao consultar dados NBA: {e}")

@app.get("/api/team/{team_id}")
def team(team_id: int):
    try:
        roster=commonteamroster.CommonTeamRoster(team_id=team_id,season=SEASON).get_data_frames()[0]
        stats=leaguedashplayerstats.LeagueDashPlayerStats(season=SEASON, team_id_nullable=team_id, per_mode_detailed="PerGame").get_data_frames()[0]
        team_stats=stats[stats["TEAM_ID"]==team_id].copy()
        if team_stats.empty: raise HTTPException(404,"Sem estatísticas para este time")
        categories={"points":"PTS","rebounds":"REB","assists":"AST","steals":"STL","blocks":"BLK","three_pointers":"FG3M","fouls":"PF","turnovers":"TOV"}
        leaders={}
        for name,col in categories.items():
            cols=[c for c in ["PLAYER_ID","PLAYER_NAME","GP",col] if c in team_stats.columns]
            leaders[name]=records(team_stats.sort_values(col,ascending=False)[cols].head(5))
        return {"team_id":team_id,"season":SEASON,"roster":records(roster),"leaders":leaders}
    except HTTPException: raise
    except Exception as e: raise HTTPException(502,f"Erro ao consultar dados NBA: {e}")
