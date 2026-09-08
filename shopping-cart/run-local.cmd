@echo off
REM Builds anything not yet built, then starts all seven services, each in its own window.
REM Ports 8761 and 8080 to 8085 must be free.
setlocal
cd /d "%~dp0"

set SERVICES=discovery-server auth-service profile-service product-service cart-service order-service api-gateway

for %%S in (%SERVICES%) do (
  if not exist "%%S\target\%%S-1.0.0.jar" (
    echo Building %%S . . .
    call mvnw.cmd -q -B -f "%%S\pom.xml" -DskipTests package
    if errorlevel 1 (
      echo.
      echo BUILD FAILED for %%S. Nothing was started.
      exit /b 1
    )
  )
)

echo.
echo Starting discovery-server, then waiting for it to accept registrations . . .
start "discovery-server" cmd /c "cd discovery-server && java -jar target\discovery-server-1.0.0.jar"
timeout /t 25 /nobreak >nul

for %%S in (auth-service profile-service product-service cart-service order-service) do (
  echo Starting %%S . . .
  start "%%S" cmd /c "cd %%S && java -jar target\%%S-1.0.0.jar"
)
timeout /t 25 /nobreak >nul

echo Starting api-gateway . . .
start "api-gateway" cmd /c "cd api-gateway && java -jar target\api-gateway-1.0.0.jar"
timeout /t 15 /nobreak >nul

echo.
echo All seven started, each in its own window.
echo   Gateway  http://localhost:8080
echo   Eureka   http://localhost:8761
echo.
echo Give it about 30 seconds, then check Eureka: all six applications should be listed.
echo Closing a window stops that service, or run stop-local.cmd to stop them all.
