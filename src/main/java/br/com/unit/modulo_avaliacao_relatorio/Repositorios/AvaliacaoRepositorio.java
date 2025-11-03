package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.AvaliacaoCsvRow;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepositorio extends JpaRepository<Avaliacao, Long> {
    @Query("SELECT a FROM Avaliacao a WHERE a.curso.id = :cursoId")
    List<Avaliacao> findByCursoId(@Param("cursoId") Long cursoId);
    @Query("SELECT a FROM Avaliacao a WHERE a.instrutor.id = :instrutorId")
    List<Avaliacao> findByInstrutorId(@Param("instrutorId") String instrutorId);
    @Query("SELECT p FROM Pergunta p WHERE p.tipo = :tipo")
    Optional<Pergunta> findPerguntaByType(@Param("tipo") Pergunta.TipoPergunta tipo);
    @Query("SELECT a FROM Avaliacao a WHERE a.aluno.id = :alunoId")
    List<Avaliacao> findByAlunoId(@Param("alunoId") String alunoId);

        @Query("""
                 select distinct a from Avaliacao a
                     left join fetch a.notas n
                     left join fetch a.curso c
                     left join fetch a.instrutor i
                     left join fetch a.aluno al
                     left join fetch a.feedback f
               """)
        List<Avaliacao> findAllComAssociacoes();

        @Query("select count(a) from Avaliacao a")
        long totalAvaliacoes();

        @Query("""
                 select new br.com.unit.modulo_avaliacao_relatorio.Modelos.AvaliacaoCsvRow(
                     a.id,
                     c.nome,
                     i.nome,
                     al.nome,
                     avg(n.nota),
                     a.data
                 )
                 from Avaliacao a
                     left join a.curso c
                     left join a.instrutor i
                     left join a.aluno al
                     left join a.notas n
                 group by a.id, c.nome, i.nome, al.nome, a.data
               """)
        List<AvaliacaoCsvRow> findCsvRows();

}