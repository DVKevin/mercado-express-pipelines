#!/bin/bash
# ============================================================
# Mercado Express - remove TODOS os recursos do Checkpoint
# Rodar SOMENTE depois da nota/correção (Azure Cloud Shell - Bash)
# ============================================================
RM="563454"
LOCATION="southcentralus"
RG="rg-mercadoexpress-rm$RM"
KV_NAME="kv-mercado-rm$RM"

echo "Apagando o Resource Group $RG (pode levar alguns minutos)..."
az group delete --name "$RG" --yes

echo "Removendo definitivamente o Key Vault excluído (soft-delete)..."
az keyvault purge --name "$KV_NAME" --location "$LOCATION" || true

echo "Recursos removidos."