package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Resposta;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.RespostaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespostaRepositorio extends JpaRepository<Resposta, RespostaId> {
}
