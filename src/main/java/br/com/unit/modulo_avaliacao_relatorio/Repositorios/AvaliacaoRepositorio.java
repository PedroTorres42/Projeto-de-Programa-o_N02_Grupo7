package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepositorio extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByCursoId(Long id);
    List<Avaliacao> findByInstrutorId(String id);
    Optional<Avaliacao> findById(String id);
    boolean existsById(String id);
    void deleteById(String id);

}
