package br.com.unit.modulo_avaliacao_relatorio.Service;


import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.CursoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CursoService {


    private final CursoRepositorio cursoRepositorio;


    public Curso criarCurso(Curso curso) {
        return cursoRepositorio.save(curso);
    }


    public List<Curso> listarCursos() {
        return cursoRepositorio.findAll();
    }


    public Optional<Curso> buscarPorId(Long id) {
        return cursoRepositorio.findById(id);
    }


    public void deletarCurso(Long id) {
        cursoRepositorio.deleteById(id);
    }
}
