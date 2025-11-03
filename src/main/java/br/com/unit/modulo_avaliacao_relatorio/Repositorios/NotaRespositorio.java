package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Nota;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotaRespositorio extends JpaRepository<Nota, Long> {

    @Query("SELECT r FROM Nota r WHERE r.avaliacao.id = :avaliacaoId")
    List<Nota> buscarRespostasPorAvaliacao(@Param("avaliacaoId") Long avaliacaoId);

}
