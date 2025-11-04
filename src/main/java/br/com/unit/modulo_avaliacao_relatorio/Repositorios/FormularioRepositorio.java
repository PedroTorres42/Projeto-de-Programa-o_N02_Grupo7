package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormularioRepositorio extends JpaRepository<Formulario, Long> {
	Optional<Formulario> findByTituloAndTipo(String titulo, Formulario.TipoFormulario tipo);
	
	@Query("SELECT DISTINCT f FROM Formulario f LEFT JOIN FETCH f.perguntas")
	List<Formulario> findAllWithPerguntas();
	
	@Query("SELECT f FROM Formulario f LEFT JOIN FETCH f.perguntas WHERE f.id = :id")
	Optional<Formulario> findByIdWithPerguntas(@Param("id") Long id);
}
