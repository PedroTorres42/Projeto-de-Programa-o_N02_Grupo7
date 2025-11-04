package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
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
    

    @Transactional
    public void criarFormulario(String titulo, List<Pergunta> perguntas) {
        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);

        // Garanta o lado "dono" (Pergunta) apontando para o formulário
        if (perguntas != null) {
            for (Pergunta p : perguntas) {
                if (p != null) {
                    p.setFormulario(formulario);
                }
            }
        }
        formulario.setPerguntas(perguntas);

        // Deixe o cascade do Formulario persistir as Perguntas
        formularioRepositorio.save(formulario);
    }


    @Transactional(readOnly = true)
    public List<Formulario> listarFormularios() {
        return formularioRepositorio.findAll();
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
        Formulario formulario = pegarFormulario(id);
        formulario.setTitulo(titulo);

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

        // Cria perguntas padronizadas (escala 1 a 5)
        List<Pergunta> perguntas = new ArrayList<>();
        perguntas.add(novaPergunta("Qualidade do conteúdo"));
        perguntas.add(novaPergunta("Didática do instrutor"));
        perguntas.add(novaPergunta("Carga horária"));
        perguntas.add(novaPergunta("Organização"));
        perguntas.add(novaPergunta("Avaliação geral"));

        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setTipo(Formulario.TipoFormulario.ALUNO);

        // owning side das perguntas
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

        // Perguntas específicas para o instrutor (1 a 5)
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

    private Pergunta novaPergunta(String texto) {
        Pergunta p = new Pergunta();
        p.setTexto(texto);
        p.setTipo(Pergunta.TipoPergunta.OUTRO);
        return p;
    }
}
