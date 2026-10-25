#!/usr/bin/env bash
# Script de teste alternativo ao Postman (Git Bash / Linux / WSL)
BASE=${BASE:-http://localhost:8080}
AUTH="cliente@mercado.com:cliente123"

echo "== Produtos"; curl -s "$BASE/api/produtos" | head -c 400; echo
echo "== Compra"
RESP=$(curl -s -u "$AUTH" -H "Content-Type: application/json" -d @"$(dirname "$0")/compra-exemplo.json" "$BASE/api/compras")
echo "$RESP"
ID=$(echo "$RESP" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)
echo "== Aguardando o Banco Tranquilo..."; sleep 3
curl -s -u "$AUTH" "$BASE/api/compras/$ID"; echo
