import os
import time
from typing import Any

import requests
from fastapi import FastAPI, HTTPException, Query
from nba_api.stats.static import players, teams

app = FastAPI(title="NBA Stats")

RSA_BASE = "https://www.realtimesportsapi.com/api/v1"
CACHE_TTL = 60
_cache: dict[str, tuple[float, Any]] = {}


def get_api_key() -> str:
    key = os.getenv("RSA_API_KEY")
    if not key:
        raise HTTPException(
            status_code=500,
            detail="RSA_API_KEY não configurada nas variáveis de ambiente da Vercel."
        )
    return key


def rsa_get(path: str, params: dict | None = None) -> Any:
    cache_key = path + "?" + str(sorted((params or {}).items()))
    cached = _cache.get(cache_key)
    now = time.time()

    if cached and now - cached[0] < CACHE_TTL:
        return cached[1]

    try:
        response = requests.get(
            f"{RSA_BASE}{path}",
            headers={
                "Authorization": f"Bearer {get_api_key()}",
                "Accept": "application/json",
            },
            params=params,
            timeout=12,
        )
    except requests.RequestException as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Não foi possível conectar à Realtime Sports API: {exc}",
        )

    try:
        payload = response.json()
    except ValueError:
        raise HTTPException(
            status_code=502,
            detail=f"Resposta inválida da Realtime Sports API (HTTP {response.status_code}).",
        )

    if not response.ok or not payload.get("success", False):
        error = payload.get("error", {}) if isinstance(payload, dict) else {}
        message = error.get("message") or "Erro ao consultar a Realtime Sports API."
        raise HTTPException(status_code=response.status_code or 502, detail=message)

    data = payload.get("data")
    _cache[cache_key] = (now, data)
    return data


def normalize_event(event: dict) -> dict:
    return {
        "id": event.get("id"),
        "name": event.get("name"),
        "shortName": event.get("shortName"),
        "date": event.get("date"),
        "status": event.get("status", {}),
        "homeTeam": event.get("homeTeam", {}),
        "awayTeam": event.get("awayTeam", {}),
        "venue": event.get("venue", {}),
        "season": event.get("season", {}),
        "broadcasts": event.get("broadcasts", []),
    }


@app.get("/api/health")
def health():
    return {
        "ok": True,
        "provider": "Realtime Sports API",
        "api_key_configured": bool(os.getenv("RSA_API_KEY")),
    }


@app.get("/api/games")
def games(limit: int = Query(default=20, ge=1, le=100)):
    data = rsa_get(
        "/sports/basketball/leagues/nba/events",
        {"limit": limit},
    )
    return {"games": [normalize_event(event) for event in (data or [])]}


@app.get("/api/search")
def search(q: str = Query(min_length=1)):
    query = q.strip().lower()

    player_results = [
        {
            "id": p["id"],
            "full_name": p["full_name"],
            "first_name": p.get("first_name"),
            "last_name": p.get("last_name"),
            "active": p.get("is_active", True),
        }
        for p in players.get_players()
        if query in p["full_name"].lower()
    ][:10]

    team_results = [
        {
            "id": t["id"],
            "full_name": t["full_name"],
            "city": t.get("city"),
            "nickname": t.get("nickname"),
            "abbreviation": t.get("abbreviation"),
        }
        for t in teams.get_teams()
        if query in t["full_name"].lower()
        or query in t.get("nickname", "").lower()
        or query == t.get("abbreviation", "").lower()
    ][:10]

    # Complementa os times com os próximos jogos vindos da API em tempo real.
    try:
        events = rsa_get("/sports/basketball/leagues/nba/events", {"limit": 50})
    except HTTPException:
        events = []

    matched_games = []
    for event in events or []:
        text = " ".join([
            str(event.get("name", "")),
            str(event.get("shortName", "")),
            str(event.get("homeTeam", {}).get("name", "")),
            str(event.get("awayTeam", {}).get("name", "")),
            str(event.get("homeTeam", {}).get("abbreviation", "")),
            str(event.get("awayTeam", {}).get("abbreviation", "")),
        ]).lower()
        if query in text:
            matched_games.append(normalize_event(event))

    return {
        "players": player_results,
        "teams": team_results,
        "games": matched_games[:10],
    }


@app.get("/api/player/{player_id}")
def player(player_id: int):
    found = next((p for p in players.get_players() if p["id"] == player_id), None)
    if not found:
        raise HTTPException(status_code=404, detail="Jogador não encontrado.")

    # A fonte anterior (stats.nba.com) foi removida para evitar timeout/403.
    # Este endpoint mantém a identificação do jogador até conectarmos um endpoint
    # específico de estatísticas de atletas da nova API.
    return {
        "player": {
            "id": found["id"],
            "full_name": found["full_name"],
            "active": found.get("is_active", True),
        },
        "stats_available": False,
        "message": "Estatísticas individuais ainda dependem de um endpoint de atletas. O site não usa mais stats.nba.com.",
    }


@app.get("/api/team/{team_id}")
def team(team_id: int):
    found = next((t for t in teams.get_teams() if t["id"] == team_id), None)
    if not found:
        raise HTTPException(status_code=404, detail="Time não encontrado.")

    abbreviation = found.get("abbreviation", "").upper()
    events = rsa_get("/sports/basketball/leagues/nba/events", {"limit": 100})

    related = []
    for event in events or []:
        home = event.get("homeTeam", {})
        away = event.get("awayTeam", {})
        if abbreviation in {
            str(home.get("abbreviation", "")).upper(),
            str(away.get("abbreviation", "")).upper(),
        }:
            related.append(normalize_event(event))

    return {
        "team": {
            "id": found["id"],
            "full_name": found["full_name"],
            "city": found.get("city"),
            "nickname": found.get("nickname"),
            "abbreviation": abbreviation,
        },
        "games": related,
    }
