@echo off
REM Builds anything not yet built, then starts all seven services, each in its own window.
REM The windows stay open on purpose: if a service dies, its error is still on screen.
setlocal enabledelayedexpansion
cd /d "%~dp0"

set SERVICES=discovery-server auth-service profile-service product-service cart-service order-service api-gateway

echo ============================================================
where java >nul 2>&1
if errorlevel 1 (
  echo  Java is not on the PATH. Install a JDK 17 or newer, or open
  echo  a terminal where "java -version" works, then run this again.
  pause
  exit /b 1
)
java -version 2>&1 | findstr /i "version"
echo ============================================================
echo.

REM ---- build anything missing -------------------------------------------
for %%S in (%SERVICES%) do (
  if not exist "%%S\target\%%S-1.0.0.jar" (
    echo Building %%S . . .  ^(first run downloads Maven and the dependencies, this is slow^)
    call mvnw.cmd -B -f "%%S\pom.xml" -DskipTests package
    if errorlevel 1 (
      echo.
      echo ############################################################
      echo  BUILD FAILED for %%S. Nothing was started.
      echo  The Maven output above says why. Common causes:
      echo    - no internet access to repo.maven.apache.org
      echo    - a corporate proxy that Maven needs configuring for
      echo ############################################################
      pause
      exit /b 1
    )
  )
)

REM ---- refuse to start if a jar is still missing -------------------------
for %%S in (%SERVICES%) do (
  if not exist "%%S\target\%%S-1.0.0.jar" (
    echo.
    echo ############################################################
    echo  %%S\target\%%S-1.0.0.jar does not exist, so nothing can start.
    echo  Run check.cmd to see what is wrong.
    echo ############################################################
    pause
    exit /b 1
  )
)

echo.
echo All seven built. Starting discovery-server, then waiting 30 seconds
echo for it to accept registrations . . .
start "discovery-server" cmd /k "cd /d "%CD%\discovery-server" && java -jar target\discovery-server-1.0.0.jar"
timeout /t 30 /nobreak >nul

for %%S in (auth-service profile-service product-service cart-service order-service) do (
  echo Starting %%S . . .
  start "%%S" cmd /k "cd /d "%CD%\%%S" && java -jar target\%%S-1.0.0.jar"
)
timeout /t 30 /nobreak >nul

echo Starting api-gateway . . .
start "api-gateway" cmd /k "cd /d "%CD%\api-gateway" && java -jar target\api-gateway-1.0.0.jar"
timeout /t 20 /nobreak >nul

echo.
echo ============================================================
echo  Seven windows are now open, one per service.
echo.
echo  Check each one. A healthy service ends with a line like
echo    Started AuthServiceApplication in 5.1 seconds
echo.
echo  A window showing a red stack trace has failed. The usual causes:
echo    "Communications link failure"  MySQL is not running
echo    "Access denied for user"       DB_PASSWORD is wrong or not set
echo    "Unknown database"             the five schemas were never created
echo    "Port XXXX was already in use" something else has that port
echo.
echo  Run check.cmd to test all of those at once.
echo.
echo    Gateway  http://localhost:8080
echo    Eureka   http://localhost:8761
echo ============================================================
echo.
echo Give it another 30 seconds, then open Eureka. All six should be listed.
pause
