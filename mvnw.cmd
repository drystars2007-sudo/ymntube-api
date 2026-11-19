@echo off
setlocal EnableDelayedExpansion
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MAVEN_EXE=%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd"

if not exist "%MAVEN_EXE%" (
    set "MAVEN_ZIP=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip"
    curl.exe --ssl-revoke-best-effort --fail --location --silent --show-error "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip" --output "!MAVEN_ZIP!"
    if errorlevel 1 exit /b 1
    powershell -NoProfile -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force -Path '!MAVEN_HOME!' | Out-Null; Expand-Archive -LiteralPath '!MAVEN_ZIP!' -DestinationPath '!MAVEN_HOME!' -Force; Remove-Item -LiteralPath '!MAVEN_ZIP!'"
    if errorlevel 1 exit /b 1
)

"%MAVEN_EXE%" %*
