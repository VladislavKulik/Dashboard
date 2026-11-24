$ErrorActionPreference = 'Stop'

$GradleVersion = '9.5.0'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BootstrapRoot = Join-Path $env:LOCALAPPDATA 'LifeDashboard\gradle-bootstrap'
$GradleHome = Join-Path $BootstrapRoot "gradle-$GradleVersion"
$ZipPath = Join-Path $BootstrapRoot "gradle-$GradleVersion-bin.zip"
$ChecksumPath = "$ZipPath.sha256"
$WrapperProject = Join-Path $BootstrapRoot 'wrapper-project'

New-Item -ItemType Directory -Force -Path $BootstrapRoot | Out-Null

if (-not (Test-Path $ChecksumPath)) {
    Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip.sha256" -OutFile $ChecksumPath
}
$ExpectedHash = ((Get-Content $ChecksumPath -Raw).Trim() -split '\s+')[0].ToLowerInvariant()

if (-not (Test-Path (Join-Path $GradleHome 'bin\gradle.bat'))) {
    Write-Host "Downloading Gradle $GradleVersion..."
    Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip" -OutFile $ZipPath

    $ActualHash = (Get-FileHash -Algorithm SHA256 $ZipPath).Hash.ToLowerInvariant()
    if ($ActualHash -ne $ExpectedHash) {
        Remove-Item $ZipPath -Force
        throw 'Gradle distribution SHA-256 checksum mismatch. Download was rejected.'
    }

    Expand-Archive -Path $ZipPath -DestinationPath $BootstrapRoot -Force
}

# Generate the wrapper in an empty temporary Gradle project. This avoids
# resolving the Android plugin or requiring an installed Android SDK first.
if (Test-Path $WrapperProject) {
    Remove-Item $WrapperProject -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $WrapperProject | Out-Null
Set-Content -Path (Join-Path $WrapperProject 'settings.gradle') -Value "rootProject.name = 'lifedashboard-wrapper-bootstrap'" -Encoding utf8

Push-Location $WrapperProject
try {
    & (Join-Path $GradleHome 'bin\gradle.bat') wrapper --gradle-version $GradleVersion --distribution-type bin
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle Wrapper generation failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

$ProjectWrapperDir = Join-Path $ProjectRoot 'gradle\wrapper'
$TempWrapperDir = Join-Path $WrapperProject 'gradle\wrapper'
New-Item -ItemType Directory -Force -Path $ProjectWrapperDir | Out-Null
Copy-Item (Join-Path $WrapperProject 'gradlew') (Join-Path $ProjectRoot 'gradlew') -Force
Copy-Item (Join-Path $WrapperProject 'gradlew.bat') (Join-Path $ProjectRoot 'gradlew.bat') -Force
Copy-Item (Join-Path $TempWrapperDir 'gradle-wrapper.jar') (Join-Path $ProjectWrapperDir 'gradle-wrapper.jar') -Force
Copy-Item (Join-Path $TempWrapperDir 'gradle-wrapper.properties') (Join-Path $ProjectWrapperDir 'gradle-wrapper.properties') -Force
Add-Content -Path (Join-Path $ProjectWrapperDir 'gradle-wrapper.properties') -Value "distributionSha256Sum=$ExpectedHash"

Write-Host ''
Write-Host 'Gradle Wrapper generated and distribution checksum configured.'
Write-Host 'Next: open this folder in Android Studio and sync the project.'
