@echo off
REM Builds anything not yet built, then opens one window per service.
REM Each window stays open, so a service that fails leaves its error on screen.
setlocal
cd /d "%~dp0"

set SERVICES=discovery-server auth-service profile-service product-service cart-service order-service api-gateway

where java >nul 2>&1
if errorlevel 1 (
  echo Java is not on the PATH. Open a terminal where "java -version" works.
  pause
  exit /b 1
)
java -version 2>&1 | findstr /i "version"
echo.

REM ---- build anything missing -------------------------------------------
for %%S in (%SERVICES%) do call :build %%S
if errorlevel 1 exit /b 1

REM ---- refuse to start if a jar is still missing -------------------------
for %%S in (%SERVICES%) do (
  if not exist "%%S\target\%%S-1.0.0.jar" (
    echo.
    echo  %%S was not built, so nothing will be started. Scroll up for the Maven error.
    pause
    exit /b 1
  )
)

echo.
echo Starting discovery-server, then waiting 30 seconds for it to accept registrations . . .
start "discovery-server" /D "%CD%\discovery-server" cmd /k java -jar target\discovery-server-1.0.0.jar
timeout /t 30 /nobreak >nul

for %%S in (auth-service profile-service product-service cart-service order-service) do (
  echo Starting %%S . . .
  start "%%S" /D "%CD%\%%S" cmd /k java -jar target\%%S-1.0.0.jar
  timeout /t 3 /nobreak >nul
)
timeout /t 30 /nobreak >nul

echo Starting api-gateway . . .
start "api-gateway" /D "%CD%\api-gateway" cmd /k java -jar target\api-gateway-1.0.0.jar

echo.
echo ============================================================
echo  Seven windows should now be open, one per service.
echo  A healthy one ends with:  Started XxxApplication in N seconds
echo.
echo  If a window shows a stack trace instead:
echo    "Communications link failure"   MySQL is not running
echo    "Access denied for user"        DB_PASSWORD wrong or not set
echo    "Unknown database"              the five schemas were not created
echo    "Port XXXX was already in use"  something else has that port
echo.
echo    Gateway  http://localhost:8080
echo    Eureka   http://localhost:8761
echo ============================================================
pause
exit /b 0

:build
if exist "%~1\target\%~1-1.0.0.jar" goto :eof
echo Building %~1 . . .  (the first one downloads Maven, this is slow)
call mvnw.cmd -B -f "%~1\pom.xml" -DskipTests package
if errorlevel 1 (
  echo.
  echo  BUILD FAILED for %~1. Nothing was started. The Maven output above says why.
  pause
  exit /b 1
)
goto :eof
