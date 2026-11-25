Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Starting Multicloud AI Optimizer Dashboard..." -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan

Set-Location -Path "$PSScriptRoot\dashboard"

if (Get-Command npm -ErrorAction SilentlyContinue) {
    if (-not (Test-Path -Path "node_modules")) {
        Write-Host "Installing dashboard npm dependencies..." -ForegroundColor Yellow
        npm install
    }
    Write-Host "Starting Vite development server..." -ForegroundColor Green
    npm run dev
} else {
    Write-Host "Node.js / npm was not found in PATH." -ForegroundColor Red
    Write-Host "Please install Node.js 20 LTS from https://nodejs.org/" -ForegroundColor Yellow
}
