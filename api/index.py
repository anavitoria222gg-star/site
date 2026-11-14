from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
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
    return {"name":"NBA Stats API","usage":"/api/search?q=lebron"}

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
        stats=leaguedashplayerstats.LeagueDashPlayerStats(season=SEASON,per_mode_detailed="PerGame").get_data_frames()[0]
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
