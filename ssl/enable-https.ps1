# Enable HTTPS for IAM Platform Services
# This script sets environment variables to enable SSL/TLS

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IAM Platform HTTPS Enable Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# SSL Configuration
$env:SSL_ENABLED = "true"
$env:SSL_KEY_STORE = "file:$PSScriptRoot\..\ssl\keystore.p12"
$env:SSL_KEY_STORE_PASSWORD = "changeit"

# JWK Configuration (using new certificates)
$env:JWK_RSA_PRIVATE_KEY = "file:$PSScriptRoot\..\ssl\private.key"
$env:JWK_RSA_PUBLIC_KEY = "file:$PSScriptRoot\..\ssl\certificate.crt"

Write-Host "Environment variables set:" -ForegroundColor Green
Write-Host "  SSL_ENABLED = $env:SSL_ENABLED" -ForegroundColor White
Write-Host "  SSL_KEY_STORE = $env:SSL_KEY_STORE" -ForegroundColor White
Write-Host "  SSL_KEY_STORE_PASSWORD = [HIDDEN]" -ForegroundColor White
Write-Host "  JWK_RSA_PRIVATE_KEY = $env:JWK_RSA_PRIVATE_KEY" -ForegroundColor White
Write-Host "  JWK_RSA_PUBLIC_KEY = $env:JWK_RSA_PUBLIC_KEY" -ForegroundColor White
Write-Host ""

# Verify certificate files exist
Write-Host "Verifying certificate files..." -ForegroundColor Yellow

$certFiles = @(
    "$PSScriptRoot\..\ssl\keystore.p12",
    "$PSScriptRoot\..\ssl\private.key",
    "$PSScriptRoot\..\ssl\certificate.crt"
)

$allFilesExist = $true
foreach ($file in $certFiles) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file (NOT FOUND)" -ForegroundColor Red
        $allFilesExist = $false
    }
}

Write-Host ""

if ($allFilesExist) {
    Write-Host "All certificate files found!" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now start IAM Platform services with HTTPS enabled." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Service URLs:" -ForegroundColor Yellow
    Write-Host "  Auth Server:  https://localhost:9000" -ForegroundColor White
    Write-Host "  Gateway:      https://localhost:8080" -ForegroundColor White
    Write-Host "  Admin Server: https://localhost:9002" -ForegroundColor White
    Write-Host "  BFF Server:   https://localhost:9010" -ForegroundColor White
    Write-Host ""
    Write-Host "Note: Browser will show certificate warning (self-signed cert)." -ForegroundColor Magenta
} else {
    Write-Host "ERROR: Some certificate files are missing!" -ForegroundColor Red
    Write-Host "Please generate certificates first." -ForegroundColor Red
    exit 1
}
