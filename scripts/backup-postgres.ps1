# Sauvegarde logique de la base erdv_db (pg_dump) depuis le conteneur erdv_postgres.
# Planification Windows : Planificateur de tâches -> exécuter ce script selon un calendrier.
# Usage : .\scripts\backup-postgres.ps1
#         .\scripts\backup-postgres.ps1 -ContainerName autre_postgres

param(
    [string] $ContainerName = "erdv_postgres"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$backupDir = Join-Path $root "backups"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$outFile = Join-Path $backupDir "erdv_db_$ts.sql"

docker exec $ContainerName pg_dump -U postgres --no-owner erdv_db | Set-Content -Path $outFile -Encoding utf8

if (-not (Test-Path $outFile) -or (Get-Item $outFile).Length -eq 0) {
    Write-Error "Échec de la sauvegarde (fichier vide ou absent). Le conteneur $ContainerName est-il démarré ?"
}

Write-Host "Sauvegarde enregistrée : $outFile"
