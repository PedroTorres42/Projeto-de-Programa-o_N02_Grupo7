package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
	Optional<Usuario> findByEmail(String email);
}
