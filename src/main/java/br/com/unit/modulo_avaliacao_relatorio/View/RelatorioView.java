package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Service.RelatorioService;

import javax.swing.*;

public class RelatorioView extends JFrame {
    private final RelatorioService relatorioService;
    public RelatorioView(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }
    //TODO: Seleção de qual tipo de relatório(Comparativo[Instrutor, Curso], Individual[Aluno, Curso, Instrutor]) gerar
    //TODO: Exportar para PDF
    //TODO: Criar e exportar CSV
    //TODO: Filtrar funcionalidades com base no usuário logado
    //TODO: Visualização de relatórios salvos
    //Administrador: Pode criar, consultar, editar e deletar qualquer tipo de relatorio
    //Instrutor: Pode visualizar somente os próprios relatórios
}
