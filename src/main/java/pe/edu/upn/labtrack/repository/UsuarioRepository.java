package pe.edu.upn.labtrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upn.labtrack.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCodigo(String codigo);
}
