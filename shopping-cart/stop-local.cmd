@echo off
REM Stops the seven windows started by run-local.cmd. Other Java programs are left alone.
for %%S in (discovery-server auth-service profile-service product-service cart-service order-service api-gateway) do (
  taskkill /FI "WINDOWTITLE eq %%S*" /T /F >nul 2>&1
)
echo Stopped.
