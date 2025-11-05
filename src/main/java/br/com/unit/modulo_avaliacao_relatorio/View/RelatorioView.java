package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Relatorio;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Service.CursoService;
import br.com.unit.modulo_avaliacao_relatorio.Service.RelatorioService;
import br.com.unit.modulo_avaliacao_relatorio.Service.UsuarioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Lazy
@Component
public class RelatorioView extends JFrame {
    private final RelatorioService relatorioService;
    private final CursoService cursoService;
    private final UsuarioService usuarioService;
    
    private JTable tabelaRelatorios;
    private DefaultTableModel modeloTabela;
    private JComboBox<FiltroTipo> comboFiltro;
    private JButton btnExportarPDF;
    private JButton btnExportarCSV;
    private JButton btnAtualizar;
    private JButton btnExcluir;
    private JButton btnGerarNovo;
    
    private List<Relatorio> relatoriosCarregados = new ArrayList<>();
    
    private enum FiltroTipo {
        TODOS("Todos"),
        CURSO("Curso"),
        INSTRUTOR("Instrutor"),
        ALUNO("Aluno");
        
        private final String nome;
        
        FiltroTipo(String nome) {
            this.nome = nome;
        }
        
        @Override
        public String toString() {
            return nome;
        }
    }
    
    public RelatorioView(RelatorioService relatorioService, CursoService cursoService, UsuarioService usuarioService) {
        this.relatorioService = relatorioService;
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;
        setTitle("Gerenciamento de Relatórios");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        carregarRelatorios();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        painelSuperior.add(new JLabel("Filtrar por tipo:"));
        comboFiltro = new JComboBox<>(FiltroTipo.values());
        comboFiltro.addActionListener(e -> filtrarRelatorios());
        painelSuperior.add(comboFiltro);
        
        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarRelatorios());
        painelSuperior.add(btnAtualizar);
        
        btnGerarNovo = new JButton("Gerar Novo Relatório");
        btnGerarNovo.addActionListener(e -> abrirDialogoGerarRelatorio());
        painelSuperior.add(btnGerarNovo);
        
        add(painelSuperior, BorderLayout.NORTH);
        
        String[] colunas = {"ID", "Tipo", "Data", "Tamanho (aprox.)"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaRelatorios = new JTable(modeloTabela);
        tabelaRelatorios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaRelatorios.setFillsViewportHeight(true);
        tabelaRelatorios.getTableHeader().setReorderingAllowed(false);
        
        tabelaRelatorios.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaRelatorios.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabelaRelatorios.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabelaRelatorios.getColumnModel().getColumn(3).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(tabelaRelatorios);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        painelInferior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        btnExportarPDF = new JButton("Exportar PDF Selecionado");
        btnExportarPDF.addActionListener(e -> exportarPDF());
        painelInferior.add(btnExportarPDF);
        
        btnExportarCSV = new JButton("Exportar CSV (Avaliações)");
        btnExportarCSV.addActionListener(e -> exportarCSV());
        painelInferior.add(btnExportarCSV);
        
        btnExcluir = new JButton("Excluir Selecionado");
        btnExcluir.addActionListener(e -> excluirRelatorio());
        btnExcluir.setForeground(Color.RED);
        painelInferior.add(btnExcluir);
        
        add(painelInferior, BorderLayout.SOUTH);
        
        tabelaRelatorios.getSelectionModel().addListSelectionListener(e -> {
            boolean temSelecao = tabelaRelatorios.getSelectedRow() != -1;
            btnExportarPDF.setEnabled(temSelecao);
            btnExcluir.setEnabled(temSelecao);
        });
        
        btnExportarPDF.setEnabled(false);
        btnExcluir.setEnabled(false);
    }
    
