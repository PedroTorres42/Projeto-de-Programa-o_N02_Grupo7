package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormularioRepositorio extends JpaRepository<Formulario, Long> {
	Optional<Formulario> findByTituloAndTipo(String titulo, Formulario.TipoFormulario tipo);
}
