package pe.edu.upn.labtrack.unit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Reloj fijo para las pruebas: martes 10/11/2026 a las 10:00 (hora de Lima). */
@TestConfiguration
public class RelojFijoConfig {

    public static final ZoneId LIMA = ZoneId.of("America/Lima");
    public static final LocalDate HOY = LocalDate.of(2026, 11, 10);
    public static final LocalDate MANANA = HOY.plusDays(1);
    public static final LocalTime AHORA = LocalTime.of(10, 0);

    @Bean
    public Clock clock() {
        return Clock.fixed(ZonedDateTime.of(HOY, AHORA, LIMA).toInstant(), LIMA);
    }
}
