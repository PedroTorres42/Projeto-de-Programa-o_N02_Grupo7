package br.com.unit.modulo_avaliacao_relatorio.Service;


import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.CursoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;


@Service
@RequiredArgsConstructor
public class CursoService {


    private final CursoRepositorio cursoRepositorio;


    @Transactional
    public Curso criarCurso(Curso curso) {
        return cursoRepositorio.save(curso);
    }


    @Transactional(readOnly = true)
    public List<Curso> listarCursos() {
        return cursoRepositorio.findAllDistinct();
    }
}
