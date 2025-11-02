package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< Updated upstream
public interface AvaliacaoRepositorio extends JpaRepository<Avaliacao, Long> {}
=======
import java.util.List;

public interface AvaliacaoRepositorio extends JpaRepository<Avaliacao, Long> {

    @Query("SELECT a FROM Avaliacao a WHERE a.curso.id = :cursoId")
    List<Avaliacao> findByCursoId(@Param("cursoId") Long cursoId);

    @Query("SELECT a FROM Avaliacao a WHERE a.instrutor.id = :instrutorId")
    List<Avaliacao> findByInstrutorId(@Param("instrutorId") String instrutorId);

    @Query("SELECT a FROM Avaliacao a WHERE a.aluno.id = :alunoId")
    List<Avaliacao> findByAlunoId(@Param("alunoId") String alunoId);
}
>>>>>>> Stashed changes
