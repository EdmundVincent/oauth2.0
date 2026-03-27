@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

if exist "%WRAPPER_JAR%" goto run

if not exist "%WRAPPER_PROPERTIES%" (
  echo Missing %WRAPPER_PROPERTIES%
  exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
  if /i "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
)

if "%WRAPPER_URL%"=="" (
  echo Missing wrapperUrl in %WRAPPER_PROPERTIES%
  exit /b 1
)

echo Downloading Maven wrapper jar from %WRAPPER_URL%
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$p='%WRAPPER_JAR%'; $u='%WRAPPER_URL%';" ^
  "New-Item -ItemType Directory -Force -Path (Split-Path -Parent $p) | Out-Null;" ^
  "Invoke-WebRequest -UseBasicParsing -Uri $u -OutFile $p"

if not exist "%WRAPPER_JAR%" (
  echo Failed to download Maven wrapper jar.
  exit /b 1
)

:run
set "MAVEN_OPTS=%MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%"
java %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
