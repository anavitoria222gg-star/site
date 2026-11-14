# NBA Stats 🏀

Aplicação web para pesquisar jogadores e times da NBA.

## Recursos
- Busca por jogador ou time
- Estatísticas da temporada
- Médias dos últimos 5 jogos
- Elenco e líderes do time
- Pontos, rebotes, assistências, roubos, bloqueios, bolas de 3, faltas e turnovers
- Próximos jogos
- Probabilidade estimada de vitória

Os dados são buscados no backend Python usando endpoints públicos utilizados pelo NBA.com através da biblioteca `nba_api`.

## Rodar localmente
```bash
pip install -r requirements.txt
uvicorn api.index:app --reload
```
