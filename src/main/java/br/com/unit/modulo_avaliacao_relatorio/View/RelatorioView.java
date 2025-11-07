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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Lazy
@Component
public class RelatorioView extends JFrame {
    private final RelatorioService relatorioService;
    private final CursoService cursoService;
    private final UsuarioService usuarioService;
    
    private JTable tabelaRelatorios;
    private DefaultTableModel modeloTabela;
    private JComboBox<FiltroTipo> comboFiltro;
    private JButton btnVisualizar;
    private JButton btnExportarPDF;
    private JButton btnExcluir;
    private JButton btnAtualizar;
    private JButton btnGerarNovo;
    private boolean loading = false;

    private final List<Relatorio> relatoriosCarregados = new ArrayList<>();
    
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
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { carregarRelatorios(); }
            @Override public void windowActivated(WindowEvent e) { carregarRelatorios(); }
        });
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        painelSuperior.add(new JLabel("Filtrar por tipo:"));
        comboFiltro = new JComboBox<>(FiltroTipo.values());
        comboFiltro.addActionListener(e -> filtrarRelatorios());
        painelSuperior.add(comboFiltro);

        btnAtualizar = new JButton("Recarregar (F5)");
        btnAtualizar.setToolTipText("Recarregar lista de relatórios do banco (F5)");
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
        
        btnVisualizar = new JButton("Visualizar");
        btnVisualizar.addActionListener(e -> visualizarRelatorio());
        painelInferior.add(btnVisualizar);
        
        btnExportarPDF = new JButton("Exportar PDF Selecionado");
        btnExportarPDF.addActionListener(e -> exportarPDF());
        painelInferior.add(btnExportarPDF);

        JButton btnExportarCSV = new JButton("Exportar CSV (Avaliações)");
        btnExportarCSV.addActionListener(e -> exportarCSV());
        painelInferior.add(btnExportarCSV);

        JButton btnExportarPDFGraficos = new JButton("Exportar PDF com Gráficos");
        btnExportarPDFGraficos.addActionListener(e -> exportarPDFComGraficos());
        painelInferior.add(btnExportarPDFGraficos);

        btnExcluir = new JButton("Excluir Selecionado");
        btnExcluir.addActionListener(e -> excluirRelatorio());
        btnExcluir.setForeground(Color.RED);
        painelInferior.add(btnExcluir);
        
        add(painelInferior, BorderLayout.SOUTH);
        
        tabelaRelatorios.getSelectionModel().addListSelectionListener(e -> {
            boolean temSelecao = tabelaRelatorios.getSelectedRow() != -1;
            boolean enable = temSelecao && !loading;
            btnVisualizar.setEnabled(enable);
            btnExportarPDF.setEnabled(enable);
            btnExcluir.setEnabled(enable);
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "reload");
        getRootPane().getActionMap().put("reload", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { carregarRelatorios(); }
        });
        
        btnVisualizar.setEnabled(false);
        btnExportarPDF.setEnabled(false);
        btnExcluir.setEnabled(false);
    }

    public void exibir() {
        carregarRelatorios();
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    private void setLoading(boolean value) {
        loading = value;
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        comboFiltro.setEnabled(!value);
        btnAtualizar.setEnabled(!value);
        btnGerarNovo.setEnabled(!value);
        tabelaRelatorios.setEnabled(!value);
    }
    
    private void carregarRelatorios() {
        try {
            setLoading(true);
            relatoriosCarregados.clear();
            modeloTabela.setRowCount(0);
            
            ActionListener[] listeners = comboFiltro.getActionListeners();
            for (ActionListener listener : listeners) {
                comboFiltro.removeActionListener(listener);
            }
            
            comboFiltro.setSelectedItem(FiltroTipo.TODOS);
            
            for (ActionListener listener : listeners) {
                comboFiltro.addActionListener(listener);
            }
            
            List<Relatorio> relatorios = relatorioService.pegarTodosRelatorios();
            
            for (Relatorio rel : relatorios) {
                relatoriosCarregados.add(rel);
                adicionarRelatorioNaTabela(rel);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar relatórios: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setLoading(false);
        }
    }
    
    private void filtrarRelatorios() {
        try {
            setLoading(true);
            relatoriosCarregados.clear();
            modeloTabela.setRowCount(0);
            
            FiltroTipo filtro = (FiltroTipo) comboFiltro.getSelectedItem();
            List<Relatorio> relatorios;
            
            if (filtro == null || filtro == FiltroTipo.TODOS) {
                relatorios = relatorioService.pegarTodosRelatorios();
            } else {
                relatorios = switch (filtro) {
                    case CURSO -> relatorioService.filtrarRelatoriosPorCurso();
                    case INSTRUTOR -> relatorioService.filtrarRelatoriosPorInstrutor();
                    case ALUNO -> relatorioService.filtrarRelatoriosPorAluno();
                    default -> relatorioService.pegarTodosRelatorios();
                };
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
        } finally {
            setLoading(false);
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
    
    private void visualizarRelatorio() {
        int linhaSelecionada = tabelaRelatorios.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um relatório para visualizar.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Relatorio relatorio = relatoriosCarregados.get(linhaSelecionada);
            byte[] pdfBytes = java.util.Base64.getDecoder().decode(relatorio.getDocumento());
            
            File tempFile = File.createTempFile("relatorio_", ".pdf");
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), pdfBytes);
            
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(tempFile);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Não foi possível abrir o visualizador de PDF.\nUse 'Exportar PDF' para salvar o arquivo.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Visualização não suportada neste sistema.\nUse 'Exportar PDF' para salvar o arquivo.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao visualizar relatório: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
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

        private void exportarPDFComGraficos() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String dataAtual = LocalDate.now().format(formatter);
            String nomeArquivo = "relatorio_geral_graficos_" + dataAtual + ".pdf";

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar PDF com Gráficos");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
            fileChooser.setSelectedFile(new File(nomeArquivo));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();

                if (!arquivo.getName().toLowerCase().endsWith(".pdf")) {
                    arquivo = new File(arquivo.getAbsolutePath() + ".pdf");
                }
                
                int incluirSatisfacao = JOptionPane.showConfirmDialog(this,
                    "Deseja incluir o gráfico de satisfação (sentimento) no PDF?",
                    "Opções de Exportação",
                    JOptionPane.YES_NO_OPTION);
                
                boolean incluir = incluirSatisfacao == JOptionPane.YES_OPTION;

                relatorioService.exportarPdfComGraficos(arquivo, incluir);

                JOptionPane.showMessageDialog(this,
                    "PDF com gráficos exportado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao exportar PDF com gráficos: " + ex.getMessage(),
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
            cursos.getFirst()
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
            instrutores.getFirst()
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
            alunos.getFirst()
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
            cursos.getFirst()
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
            cursos.getFirst()
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
            instrutores.getFirst()
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
