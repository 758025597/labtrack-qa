package pe.edu.upn.labtrack.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pe.edu.upn.labtrack.model.Categoria;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HU-02 Catálogo y filtrado (Selenium)")
class CatalogoE2ETest extends BaseE2ETest {

    @BeforeEach
    void ingresar() {
        login(ANA, CLAVE_ESTUDIANTE);
        abrir("/catalogo");
    }

    private List<String> columna(String clase) {
        return driver.findElements(By.cssSelector("#tabla-equipos ." + clase)).stream()
                .map(WebElement::getText).map(String::trim).toList();
    }

    @Test
    @DisplayName("CP-10 El catálogo muestra nombre, código, categoría y estado")
    void cp10ColumnasDelCatalogo() {
        List<String> cabeceras = driver.findElements(By.cssSelector("#tabla-equipos th")).stream()
                .map(WebElement::getText).toList();
        assertThat(cabeceras).contains("Nombre", "Código de inventario", "Categoría", "Estado");
        assertThat(driver.findElements(By.cssSelector("#tabla-equipos .fila-equipo"))).hasSize(12);
        assertThat(columna("col-estado")).allMatch(e -> e.equals("Disponible") || e.equals("Prestado"));
    }

    @Test
    @DisplayName("CP-11 Filtro por categoría 'Cómputo'")
    void cp11FiltroComputo() {
        filtrarCategoria("COMPUTO");
        assertThat(columna("col-categoria")).isNotEmpty().containsOnly("Cómputo");
    }

    @Test
    @DisplayName("CP-12 Filtro por categoría 'Electrónica'")
    void cp12FiltroElectronica() {
        filtrarCategoria("ELECTRONICA");
        assertThat(columna("col-categoria")).isNotEmpty().containsOnly("Electrónica");
    }

    @Test
    @DisplayName("CP-13 Filtro por categoría 'Redes'")
    void cp13FiltroRedes() {
        filtrarCategoria("REDES");
        assertThat(columna("col-categoria")).isNotEmpty().containsOnly("Redes");
    }

    @Test
    @DisplayName("CP-14 Filtro sin resultados muestra el mensaje")
    void cp14FiltroSinResultados() {
        equipoRepository.deleteAll(equipoRepository.findByCategoriaOrderByCodigoAsc(Categoria.REDES));
        filtrarCategoria("REDES");
        assertThat(texto(By.id("mensaje-vacio"))).isEqualTo("No hay equipos disponibles en esta categoría");
        assertThat(existe(By.id("tabla-equipos"))).isFalse();
    }
}
