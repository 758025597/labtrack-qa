@echo off
REM Ejecuta los 3 escenarios de carga del catalogo (LabTrack debe estar corriendo en localhost:8080).
REM Requiere que jmeter.bat este en el PATH.
cd /d %~dp0
if not exist resultados mkdir resultados
call :escenario 50 10
call :escenario 100 20
call :escenario 150 30
echo Listo. Abra resultados\reporte_XX\index.html para ver tablas y graficas.
goto :eof

:escenario
echo ^>^> Escenario: %1 usuarios, ramp-up %2 s
if exist resultados\reporte_%1 rmdir /s /q resultados\reporte_%1
if exist resultados\escenario_%1.jtl del resultados\escenario_%1.jtl
call jmeter -n -t Catalogo.jmx -Jusuarios=%1 -Jrampup=%2 -Jiteraciones=5 -l resultados\escenario_%1.jtl -e -o resultados\reporte_%1
goto :eof
