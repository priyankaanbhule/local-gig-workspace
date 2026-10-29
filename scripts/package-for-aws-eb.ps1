$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $projectRoot "frontend"
$staticRoot = Join-Path $projectRoot "src\main\resources\static"
$targetRoot = Join-Path $projectRoot "target"
$buildRoot = Join-Path $projectRoot "build\aws-eb-bundle"

Push-Location $frontendRoot
try {
    npm ci
    if ($LASTEXITCODE -ne 0) { throw "npm ci failed." }

    npm run build
    if ($LASTEXITCODE -ne 0) { throw "React build failed." }
}
finally {
    Pop-Location
}

if (Test-Path $staticRoot) {
    Get-ChildItem -LiteralPath $staticRoot -Force | Remove-Item -Recurse -Force
}
else {
    New-Item -ItemType Directory -Path $staticRoot -Force | Out-Null
}
Copy-Item -Path (Join-Path $frontendRoot "dist\*") -Destination $staticRoot -Recurse -Force

$maven = Get-Command mvn -ErrorAction SilentlyContinue
$originalTemp = $env:TEMP
$originalTmp = $env:TMP
$wrapperTemp = Join-Path $projectRoot "build\maven-wrapper-tmp"
New-Item -ItemType Directory -Path $wrapperTemp -Force | Out-Null
try {
    # Keep wrapper extraction under the project so Windows sandbox/temp ACLs
    # cannot prevent Maven from preparing its downloaded distribution.
    $env:TEMP = $wrapperTemp
    $env:TMP = $wrapperTemp
    if ($maven) {
        & $maven.Source -B -DskipTests package
    }
    else {
        & (Join-Path $projectRoot "mvnw.cmd") -B -DskipTests package
    }
}
finally {
    $env:TEMP = $originalTemp
    $env:TMP = $originalTmp
}
if ($LASTEXITCODE -ne 0) { throw "Spring Boot package build failed." }

$jar = Get-ChildItem -LiteralPath $targetRoot -Filter "localGigWorkplace-*.jar" |
    Where-Object { $_.Name -notlike "*.original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $jar) { throw "Spring Boot executable JAR was not found in target/." }

if (Test-Path $buildRoot) {
    Get-ChildItem -LiteralPath $buildRoot -Force | Remove-Item -Recurse -Force
}
else {
    New-Item -ItemType Directory -Path $buildRoot -Force | Out-Null
}
Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $buildRoot "application.jar")
Copy-Item -LiteralPath (Join-Path $projectRoot "Procfile") -Destination $buildRoot

$archive = Join-Path $projectRoot "build\localgig-aws-eb.zip"
if (Test-Path $archive) { Remove-Item -LiteralPath $archive -Force }
Compress-Archive -Path (Join-Path $buildRoot "*") -DestinationPath $archive -Force

Write-Host "Elastic Beanstalk bundle ready: $archive"
