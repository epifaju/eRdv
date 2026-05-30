# Démarre eRDV avec Docker Compose : .env, JWT_SECRET, build et lancement.
# Usage (depuis n'importe où) :
#   .\scripts\start-docker.ps1
#   .\scripts\start-docker.ps1 -RefreshEnv   # recopie .env.example vers .env avant génération JWT

param(
    [switch] $RefreshEnv
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"
$example = Join-Path $root ".env.example"

Set-Location $root
Write-Host "Répertoire de travail : $root"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker n'est pas installé ou n'est pas dans le PATH."
}

Write-Host "`n[1/4] Fichier .env et JWT_SECRET..."
if (-not (Test-Path $example)) {
    Write-Error ".env.example introuvable à la racine du projet."
}
if ($RefreshEnv -or -not (Test-Path $envFile)) {
    Copy-Item $example $envFile -Force
    Write-Host "  .env créé ou mis à jour depuis .env.example"
} else {
    Write-Host "  .env existant conservé (utilisez -RefreshEnv pour repartir de .env.example)"
}
& (Join-Path $PSScriptRoot "generate-docker-env.ps1")
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "`n[2/4] Construction des images..."
docker compose build
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "`n[3/4] Démarrage des conteneurs..."
docker compose up -d
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "`n[4/4] État des services :"
docker compose ps

Write-Host @"

Application démarrée.

  Frontend     : http://localhost:3001
  API backend  : http://localhost:8084/api
  Santé API    : http://localhost:8084/api/actuator/health
  PostgreSQL   : localhost:5436

Comptes de démo (si APP_SEED_DEMO_USERS=true) :
  admin@erdv.com / admin123
  user@erdv.com  / user123
  martin@erdv.com / prestataire123  (prestataire)
  dubois@erdv.com / prestataire123
  laurent@erdv.com / prestataire123

Commandes utiles :
  docker compose logs -f backend
  docker compose down
"@
