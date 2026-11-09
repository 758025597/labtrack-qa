package pe.edu.upn.labtrack.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Rol;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;
import pe.edu.upn.labtrack.service.CatalogoService;
import pe.edu.upn.labtrack.service.EquipoVista;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.edu.upn.labtrack.unit.DatosPrueba.h;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.HOY;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.MANANA;

@DataJpaTest
@Import({RelojFijoConfig.class, CatalogoService.class})
@DisplayName("HU-02 Catálogo y filtrado")
class CatalogoServiceTest {

    @Autowired
    private CatalogoService catalogoService;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private EquipoRepository equipos;
    @Autowired
    private ReservaRepository reservas;

    private Equipo laptop;

    @BeforeEach
    void datos() {
        laptop = equipos.save(new Equipo("EQ-001", "Laptop Dell", Categoria.COMPUTO));
        equipos.save(new Equipo("EQ-005", "Osciloscopio", Categoria.ELECTRONICA));
    }

    @Test
    @DisplayName("CP-11/12 Filtro exacto por categoría")
    void cp11FiltroPorCategoria() {
        List<EquipoVista> computo = catalogoService.listar(Categoria.COMPUTO);
        assertThat(computo).extracting(v -> v.getEquipo().getCategoria()).containsOnly(Categoria.COMPUTO);
        assertThat(catalogoService.listar(null)).hasSize(2);
    }

    @Test
    @DisplayName("CP-14 Categoría sin equipos devuelve lista vacía")
    void cp14CategoriaVacia() {
        assertThat(catalogoService.listar(Categoria.REDES)).isEmpty();
    }

    @Test
    @DisplayName("CP-15 El estado pasa a Prestado cuando empieza una reserva aprobada")
    void cp15EstadoPrestado() {
        Usuario ana = usuarios.save(new Usuario("N00123456", "Ana Torres", "x", Rol.ESTUDIANTE));
        DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(10), h(12), EstadoReserva.APROBADA);
        assertThat(catalogoService.estadoActual(laptop)).isEqualTo(EstadoEquipo.DISPONIBLE);

        DatosPrueba.reserva(reservas, ana, laptop, HOY, h(9), h(11), EstadoReserva.APROBADA);
        assertThat(catalogoService.estadoActual(laptop)).isEqualTo(EstadoEquipo.PRESTADO);
    }
}
