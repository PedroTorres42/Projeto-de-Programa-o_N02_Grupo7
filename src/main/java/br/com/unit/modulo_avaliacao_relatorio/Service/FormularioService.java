package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.FormularioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class FormularioService {


    private final FormularioRepositorio formularioRepositorio;
    


    public Formulario criarFormulario(String titulo, List<Pergunta> perguntas) {
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
        return formularioRepositorio.save(formulario);
    }


    public List<Formulario> listarFormularios() {
        return formularioRepositorio.findAll();
    }

    public Formulario pegarFormulario(Long id) {
        return formularioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Formulario não encontrado"));
    }

    public void deletarFormulario(Long id) {
        formularioRepositorio.deleteById(id);
    }

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
}
