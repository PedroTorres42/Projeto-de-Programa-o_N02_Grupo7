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
    
    @Transactional(readOnly = true)
    public List<Curso> listarCursosUnicosOrdenados() {
        List<Curso> base = cursoRepositorio.findAllDistinct();
        Map<String, Curso> porNome = new LinkedHashMap<>();
        for (Curso c : base) {
            String key = normalizarNome(c != null ? c.getNome() : null);
            porNome.putIfAbsent(key, c);
        }
        return new ArrayList<>(porNome.values());
    }

    private String normalizarNome(String nome) {
        if (nome == null) return "";
        String n = nome.trim();
        n = n.replaceAll("\\s+", " ");
        return n.toLowerCase(java.util.Locale.ROOT);
    }


    @Transactional(readOnly = true)
    public Optional<Curso> buscarPorId(Long id) {
        return cursoRepositorio.findById(id);
    }


    @Transactional
    public void deletarCurso(Long id) {
        cursoRepositorio.deleteById(id);
    }
}
