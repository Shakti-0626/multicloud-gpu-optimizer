Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Starting Multicloud AI Optimizer Backend..." -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan

Set-Location -Path "$PSScriptRoot\backend"

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "Using Maven..." -ForegroundColor Yellow
    mvn clean spring-boot:run
} elseif (Get-Command .\mvnw.cmd -ErrorAction SilentlyContinue) {
    Write-Host "Using Maven Wrapper..." -ForegroundColor Yellow
    .\mvnw.cmd clean spring-boot:run
} else {
    Write-Host "Maven was not found in PATH." -ForegroundColor Red
    Write-Host "Please install Maven or ensure 'mvn' is available in your PATH." -ForegroundColor Yellow
}
