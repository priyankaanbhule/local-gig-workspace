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
$jdkDirectory = Join-Path $env:ProgramFiles "Eclipse Adoptium"
$java21Home = Get-ChildItem -LiteralPath $jdkDirectory -Directory -Filter "jdk-21*" -ErrorAction SilentlyContinue |
    Where-Object { Test-Path (Join-Path $_.FullName "bin\javac.exe") } |
    Sort-Object Name -Descending |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $java21Home) {
    throw "JDK 21 was not found under $jdkDirectory. Install JDK 21 or update this script's java21Home selection."
}

$originalJavaHome = $env:JAVA_HOME
$originalPath = $env:Path
$originalTemp = $env:TEMP
$originalTmp = $env:TMP
$wrapperTemp = Join-Path $projectRoot "build\maven-wrapper-tmp"
New-Item -ItemType Directory -Path $wrapperTemp -Force | Out-Null
try {
    # Maven uses JAVA_HOME, which may differ from the java.exe first on PATH.
    $env:JAVA_HOME = $java21Home
    $env:Path = "$(Join-Path $java21Home 'bin');$originalPath"
    # Keep wrapper extraction under the project so Windows sandbox/temp ACLs
    # cannot prevent Maven from preparing its downloaded distribution.
    $env:TEMP = $wrapperTemp
    $env:TMP = $wrapperTemp
    if ($maven) {
        & $maven.Source -B "-Dmaven.test.skip=true" package
    }
    else {
        & (Join-Path $projectRoot "mvnw.cmd") -B "-Dmaven.test.skip=true" package
    }
}
finally {
    $env:JAVA_HOME = $originalJavaHome
    $env:Path = $originalPath
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
