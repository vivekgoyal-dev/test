@echo off
REM Reports everything that has to be true before the services can start.
setlocal enabledelayedexpansion
cd /d "%~dp0"
echo ============================================================
echo  Shopping Cart - environment check
echo ============================================================
echo.

echo [1] Java
where java >nul 2>&1
if errorlevel 1 (
  echo     NOT FOUND. Java is not on the PATH.
) else (
  java -version 2>&1 | findstr /i "version"
)
echo.

echo [2] Built jars
set MISSING=0
for %%S in (discovery-server auth-service profile-service product-service cart-service order-service api-gateway) do (
  if exist "%%S\target\%%S-1.0.0.jar" (
    echo     ok       %%S
  ) else (
    echo     MISSING  %%S
    set /a MISSING+=1
  )
)
if !MISSING! GTR 0 echo     ^-^-^> !MISSING! not built yet. run-local.cmd builds them.
echo.

echo [3] MySQL on port 3306
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
  echo     NOT LISTENING. MySQL is not running, so five of the seven services cannot start.
) else (
  echo     ok, something is listening on 3306
)
echo.

echo [4] DB_PASSWORD
if "%DB_PASSWORD%"=="" (
  echo     NOT SET in this window.
  echo     Set it with:  setx DB_PASSWORD "your-mysql-root-password"
  echo     then open a NEW terminal, setx does not affect the current one.
) else (
  echo     ok, it is set
)
echo.

echo [5] Ports that must be free
for %%P in (8761 8080 8081 8082 8083 8084 8085) do (
  netstat -ano | findstr ":%%P " | findstr "LISTENING" >nul 2>&1
  if errorlevel 1 (echo     free   %%P) else (echo     IN USE %%P)
)
echo.
echo ============================================================
echo  Fix anything marked NOT or MISSING above, then run run-local.cmd
echo ============================================================
pause
