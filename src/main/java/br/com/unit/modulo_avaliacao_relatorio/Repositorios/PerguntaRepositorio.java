package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerguntaRepositorio extends JpaRepository<Pergunta, Long> {
    Optional<Pergunta> findFirstByTipo(Pergunta.TipoPergunta tipo);
}
