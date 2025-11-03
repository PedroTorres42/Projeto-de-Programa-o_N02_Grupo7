package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Nota;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.MediaPorCurso;

public interface NotaRespositorio extends JpaRepository<Nota, Long> {

    @Query("SELECT r FROM Nota r WHERE r.avaliacao.id = :avaliacaoId")
    List<Nota> buscarRespostasPorAvaliacao(@Param("avaliacaoId") Long avaliacaoId);

    @Query("select coalesce(avg(n.nota), 0) from Nota n where n.nota is not null")
    Double mediaGeralNotas();

    @Query("""
                 select coalesce(c.nome, 'Curso ?') as curso, avg(n.nota) as media
                 from Nota n
                     join n.avaliacao a
                     left join a.curso c
                 where n.nota is not null
                 group by coalesce(c.nome, 'Curso ?')
                 """)
    List<Object[]> mediaPorCurso();

        @Query("""
                 select new br.com.unit.modulo_avaliacao_relatorio.Modelos.MediaPorCurso(
                     coalesce(c.nome, 'Curso ?'), avg(n.nota)
                 )
                 from Nota n
                     join n.avaliacao a
                     left join a.curso c
                 where n.nota is not null
                 group by coalesce(c.nome, 'Curso ?')
             """)
        List<MediaPorCurso> mediasPorCursoDto();


    @Query("SELECT n.nota FROM Nota n WHERE n.avaliacao = :avaliacao " +
           "AND n.pergunta.tipo = 'FREQUENCIA' AND n.nota IS NOT NULL")
    Optional<Integer> findFrequenciaByAvaliacao(@Param("avaliacao") Avaliacao avaliacao);

}
