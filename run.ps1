# Smart Library Launcher Script for PowerShell
$jarPath = "backend/target/smartlibrary-1.0.0.jar"

if (-not (Test-Path $jarPath)) {
    $mvnCmd = "mvn"
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        $candidates = @(
            "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd",
            "$env:USERPROFILE\.vscode\extensions\oracle.oracle-java-26.0.2\nbcode\java\maven\bin\mvn.cmd"
        )
        foreach ($c in $candidates) {
            if (Test-Path $c) {
                $mvnCmd = $c
                break
            }
        }
    }
    Write-Host "Building project package..." -ForegroundColor Cyan
    & $mvnCmd package -DskipTests -f backend/pom.xml
}

Write-Host "Starting Smart Library Resource Management at http://localhost:8080 ..." -ForegroundColor Green
Start-Process "http://localhost:8080/index.html"
java -jar $jarPath
