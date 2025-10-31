package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepositorio extends JpaRepository<Avaliacao, Long> {
    @Query("SELECT a FROM Avaliacao a WHERE a.aluno.id = :alunoId")
    List<Avaliacao> findByCursoId(@Param("alunoId") Long alunoId);
    @Query("SELECT a FROM Avaliacao a WHERE a.instrutor.id = :id")
    List<Avaliacao> findByInstrutorId(@Param("instrutorId") String instrutorId);
    @Query("SELECT p FROM Pergunta p WHERE p.TipoPergunta = :tipo")
    Optional<Pergunta> findPerguntaByType(@Param("tipo") String tipo);
    @Query("SELECT a FROM Avaliacao a WHERE a.aluno.id = :alunoId")
    List<Avaliacao> findByAlunoId(@Param("alunoId") String alunoId);

}
