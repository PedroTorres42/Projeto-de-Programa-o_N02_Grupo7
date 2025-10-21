package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Relatorio;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.*;

@Service
public class RelatorioService {
    private final RelatorioRepositorio relatorioRepositorio;
    private final AvaliacaoRepositorio avaliacaoRepositorio;
    private final CursoRepositorio cursoRepositorio;

    public RelatorioService(RelatorioRepositorio relatorioRepositorio, AvaliacaoRepositorio avaliacaoRepositorio,CursoRepositorio cursoRepositorio) {
        this.relatorioRepositorio = relatorioRepositorio;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.cursoRepositorio = cursoRepositorio;
    }

    public Relatorio pegarRelatorioPorId(Long id){
        return relatorioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Relatório não encontrado"));
    }

    public Iterable<Relatorio> pegarTodosRelatorios(){
        return relatorioRepositorio.findAll();
    }

    public void salvarRelatorio(Relatorio relatorio){
        relatorioRepositorio.save(relatorio);
    }

    public void editarRelatorio(Long id){
        Avaliacao avaliacao = avaliacaoRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Relatório nâo encontrado"));
    }
    public void excluirRelatorio(Long id){
        relatorioRepositorio.deleteById(id);
    }

    public Relatorio gerarRelatorioComparativoPorCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId).orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(curso);
        String nomeCurso = curso.getNome();
        byte[] pdf = montarPdfComparativo(
                "Relatório Comparativo por Curso",
                "Curso: " + nomeCurso,
                agruparPorInstrutor(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }
}
