#!/bin/bash
# ============================================================
# Mercado Express - 3º Checkpoint DevOps (Pipelines CI/CD)
# Script de infraestrutura - Azure CLI (rodar no Azure Cloud Shell - Bash)
# ------------------------------------------------------------
# Cria, em sequência (pode ser executado de novo: pula o que já existe):
#   0) Registro dos provedores Microsoft.Sql, Microsoft.KeyVault e Microsoft.Web
#   1) Resource Group
#   2) Azure SQL Server + Banco (PaaS) + regra de firewall p/ serviços Azure
#   3) Azure Key Vault + segredos (URL, usuário e senha do banco)
#   4) App Service Plan Linux + Web App Java 17 (Java SE)
#   5) Identidade gerenciada do Web App com permissão de leitura no Key Vault
#   6) App Settings (variáveis de ambiente) apontando para o Key Vault
#
# A senha do banco NÃO fica em arquivo: é digitada na execução e
# gravada somente no Key Vault.
#
# USO:
#   chmod +x criar-infra.sh
#   ./criar-infra.sh
# ============================================================
set -e

# ────────────────────────────────────────────────────────────
# 1) VARIÁVEIS
# ────────────────────────────────────────────────────────────
RM="563454"
# Regiões permitidas pela assinatura Azure for Students (política "Allowed resource
# deployment regions"): eastus2, chilecentral, southcentralus, eastus, centralus.
# Em 10/2026 o Azure SQL não aceitava novos servidores em chilecentral/southcentralus
# e a cota de B1 era 0 em southcentralus, por isso SQL e App Service ficam em centralus.
LOCATION="southcentralus"        # Resource Group e Key Vault
SQL_LOCATION="centralus"         # Azure SQL Server + banco
APP_LOCATION="centralus"         # App Service Plan + Web App
RG="rg-mercadoexpress-rm$RM"
SQL_SERVER="sql-mercadoexpress-rm$RM"
SQL_DB="mercadoexpressdb"
SQL_ADMIN="adminmercado"
KV_NAME="kv-mercado-rm$RM"
PLAN="asp-mercadoexpress-rm$RM"
WEBAPP="app-mercadoexpress-rm$RM"
RUNTIME="JAVA:17-java17"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
step() { echo -e "\n${CYAN}===> $1${NC}"; }
ok()   { echo -e "${GREEN}[OK] $1${NC}"; }
warn() { echo -e "${YELLOW}[!]  $1${NC}"; }

# Senha do admin do banco: lida da variável SQL_PASSWORD ou digitada (não aparece na tela)
if [ -z "$SQL_PASSWORD" ]; then
  echo "Senha do admin do Azure SQL (min. 8 caracteres, com maiúscula, minúscula, número e símbolo):"
  read -s SQL_PASSWORD
  echo
fi

DB_URL="jdbc:sqlserver://${SQL_SERVER}.database.windows.net:1433;database=${SQL_DB};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

existe() { "$@" --output none 2>/dev/null; }

step "Assinatura ativa"
az account show --query "{assinatura:name, id:id}" -o table

# ────────────────────────────────────────────────────────────
# 1.1) PROVEDORES DE RECURSOS (assinaturas novas vêm sem registro)
# ────────────────────────────────────────────────────────────
for NS in Microsoft.Sql Microsoft.KeyVault Microsoft.Web; do
  STATE=$(az provider show --namespace "$NS" --query registrationState -o tsv 2>/dev/null)
  if [ "$STATE" != "Registered" ]; then
    step "Registrando provedor $NS (pode levar 1-3 min)"
    az provider register --namespace "$NS" --wait
  fi
  ok "Provedor $NS registrado"
done

# ────────────────────────────────────────────────────────────
# 2) RESOURCE GROUP
# ────────────────────────────────────────────────────────────
step "Criando Resource Group $RG em $LOCATION"
az group create --name "$RG" --location "$LOCATION" -o table
ok "Resource Group criado"

# ────────────────────────────────────────────────────────────
# 3) AZURE SQL (PaaS)
# ────────────────────────────────────────────────────────────
if existe az sql server show --name "$SQL_SERVER" --resource-group "$RG"; then
  warn "SQL Server $SQL_SERVER já existe - pulando"
else
  step "Criando Azure SQL Server $SQL_SERVER em $SQL_LOCATION (1-3 min)"
  az sql server create \
    --name "$SQL_SERVER" \
    --resource-group "$RG" \
    --location "$SQL_LOCATION" \
    --admin-user "$SQL_ADMIN" \
    --admin-password "$SQL_PASSWORD" \
    -o table
  ok "SQL Server criado"
fi

step "Liberando acesso dos serviços Azure (App Service) ao SQL Server"
az sql server firewall-rule create \
  --resource-group "$RG" \
  --server "$SQL_SERVER" \
  --name "AllowAzureServices" \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0 \
  -o table
ok "Firewall configurado"

if existe az sql db show --name "$SQL_DB" --server "$SQL_SERVER" --resource-group "$RG"; then
  warn "Banco $SQL_DB já existe - pulando"
else
  step "Criando banco $SQL_DB (camada Basic)"
  az sql db create \
    --resource-group "$RG" \
    --server "$SQL_SERVER" \
    --name "$SQL_DB" \
    --edition Basic \
    --capacity 5 \
    --max-size 2GB \
    --backup-storage-redundancy Geo \
    -o table
  ok "Banco criado"
