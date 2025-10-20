package br.com.unit.modulo_avaliacao_relatorio.Service;


import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.CursoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@ServiceProjeto-de-Programa-o_N02_Grupo7/src/main/java/Servicos/AvaliacaoService.java Projeto-de-Programa-o_N02_Grupo7/src/main/java/Servicos/CursoService.java Projeto-de-Programa-o_N02_Grupo7/src/main/java/Servicos/FormularioService.java Projeto-de-Programa-o_N02_Grupo7/src/main/java/Servicos/RelatorioService.java Projeto-de-Programa-o_N02_Grupo7/src/main/java/Servicos/UsuarioService.java
@RequiredArgsConstructor
public class CursoService {


    private final CursoRepositorio cursoRepositorio;


    public Curso criarCurso(Curso curso) {
        return cursoRepositorio.save(curso);
    }


    public List<Curso> listarCursos() {
        return cursoRepositorio.findAll();
    }


    public Optional<Curso> buscarPorId(String id) {
        return cursoRepositorio.findById(id);
    }


    public void deletarCurso(String id) {
        cursoRepositorio.deleteById(id);
    }
}
