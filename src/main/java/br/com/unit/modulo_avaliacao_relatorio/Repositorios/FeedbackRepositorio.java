package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepositorio extends JpaRepository<Feedback, Long> {
}
