package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Resposta;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.RespostaId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RespostaRepositorio extends JpaRepository<Resposta, RespostaId> {

    @Query("SELECT r FROM Resposta r WHERE r.avaliacao.id = :avaliacaoId")
    List<Resposta> buscarRespostasPorAvaliacao(@Param("avaliacaoId") Long avaliacaoId);

}