    private void carregarRelatorios() {
        try {
            relatoriosCarregados.clear();
            modeloTabela.setRowCount(0);
            
            java.awt.event.ActionListener[] listeners = comboFiltro.getActionListeners();
            for (java.awt.event.ActionListener listener : listeners) {
                comboFiltro.removeActionListener(listener);
            }
            
            comboFiltro.setSelectedItem(FiltroTipo.TODOS);
            
            for (java.awt.event.ActionListener listener : listeners) {
                comboFiltro.addActionListener(listener);
            }
            
            Iterable<Relatorio> relatorios = relatorioService.pegarTodosRelatorios();
            
            for (Relatorio rel : relatorios) {
                relatoriosCarregados.add(rel);
                adicionarRelatorioNaTabela(rel);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar relatórios: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarRelatorios() {
        try {
            relatoriosCarregados.clear();
            modeloTabela.setRowCount(0);
            
            FiltroTipo filtro = (FiltroTipo) comboFiltro.getSelectedItem();
            List<Relatorio> relatorios;
            
            if (filtro == null || filtro == FiltroTipo.TODOS) {
                relatorios = new ArrayList<>();
                relatorioService.pegarTodosRelatorios().forEach(relatorios::add);
            } else {
                switch (filtro) {
                    case CURSO:
                        relatorios = relatorioService.filtrarRelatoriosPorCurso();
                        break;
                    case INSTRUTOR:
                        relatorios = relatorioService.filtrarRelatoriosPorInstrutor();
                        break;
                    case ALUNO:
                        relatorios = relatorioService.filtrarRelatoriosPorAluno();
                        break;
                    default:
                        relatorios = new ArrayList<>();
                        relatorioService.pegarTodosRelatorios().forEach(relatorios::add);
                }
            }
            
            for (Relatorio rel : relatorios) {
                relatoriosCarregados.add(rel);
                adicionarRelatorioNaTabela(rel);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao filtrar relatórios: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void adicionarRelatorioNaTabela(Relatorio rel) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFormatada = rel.getData() != null ? rel.getData().format(formatter) : "—";
        
        String tamanho = "—";
        if (rel.getDocumento() != null && !rel.getDocumento().isEmpty()) {
            long tamanhoBytes = (long) (rel.getDocumento().length() / 1.33);
            tamanho = formatarTamanho(tamanhoBytes);
        }
        
        modeloTabela.addRow(new Object[]{
            rel.getId(),
            rel.getTipo() != null ? rel.getTipo().toString() : "—",
            dataFormatada,
            tamanho
        });
    }
    
    private String formatarTamanho(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    private void exportarPDF() {
        int linhaSelecionada = tabelaRelatorios.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um relatório para exportar.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Relatorio relatorio = relatoriosCarregados.get(linhaSelecionada);
            
            String nomeArquivo = gerarNomeArquivo(relatorio, "pdf");
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar PDF");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
            fileChooser.setSelectedFile(new File(nomeArquivo));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                
                // Adicionar extensão .pdf se não tiver
                if (!arquivo.getName().toLowerCase().endsWith(".pdf")) {
                    arquivo = new File(arquivo.getAbsolutePath() + ".pdf");
                }
                
                relatorioService.exportarDocumentoRelatorio(relatorio.getId(), arquivo);
                
                JOptionPane.showMessageDialog(this,
                    "PDF exportado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao exportar PDF: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportarCSV() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String dataAtual = LocalDate.now().format(formatter);
            String nomeArquivo = "planilha_Avaliacoes_" + dataAtual + ".csv";
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar CSV de Avaliações");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
            fileChooser.setSelectedFile(new File(nomeArquivo));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                
                if (!arquivo.getName().toLowerCase().endsWith(".csv")) {
                    arquivo = new File(arquivo.getAbsolutePath() + ".csv");
                }
                
                relatorioService.exportarCsv(arquivo);
                
                JOptionPane.showMessageDialog(this,
                    "CSV exportado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao exportar CSV: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void excluirRelatorio() {
        int linhaSelecionada = tabelaRelatorios.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um relatório para excluir.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Relatorio relatorio = relatoriosCarregados.get(linhaSelecionada);
            
            int confirmacao = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir o relatório ID " + relatorio.getId() + "?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirmacao == JOptionPane.YES_OPTION) {
                relatorioService.excluirRelatorio(relatorio.getId());
                
                JOptionPane.showMessageDialog(this,
                    "Relatório excluído com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                
                carregarRelatorios();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao excluir relatório: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirDialogoGerarRelatorio() {
        String[] opcoes = {
            "Comparar Instrutores de um Curso",
            "Comparar Cursos de um Instrutor",
            "Relatório Individual de Aluno",
            "Comparar Alunos de um Curso",
            "Relatório Detalhado de Curso",
            "Relatório Detalhado de Instrutor"
        };
        
        String escolha = (String) JOptionPane.showInputDialog(
            this,
            "Selecione o tipo de relatório a gerar:",
            "Gerar Novo Relatório",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcoes,
            opcoes[0]
        );
        
        if (escolha == null) return;
        
        try {
            switch (escolha) {
                case "Comparar Instrutores de um Curso":
                    gerarRelatorioComparativoCurso();
                    break;
                case "Comparar Cursos de um Instrutor":
                    gerarRelatorioComparativoInstrutor();
                    break;
                case "Relatório Individual de Aluno":
                    gerarRelatorioAluno();
                    break;
                case "Comparar Alunos de um Curso":
                    gerarRelatorioAlunosCurso();
                    break;
                case "Relatório Detalhado de Curso":
                    gerarRelatorioCursoDetalhado();
                    break;
                case "Relatório Detalhado de Instrutor":
                    gerarRelatorioInstrutorDetalhado();
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao gerar relatório: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void gerarRelatorioComparativoCurso() {
        List<Curso> cursos = cursoService.listarCursos();
        
        if (cursos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há cursos cadastrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Curso cursoSelecionado = (Curso) JOptionPane.showInputDialog(
            this,
            "Selecione o curso para comparar seus instrutores:",
            "Comparar Instrutores de um Curso",
            JOptionPane.QUESTION_MESSAGE,
            null,
            cursos.toArray(),
            cursos.get(0)
        );
        
        if (cursoSelecionado != null) {
            try {
                relatorioService.gerarRelatorioComparativoInstrutoresPorCurso(cursoSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarRelatorios();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gerarRelatorioComparativoInstrutor() {
        List<Usuario> instrutores = new ArrayList<>(usuarioService.listarInstrutores());
        
        if (instrutores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há instrutores cadastrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Usuario instrutorSelecionado = (Usuario) JOptionPane.showInputDialog(
            this,
            "Selecione o instrutor para comparar seus cursos:",
            "Comparar Cursos de um Instrutor",
            JOptionPane.QUESTION_MESSAGE,
            null,
            instrutores.toArray(),
            instrutores.get(0)
        );
        
        if (instrutorSelecionado != null) {
            try {
                relatorioService.gerarRelatorioComparativoCursosPorInstrutor(instrutorSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarRelatorios();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gerarRelatorioAluno() {
        List<Usuario> alunos = new ArrayList<>(usuarioService.listarAlunos());
        
        if (alunos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há alunos cadastrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Usuario alunoSelecionado = (Usuario) JOptionPane.showInputDialog(
            this,
            "Selecione o aluno:",
            "Gerar Relatório do Aluno",
            JOptionPane.QUESTION_MESSAGE,
            null,
            alunos.toArray(),
            alunos.get(0)
        );
        
        if (alunoSelecionado != null) {
            try {
                relatorioService.gerarRelatorioAluno(alunoSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarRelatorios();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gerarRelatorioAlunosCurso() {
        List<Curso> cursos = cursoService.listarCursos();
        
        if (cursos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há cursos cadastrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Curso cursoSelecionado = (Curso) JOptionPane.showInputDialog(
            this,
            "Selecione o curso para comparar seus alunos:",
            "Comparar Alunos de um Curso",
            JOptionPane.QUESTION_MESSAGE,
            null,
            cursos.toArray(),
            cursos.get(0)
        );
        
        if (cursoSelecionado != null) {
            try {
                relatorioService.gerarRelatorioComparativoAlunosPorCurso(cursoSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarRelatorios();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gerarRelatorioCursoDetalhado() {
        List<Curso> cursos = cursoService.listarCursos();
        
        if (cursos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há cursos cadastrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Curso cursoSelecionado = (Curso) JOptionPane.showInputDialog(
            this,
            "Selecione o curso:",
            "Gerar Relatório Detalhado do Curso",
            JOptionPane.QUESTION_MESSAGE,
            null,
            cursos.toArray(),
            cursos.get(0)
        );
        
        if (cursoSelecionado != null) {
            try {
                relatorioService.gerarRelatorioCurso(cursoSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarRelatorios();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void gerarRelatorioInstrutorDetalhado() {
        List<Usuario> instrutores = new ArrayList<>(usuarioService.listarInstrutores());
        
        if (instrutores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há instrutores cadastrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Usuario instrutorSelecionado = (Usuario) JOptionPane.showInputDialog(
            this,
            "Selecione o instrutor:",
            "Gerar Relatório Detalhado do Instrutor",
            JOptionPane.QUESTION_MESSAGE,
            null,
            instrutores.toArray(),
            instrutores.get(0)
        );
        
        if (instrutorSelecionado != null) {
            try {
                relatorioService.gerarRelatorioDetalhadoInstrutor(instrutorSelecionado.getId());
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarRelatorios();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private String gerarNomeArquivo(Relatorio relatorio, String extensao) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String data = relatorio.getData() != null ? 
            relatorio.getData().format(formatter) : 
            LocalDate.now().format(formatter);
        
        String prefixo = "pdf".equalsIgnoreCase(extensao) ? "relatorio" : "planilha";
        
        StringBuilder nomeArquivo = new StringBuilder(prefixo).append("_");
        
        if (relatorio.getDescricao() != null && !relatorio.getDescricao().isBlank()) {
            nomeArquivo.append(relatorio.getDescricao()).append("_");
            
            if (relatorio.getNomeEntidade() != null && !relatorio.getNomeEntidade().isBlank()) {
                String nomeEntidadeLimpo = relatorio.getNomeEntidade()
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
                nomeArquivo.append(nomeEntidadeLimpo).append("_");
            }
        } 
        else {
            nomeArquivo.append("ID_").append(relatorio.getId()).append("_");
        }
        
        nomeArquivo.append(data).append(".").append(extensao);
        
        return nomeArquivo.toString();
    }
}
