# Arrête la stack et supprime les volumes (base PostgreSQL effacée). Utile avant un premier déploiement Flyway ou après changement de schéma incompatible.
# Usage : .\scripts\reset-docker-volumes.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host ""
Write-Host "ATTENTION : toutes les données PostgreSQL du volume 'postgres_data' seront supprimées."
$confirm = Read-Host "Tapez OUI pour continuer"
if ($confirm -ne "OUI") {
    Write-Host "Annulé."
    exit 0
}

docker compose down -v
Write-Host "Terminé. Relancez : docker compose up -d --build"
