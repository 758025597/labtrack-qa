package pe.edu.upn.labtrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj del sistema inyectable: permite probar las reglas que dependen de la hora
 * (reservas en el pasado, penalización por devolución tardía) con un reloj fijo.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Lima"));
    }
}
