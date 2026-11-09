package pe.edu.upn.labtrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findByCodigo(String codigo);

    List<Equipo> findAllByOrderByCodigoAsc();

    List<Equipo> findByCategoriaOrderByCodigoAsc(Categoria categoria);
}
