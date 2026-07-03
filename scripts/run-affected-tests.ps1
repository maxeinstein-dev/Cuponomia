param(
    [switch]$Staged,
    [string]$BaseRef
)

$ErrorActionPreference = "Stop"

function Normalize-PathForMaven([string]$Path) {
    return $Path -replace "\\", "/"
}

function Get-ChangedFiles {
    if ($Staged) {
        return git diff --cached --name-only --diff-filter=ACMRT
    }

    if (-not [string]::IsNullOrWhiteSpace($BaseRef)) {
        return git diff --name-only --diff-filter=ACMRT "$BaseRef...HEAD"
    }

    return git diff --name-only --diff-filter=ACMRT HEAD
}

function Test-JavaDiffHasOnlyComments([string]$Path) {
    if ($Staged) {
        $diff = git diff --cached --unified=0 -- $Path
    }
    elseif (-not [string]::IsNullOrWhiteSpace($BaseRef)) {
        $diff = git diff --unified=0 "$BaseRef...HEAD" -- $Path
    }
    else {
        $diff = git diff --unified=0 HEAD -- $Path
    }

    $changedLines = @()

    foreach ($line in $diff) {
        if ($line.StartsWith("+++") -or $line.StartsWith("---")) {
            continue
        }

        if ($line.StartsWith("+") -or $line.StartsWith("-")) {
            $changedLines += $line.Substring(1).Trim()
        }
    }

    if ($changedLines.Count -eq 0) {
        return $true
    }

    foreach ($line in $changedLines) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }

        if ($line.StartsWith("//") -or
            $line.StartsWith("/*") -or
            $line.StartsWith("*") -or
            $line.StartsWith("*/")) {
            continue
        }

        return $false
    }

    return $true
}

function Add-Module([System.Collections.Generic.HashSet[string]]$Modules, [string]$Module) {
    [void]$Modules.Add($Module)
}

$changedFiles = @(Get-ChangedFiles | ForEach-Object { Normalize-PathForMaven $_ } | Where-Object { $_ })

if ($changedFiles.Count -eq 0) {
    Write-Host "No changed files found. Skipping tests."
    exit 0
}

if (-not $Staged -and [string]::IsNullOrWhiteSpace($BaseRef)) {
    Write-Host "No base reference provided. Falling back to changes relative to HEAD."
}

$affectedModules = [System.Collections.Generic.HashSet[string]]::new()
$runAll = $false

foreach ($file in $changedFiles) {
    if ($file -eq "pom.xml") {
        $runAll = $true
        break
    }

    if ($file -match "^(README|DESENVOLVIMENTO|HELP)\.md$" -or
        $file -match "\.md$" -or
        $file -match "^\.gitignore$" -or
        $file -match "^\.gitattributes$" -or
        $file -match "^\.dockerignore$") {
        continue
    }

    if ($file -match "^coupon-contracts/") {
        if ($file -match "\.java$" -and (Test-JavaDiffHasOnlyComments $file)) {
            continue
        }

        Add-Module $affectedModules "coupon-management-service"
        Add-Module $affectedModules "coupon-validation-service"
        continue
    }

    if ($file -match "^coupon-management-service/") {
        if ($file -match "\.java$" -and (Test-JavaDiffHasOnlyComments $file)) {
            continue
        }

        Add-Module $affectedModules "coupon-management-service"
        continue
    }

    if ($file -match "^coupon-validation-service/") {
        if ($file -match "\.java$" -and (Test-JavaDiffHasOnlyComments $file)) {
            continue
        }

        Add-Module $affectedModules "coupon-validation-service"
        continue
    }

    if ($file -match "^(Dockerfile|docker-compose\.yml|scripts/|\.githooks/)") {
        continue
    }

    $runAll = $true
    break
}

if ($runAll) {
    Write-Host "Root-level build change detected. Running all tests."
    $mavenArgs = @("test")
} elseif ($affectedModules.Count -gt 0) {
    $moduleList = ($affectedModules | Sort-Object) -join ","
    Write-Host "Affected modules: $moduleList"
    $mavenArgs = @("-pl", $moduleList, "-am", "test")
} else {
    Write-Host "Only documentation, hook, script, or comment-only changes detected. Skipping tests."
    exit 0
}

$mvnw = if (Test-Path ".\mvnw.cmd") { ".\mvnw.cmd" } else { ".\mvnw" }
Write-Host "Running: $mvnw $($mavenArgs -join ' ')"
& $mvnw @mavenArgs
exit $LASTEXITCODE