fi

# ────────────────────────────────────────────────────────────
# 4) KEY VAULT (dados sensíveis)
# ────────────────────────────────────────────────────────────
if existe az keyvault show --name "$KV_NAME" --resource-group "$RG"; then
  warn "Key Vault $KV_NAME já existe - pulando"
else
  step "Criando Key Vault $KV_NAME"
  az keyvault create \
    --name "$KV_NAME" \
    --resource-group "$RG" \
    --location "$LOCATION" \
    --enable-rbac-authorization false \
    -o table
  ok "Key Vault criado"
fi

step "Gravando segredos do banco no Key Vault"
az keyvault secret set --vault-name "$KV_NAME" --name "db-url"      --value "$DB_URL"       --output none
az keyvault secret set --vault-name "$KV_NAME" --name "db-username" --value "$SQL_ADMIN"    --output none
az keyvault secret set --vault-name "$KV_NAME" --name "db-password" --value "$SQL_PASSWORD" --output none
ok "Segredos db-url, db-username e db-password gravados"

# ────────────────────────────────────────────────────────────
# 5) APP SERVICE (Web App Java 17)
# ────────────────────────────────────────────────────────────
if existe az appservice plan show --name "$PLAN" --resource-group "$RG"; then
  warn "App Service Plan $PLAN já existe - pulando"
else
  step "Criando App Service Plan Linux $PLAN (B1) em $APP_LOCATION"
  az appservice plan create \
    --name "$PLAN" \
    --resource-group "$RG" \
    --location "$APP_LOCATION" \
    --is-linux \
    --sku B1 \
    -o table
  ok "App Service Plan criado"
fi

if existe az webapp show --name "$WEBAPP" --resource-group "$RG"; then
  warn "Web App $WEBAPP já existe - pulando"
else
  step "Criando Web App $WEBAPP (runtime $RUNTIME)"
  az webapp create \
    --name "$WEBAPP" \
    --resource-group "$RG" \
    --plan "$PLAN" \
    --runtime "$RUNTIME" \
    -o table
  ok "Web App criado"
fi

# ────────────────────────────────────────────────────────────
# 6) IDENTIDADE GERENCIADA + PERMISSÃO NO KEY VAULT
# ────────────────────────────────────────────────────────────
step "Habilitando identidade gerenciada do Web App"
PRINCIPAL_ID=$(az webapp identity assign --name "$WEBAPP" --resource-group "$RG" --query principalId -o tsv)
ok "Identidade: $PRINCIPAL_ID"

step "Permitindo que o Web App leia os segredos do Key Vault"
az keyvault set-policy \
  --name "$KV_NAME" \
  --object-id "$PRINCIPAL_ID" \
  --secret-permissions get list \
  --output none
ok "Política de acesso criada"

# ────────────────────────────────────────────────────────────
# 7) VARIÁVEIS DE AMBIENTE (App Settings) + CONFIGURAÇÃO
# ────────────────────────────────────────────────────────────
step "Configurando variáveis de ambiente do Web App (Key Vault References)"
az webapp config appsettings set \
  --name "$WEBAPP" \
  --resource-group "$RG" \
  --settings \
    "DB_URL=@Microsoft.KeyVault(VaultName=${KV_NAME};SecretName=db-url)" \
    "DB_USERNAME=@Microsoft.KeyVault(VaultName=${KV_NAME};SecretName=db-username)" \
    "DB_PASSWORD=@Microsoft.KeyVault(VaultName=${KV_NAME};SecretName=db-password)" \
    "RABBIT_LISTENERS=false" \
    "SERVER_PORT=80" \
  --output none
ok "App Settings configurados"

step "Configurando comando de inicialização, Always On e logs"
az webapp config set \
  --name "$WEBAPP" \
  --resource-group "$RG" \
  --startup-file "java -jar /home/site/wwwroot/app.jar" \
  --always-on true \
  --output none
az webapp log config \
  --name "$WEBAPP" \
  --resource-group "$RG" \
  --application-logging filesystem \
  --docker-container-logging filesystem \
  --level information \
  --output none
ok "Configuração concluída"

# ────────────────────────────────────────────────────────────
# 8) RESUMO
# ────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}=============================================================${NC}"
echo -e "${GREEN}  INFRAESTRUTURA CRIADA - aguardando a Pipeline de CI/CD${NC}"
echo -e "${GREEN}=============================================================${NC}"
echo -e "Resource Group : ${CYAN}$RG${NC}"
echo -e "SQL Server     : ${CYAN}$SQL_SERVER.database.windows.net${NC}"
echo -e "Banco          : ${CYAN}$SQL_DB${NC}  (usuário: $SQL_ADMIN)"
echo -e "Key Vault      : ${CYAN}$KV_NAME${NC}"
echo -e "Web App        : ${CYAN}$WEBAPP${NC}"
echo -e "URL do App     : ${CYAN}https://$WEBAPP.azurewebsites.net${NC}"
echo ""
warn "Próximo passo: rodar o script.sql no Query editor do banco $SQL_DB"
echo -e "${GREEN}=============================================================${NC}"