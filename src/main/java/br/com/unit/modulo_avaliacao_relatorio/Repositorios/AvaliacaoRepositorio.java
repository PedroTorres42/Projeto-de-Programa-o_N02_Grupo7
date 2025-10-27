package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepositorio extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByCursoId(Long id);
    List<Avaliacao> findByInstrutorId(Long id);
}
