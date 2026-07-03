param(
    [string]$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"

Push-Location $RepositoryRoot
try {
    $currentPath = git config --get core.hooksPath

    if ($currentPath -eq ".githooks") {
        Write-Host "Git hooks are already configured to use .githooks."
        exit 0
    }

    git config core.hooksPath .githooks
    Write-Host "Configured Git to use .githooks as the hooks path."
}
finally {
    Pop-Location
}