#!/usr/bin/env bash
# Ejecuta los 3 escenarios de carga del catálogo (LabTrack debe estar corriendo en localhost:8080).
# Uso: ./ejecutar.sh            (requiere 'jmeter' en el PATH)
set -e
cd "$(dirname "$0")"
mkdir -p resultados
for escenario in "50 10" "100 20" "150 30"; do
  set -- $escenario
  echo ">> Escenario: $1 usuarios, ramp-up $2 s"
  rm -rf "resultados/reporte_$1" "resultados/escenario_$1.jtl"
  jmeter -n -t Catalogo.jmx -Jusuarios="$1" -Jrampup="$2" -Jiteraciones=5 \
         -l "resultados/escenario_$1.jtl" -e -o "resultados/reporte_$1"
done
echo "Listo. Abra resultados/reporte_XX/index.html para ver tablas y gráficas."
