# Génère ou met à jour le fichier .env à la racine du dépôt avec un JWT_SECRET aléatoire (64 caractères hex = 32 octets, adapté HS256).
# Usage : .\scripts\generate-docker-env.ps1
#         .\scripts\generate-docker-env.ps1 -Force   # régénère JWT_SECRET même s'il est déjà renseigné

param(
    [switch] $Force
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"
$example = Join-Path $root ".env.example"

if (-not (Test-Path $example)) {
    Write-Error ".env.example introuvable à la racine du projet."
}

if (-not (Test-Path $envFile)) {
    Copy-Item $example $envFile
    Write-Host "Fichier .env créé à partir de .env.example"
}

$lines = Get-Content $envFile
$hasJwt = $false
$currentSecret = ""
foreach ($line in $lines) {
    if ($line -match '^\s*JWT_SECRET=(.*)$') {
        $hasJwt = $true
        $currentSecret = $Matches[1].Trim()
        break
    }
}

$needNew = $Force -or (-not $hasJwt) -or ($currentSecret.Length -lt 32)

if (-not $needNew) {
    Write-Host "JWT_SECRET déjà défini (>= 32 caractères). Utilisez -Force pour régénérer."
    exit 0
}

$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$secret = -join ($bytes | ForEach-Object { '{0:x2}' -f $_ })

$jwtLineFound = $false
$out = foreach ($line in $lines) {
    if ($line -match '^\s*JWT_SECRET=') {
        $jwtLineFound = $true
        "JWT_SECRET=$secret"
    } else {
        $line
    }
}
if (-not $jwtLineFound) {
    $out = @($out) + "JWT_SECRET=$secret"
}

$out | Set-Content -Path $envFile -Encoding utf8
Write-Host "JWT_SECRET mis à jour dans .env (ne commitez pas ce fichier)."
