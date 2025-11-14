package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.PerguntaRepositorio;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.FormularioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FormularioService {


    private final FormularioRepositorio formularioRepositorio;
    private final PerguntaRepositorio perguntaRepositorio;
    

    @Transactional
    public void criarFormulario(String titulo, List<Pergunta> perguntas) {
        criarFormulario(titulo, perguntas, null);
    }

    @Transactional
    public void criarFormulario(String titulo, List<Pergunta> perguntas, Formulario.TipoFormulario tipo) {
        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setTipo(tipo);

        if (perguntas != null) {
            for (Pergunta p : perguntas) {
                if (p != null) {
                    p.setFormulario(formulario);
                }
            }
        }
        formulario.setPerguntas(perguntas);
        formularioRepositorio.save(formulario);
    }


    @Transactional(readOnly = true)
    public List<Formulario> listarFormularios() {
        return formularioRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<Formulario> listarFormulariosComPerguntas() {
        return formularioRepositorio.findAllWithPerguntas();
    }

    @Transactional(readOnly = true)
    public List<Formulario> listarFormulariosPorTipo(Formulario.TipoFormulario tipo) {
        if (tipo == null) return listarFormularios();
        return formularioRepositorio.findByTipoWithPerguntas(tipo);
    }

    @Transactional(readOnly = true)
    public Formulario pegarFormulario(Long id) {
        return formularioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Formulario não encontrado"));
    }

    @Transactional
    public void deletarFormulario(Long id) {
        formularioRepositorio.deleteById(id);
    }

    @Transactional
    public Formulario editarFormulario(Long id, List<Pergunta> perguntas, String titulo) {
        return editarFormulario(id, perguntas, titulo, null);
    }

    @Transactional
    public Formulario editarFormulario(Long id, List<Pergunta> perguntas, String titulo, Formulario.TipoFormulario tipo) {
        Formulario formulario = pegarFormulario(id);
        formulario.setTitulo(titulo);
        if (tipo != null) {
            formulario.setTipo(tipo);
        }

        if (perguntas != null) {
            for (Pergunta p : perguntas) {
                if (p != null) {
                    p.setFormulario(formulario);
                }
            }
        }
        formulario.setPerguntas(perguntas);
        return formularioRepositorio.save(formulario);
    }

    @Transactional
    public Formulario obterOuCriarFormularioAlunoPadrao() {
        final String titulo = "Avaliação do Curso e Instrutor (Aluno)";
        Optional<Formulario> existente = formularioRepositorio.findByTituloAndTipo(
                titulo, Formulario.TipoFormulario.ALUNO);
        if (existente.isPresent()) {
            return existente.get();
        }

        List<Pergunta> perguntas = new ArrayList<>();
        perguntas.add(novaPergunta("Qualidade do conteúdo"));
        perguntas.add(novaPergunta("Didática do instrutor"));
        perguntas.add(novaPergunta("Carga horária"));
        perguntas.add(novaPergunta("Organização"));
        perguntas.add(novaPergunta("Avaliação geral"));

        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setTipo(Formulario.TipoFormulario.ALUNO);

        for (Pergunta p : perguntas) {
            p.setFormulario(formulario);
        }
        formulario.setPerguntas(perguntas);

        return formularioRepositorio.save(formulario);
    }

    @Transactional
    public Formulario obterOuCriarFormularioInstrutorPadrao() {
        final String titulo = "Avaliação do Instrutor (Aluno)";
        Optional<Formulario> existente = formularioRepositorio.findByTituloAndTipo(
                titulo, Formulario.TipoFormulario.INSTRUTOR);
        if (existente.isPresent()) {
            return existente.get();
        }

        List<Pergunta> perguntas = new ArrayList<>();
        perguntas.add(novaPergunta("Didática"));
        perguntas.add(novaPergunta("Domínio do conteúdo"));
        perguntas.add(novaPergunta("Clareza"));
        perguntas.add(novaPergunta("Assiduidade"));
        perguntas.add(novaPergunta("Interação com a turma"));
        perguntas.add(novaPergunta("Capacidade de resolver dúvidas"));

        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setTipo(Formulario.TipoFormulario.INSTRUTOR);

        for (Pergunta p : perguntas) {
            p.setFormulario(formulario);
        }
        formulario.setPerguntas(perguntas);

        return formularioRepositorio.save(formulario);
    }

    @Transactional
    public Formulario obterOuCriarFormularioCursoPadrao() {
        final String titulo = "Avaliação do Curso";
        Optional<Formulario> existente = formularioRepositorio.findByTituloAndTipo(
                titulo, Formulario.TipoFormulario.CURSO);
        if (existente.isPresent()) {
            return existente.get();
        }

        List<Pergunta> perguntas = new ArrayList<>();
        perguntas.add(novaPergunta("Qualidade do conteúdo programático"));
        perguntas.add(novaPergunta("Organização do curso"));
        perguntas.add(novaPergunta("Carga horária adequada"));
        perguntas.add(novaPergunta("Material didático"));
        perguntas.add(novaPergunta("Infraestrutura"));
        perguntas.add(novaPergunta("Atendimento às expectativas"));

        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setTipo(Formulario.TipoFormulario.CURSO);

        for (Pergunta p : perguntas) {
            p.setFormulario(formulario);
        }
        formulario.setPerguntas(perguntas);

        return formularioRepositorio.save(formulario);
    }

    private Pergunta novaPergunta(String texto) {
        Pergunta p = new Pergunta();
        p.setTexto(texto);
        p.setTipo(Pergunta.TipoPergunta.OUTRO);
        return p;
    }

    @Transactional
    public Pergunta obterOuCriarPerguntaFrequencia() {
        return perguntaRepositorio.findAll().stream()
                .filter(pr -> pr.getTipo() == Pergunta.TipoPergunta.FREQUENCIA)
                .findFirst()
                .orElseGet(() -> {
                    Pergunta p = new Pergunta();
                    p.setTexto("Frequência (%)");
                    p.setTipo(Pergunta.TipoPergunta.FREQUENCIA);
                    return perguntaRepositorio.save(p);
                });
    }
}
