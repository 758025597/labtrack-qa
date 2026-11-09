# LabTrack – Sistema de Gestión de Laboratorios

Proyecto del curso **Calidad y Pruebas de Software** (UPN) – Desarrollo de Caso T3.
Docente: Mark Anthony Arribasplata Palomino.

**Integrantes:**
- _[Apellidos y nombres – código]_
- _[Apellidos y nombres – código]_
- _[Apellidos y nombres – código]_

---

## 1. ¿Qué hace LabTrack?

| HU | Funcionalidad | Dónde está |
|----|---------------|------------|
| HU-01 | Inicio de sesión con código y contraseña, mensaje "Credenciales inválidas" y bloqueo temporal tras 3 intentos fallidos consecutivos | `/login` – `AuthService` |
| HU-02 | Catálogo de equipos (nombre, código, categoría, estado) con filtros Cómputo / Electrónica / Redes | `/catalogo` – `CatalogoService` |
| HU-03 | Reserva de un equipo disponible con fecha y horario; bloquea cruces con reservas aprobadas y más de 1 equipo simultáneo | `/reservas/nueva` – `ReservaService`, `ValidadorCruces` |
| HU-04 | Panel del encargado: aprobar o rechazar (con motivo obligatorio) las solicitudes pendientes | `/admin/solicitudes` – `SolicitudService` |
| HU-05 | Registrar la devolución; el equipo vuelve a "Disponible" y se penaliza al estudiante si devuelve tarde | `/admin/devoluciones` – `DevolucionService` |

**Tecnologías:** Java 17, Spring Boot 3.3, Thymeleaf, Spring Data JPA, H2 (en memoria), BCrypt.
**Pruebas:** JUnit 5, Selenium WebDriver 4, Apache JMeter 5.6, SonarQube, JaCoCo.

## 2. Estructura del repositorio

```
labtrack-qa/
├── pom.xml                         → dependencias, JaCoCo, perfil e2e y SonarQube
├── src/main/java/pe/edu/upn/labtrack/
│   ├── model/                      → entidades: Usuario, Equipo, Reserva
│   ├── repository/                 → acceso a datos (Spring Data JPA)
│   ├── service/                    → reglas de negocio de HU-01 a HU-05
│   ├── web/                        → controladores y control de acceso
│   └── config/                     → reloj, BCrypt y datos iniciales
├── src/main/resources/templates/   → vistas HTML (Thymeleaf)
├── src/test/java/.../unit/         → pruebas unitarias de reglas de negocio
├── src/test/java/.../e2e/          → pruebas automatizadas con Selenium (27 casos)
└── tests/jmeter/                   → plan de carga Catalogo.jmx, usuarios.csv y scripts
```

## 3. Requisitos

- JDK 17 o superior
- Maven 3.9 o superior (o abrir el proyecto en IntelliJ IDEA / Eclipse, que traen Maven)
- Google Chrome (para Selenium; el driver se descarga solo con Selenium Manager)
- Apache JMeter 5.6.3 (prueba de carga)
- Docker Desktop (para levantar SonarQube)

## 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Abrir <http://localhost:8080>. Usuarios de demostración:

| Rol | Código | Contraseña |
|-----|--------|-----------|
| Estudiante | N00123456 (Ana Torres) | Lab2026* |
| Estudiante | N00654321 (Luis Rojas) | Lab2026* |
| Estudiante | N00111222 (María Quispe) | Lab2026* |
| Encargado | ADMIN01 | Admin2026* |
| Prueba de carga | N00000001 … N00000150 | Carga2026* |

> Las claves de demostración se pueden cambiar con las variables de entorno
> `LABTRACK_CLAVE_ESTUDIANTE`, `LABTRACK_CLAVE_ENCARGADO` y `LABTRACK_CLAVE_CARGA`.

## 5. Pruebas automatizadas

### 5.1 Pruebas unitarias (reglas de negocio)

```bash
mvn test
```

### 5.2 Pruebas Selenium (flujos de Login, Catálogo, Reservas, Solicitudes y Devoluciones)

```bash
mvn verify -Pe2e                      # Chrome en modo headless
mvn verify -Pe2e -De2e.visible=true   # para ver el navegador (útil para capturas)
```

La prueba levanta LabTrack sola, en un puerto aleatorio. Los reportes quedan en `target/surefire-reports/` (XML de JUnit).

**Subir los resultados a Testomat.io** (según la documentación del reporter de Testomat.io):

```bash
npm install @testomatio/reporter --save-dev
TESTOMATIO=<API_KEY_DEL_PROYECTO> npx report-xml "target/surefire-reports/*.xml" --lang java
```

En **TestLink**, registrar los casos CP-xx de la matriz y marcar el resultado de cada ejecución según el reporte de Surefire.

### 5.3 Trazabilidad: caso de prueba → prueba automatizada

