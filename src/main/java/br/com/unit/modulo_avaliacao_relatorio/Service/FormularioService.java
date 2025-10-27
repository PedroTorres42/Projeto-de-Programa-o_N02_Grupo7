package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.FormularioRepositorio;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.PerguntaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class FormularioService {


    private final FormularioRepositorio formularioRepositorio;
    private final PerguntaRepositorio perguntaRepositorio;


    public Formulario criarFormulario(String titulo, List<Pergunta> perguntas) {
        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setPerguntas(perguntas);


        perguntaRepositorio.saveAll(perguntas);
        return formularioRepositorio.save(formulario);
    }


    public List<Formulario> listarFormularios() {
        return formularioRepositorio.findAll();
    }
}
