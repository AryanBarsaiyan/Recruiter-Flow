# Run all tests using Maven Wrapper. Ensures Java is found.
$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

function Find-Java {
    if ($env:JAVA_HOME) {
        $java = Join-Path $env:JAVA_HOME "bin\java.exe"
        if (Test-Path $java) { return $java }
    }
    $javaInPath = Get-Command java -ErrorAction SilentlyContinue
    if ($javaInPath) { return $javaInPath.Source }
    $dirs = @(
        (Join-Path $env:ProgramFiles "Java\jdk*"),
        (Join-Path $env:ProgramFiles "Eclipse Adoptium\jdk*"),
        (Join-Path $env:ProgramFiles "Microsoft\jdk*"),
        (Join-Path $env:USERPROFILE ".jdks\*")
    )
    foreach ($pattern in $dirs) {
        $dir = Get-Item $pattern -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($dir) {
            $java = Join-Path $dir.FullName "bin\java.exe"
            if (Test-Path $java) { return $java }
        }
    }
    throw "Java not found. Set JAVA_HOME or install JDK 21+ and add to PATH."
}

$javaExe = Find-Java
$wrapperJar = Join-Path $scriptDir ".mvn\wrapper\maven-wrapper.jar"
if (-not (Test-Path $wrapperJar)) {
    Write-Host "Downloading Maven Wrapper..."
    Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar" -OutFile $wrapperJar -UseBasicParsing
}
& $javaExe -jar $wrapperJar @args
exit $LASTEXITCODE