| Caso | Prueba Selenium | Prueba unitaria |
|------|-----------------|-----------------|
| CP-01 a CP-06, CP-08 | `LoginE2ETest` | `AuthServiceTest` (CP-01 a CP-09) |
| CP-10 a CP-14 | `CatalogoE2ETest` | `CatalogoServiceTest` (CP-11, CP-14, CP-15) |
| CP-18 a CP-21, CP-23 a CP-25, CP-27 | `ReservaE2ETest` | `ReservaServiceTest` (CP-18 a CP-28) |
| CP-30 a CP-34 | `AdminE2ETest` | `SolicitudServiceTest` (CP-29, CP-30, CP-32 a CP-34) |
| CP-36, CP-37 | `DevolucionE2ETest` | `DevolucionServiceTest` (CP-36 a CP-41) |
| CP-16, CP-17 | — | `tests/jmeter/Catalogo.jmx` |

Cada prueba lleva el ID del caso en su `@DisplayName`, por ejemplo: "CP-05 Bloqueo tras 3 intentos fallidos consecutivos".

**Resultado esperado de Selenium:**
- En la versión `v1.0-antes` deben fallar 3 casos, que son los defectos del backlog: CP-06 (REF-04), CP-08 (REF-01) y CP-31 (REF-05).
- En la versión final (`main` / `v2.0-despues`) deben pasar los 27 casos.

## 6. Prueba de carga con JMeter (HU-02 / CA-7)

1. Levantar la aplicación: `mvn spring-boot:run`.
2. Ejecutar los tres escenarios (50, 100 y 150 usuarios):

```bash
cd tests/jmeter
./ejecutar.sh        # Linux / Mac
ejecutar.bat         # Windows
```

Cada usuario virtual inicia sesión una vez y consulta el catálogo completo y luego filtrado por Cómputo, Electrónica y Redes, durante 5 iteraciones.

Los resultados quedan en `tests/jmeter/resultados/`:
- `escenario_XX.jtl`: los datos crudos.
- `reporte_XX/index.html`: dashboard con tablas (promedio, P90, % de error, throughput) y gráficas.

También se puede abrir `Catalogo.jmx` en la interfaz de JMeter. Ahí se ven el Summary Report, el Aggregate Report y el Response Time Graph. Los parámetros se cambian con `-Jusuarios=100 -Jrampup=20 -Jiteraciones=5`.

## 7. Análisis estático con SonarQube (antes y después)

1. Levantar SonarQube:
   ```bash
   docker run -d --name sonarqube -p 9000:9000 sonarqube:community
   ```
2. Entrar a <http://localhost:9000> (usuario `admin`, clave `admin`), crear el proyecto local `labtrack` y generar un token.
3. **Análisis ANTES** (código con defectos):
   ```bash
   git checkout v1.0-antes
   mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<TOKEN>
   ```
   Capturar el dashboard: Quality Gate, Bugs, Vulnerabilities, Security Hotspots y Code Smells.
4. **Análisis DESPUÉS** (código corregido):
   ```bash
   git checkout main
   mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<TOKEN>
   ```
   Capturar de nuevo el dashboard y comparar con el análisis anterior.

> En la edición Community, varios riesgos de seguridad aparecen como **Security Hotspots**
> (por ejemplo el SQL armado por concatenación o la contraseña escrita en el código).
> Hay que revisarlos en la pestaña *Security Hotspots*.

## 8. Backlog de refactorización

| ID | Defecto detectado | Origen | Solución aplicada | Commit |
|----|-------------------|--------|-------------------|--------|
| REF-01 | Consulta SQL armada por concatenación en el login: permite inyección SQL | SonarQube (S2077) / CP-08 | Consulta parametrizada de JPA (`findByCodigo`) | `df447dd` |
| REF-02 | Contraseñas guardadas y comparadas en texto plano | Revisión de código | Hash BCrypt (`SeguridadConfig`, `passwordEncoder.matches`) | `df447dd` |
| REF-03 | Credenciales escritas en el código y en `application.properties` | SonarQube (S2068) | Lectura desde variables de entorno | `df447dd` |
| REF-04 | El contador de intentos no se reiniciaba tras un ingreso correcto, así que bloqueaba con fallos no consecutivos | CP-06 (Selenium) | Reinicio del contador al iniciar sesión | `df447dd` |
| REF-05 | Un estudiante podía entrar al panel del encargado y aprobar reservas | CP-31 (Selenium) | Control de acceso por rol en `/admin/**` (HTTP 403) | `97f37ca` |
| REF-06 | Se podían aprobar dos solicitudes del mismo equipo y horario (doble préstamo) | CP-29 | Validación de cruce al aprobar | `7169b42` |
| REF-07 | `System.out.println`, `printStackTrace()` y `Optional.get()` sin verificar | SonarQube (S106, S1148, S3655) | Logger SLF4J y `orElseThrow()` | `5bd9a58` |
| REF-08 | Lógica de cruce duplicada en bucles anidados que cargaban todas las reservas en memoria | SonarQube (complejidad cognitiva, S1066) | Consultas JPQL y la clase `ValidadorCruces` | `7169b42` |
| REF-09 | Inyección por atributo (`@Autowired`) y literales repetidos | SonarQube (S6813, S1192) | Inyección por constructor y constantes | `5bd9a58` |

Versiones etiquetadas:
- `v1.0-antes`: código original, para el primer análisis SonarQube.
- `v2.0-despues`: código corregido.

Para ver cada corrección: `git show <commit>`.
