package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CursoRepositorio extends JpaRepository<Curso, Long> {

	@Query("select distinct c from Curso c")
	List<Curso> findAllDistinct();
}
