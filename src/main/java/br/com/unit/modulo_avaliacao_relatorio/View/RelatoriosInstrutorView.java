package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Instrutor;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Nota;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import br.com.unit.modulo_avaliacao_relatorio.Service.RelatorioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Lazy
@Component
public class RelatoriosInstrutorView extends JFrame {

    private final RelatorioService relatorioService;

    private Instrutor instrutorAtual;
    private JLabel lblInstrutor;
    private JTable tabela;
    private DefaultTableModel modelo;

    private JPanel detalhesPanel;
    private DefaultTableModel detalhesModelo;
    private JLabel lblMedia;
    private JLabel lblFeedback;

    private List<Avaliacao> cacheAvaliacoes = Collections.emptyList();
    private int linhaExpandida = -1;

    public RelatoriosInstrutorView(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    setTitle("Relatórios do Instrutor");
    // Janela maior por padrão para melhor visualização
    setSize(1100, 700);
    setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        lblInstrutor = UIUtils.subtitleLabel("Instrutor: (não definido)");
        lblInstrutor.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(lblInstrutor, BorderLayout.NORTH);

        String[] colunas = {"Data", "Curso", "Aluno (anônimo)", "Média"};
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabela);

        detalhesPanel = new JPanel(new BorderLayout());
        detalhesPanel.setBorder(BorderFactory.createTitledBorder("Detalhes da Avaliação"));

        JPanel topoDetalhe = new JPanel(new GridLayout(0,1));
        lblMedia = new JLabel("Média: —");
        lblFeedback = new JLabel("Feedback: —");
        topoDetalhe.add(lblMedia);
        topoDetalhe.add(lblFeedback);
        detalhesPanel.add(topoDetalhe, BorderLayout.NORTH);

        detalhesModelo = new DefaultTableModel(new String[]{"Pergunta", "Nota"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable detalhesTabela = new JTable(detalhesModelo);
        detalhesPanel.add(new JScrollPane(detalhesTabela), BorderLayout.CENTER);
        detalhesPanel.setVisible(false);

        JPanel center = new JPanel(new BorderLayout());
        center.add(scroll, BorderLayout.CENTER);
        center.add(detalhesPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(UIConstants.BG);
        JButton btnFechar = UIUtils.dangerButton("Fechar", this::dispose);
        south.add(btnFechar);
        add(south, BorderLayout.SOUTH);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tabela.getSelectedRow();
            if (row < 0 || row >= cacheAvaliacoes.size()) {
                esconderDetalhes();
                return;
            }
            if (linhaExpandida == row && detalhesPanel.isVisible()) {
                esconderDetalhes();
            } else {
                mostrarDetalhes(row);
            }
        });
    }

    public void setInstrutorAtual(Instrutor instrutor) {
        this.instrutorAtual = instrutor;
        lblInstrutor.setText("Instrutor: " + (instrutor != null && instrutor.getNome() != null && !instrutor.getNome().isBlank()
                ? instrutor.getNome() : "(sem nome)"));
        carregarDados();
    }

    private void carregarDados() {
        modelo.setRowCount(0);
        esconderDetalhes();
        if (instrutorAtual == null || instrutorAtual.getId() == null) return;
        cacheAvaliacoes = relatorioService.listarAvaliacoesDoInstrutor(instrutorAtual.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Avaliacao a : cacheAvaliacoes) {
            String data = a.getData() != null ? a.getData().format(fmt) : "—";
            Curso c = a.getCurso();
            String curso = c != null && c.getNome() != null && !c.getNome().isBlank() ? c.getNome() : (c != null && c.getId() != null ? "Curso " + c.getId() : "—");
            String alunoAnonimo = "Anônimo";
            String media = a.getMedia() != null ? String.format(java.util.Locale.US, "%.2f", a.getMedia()) : "—";
            modelo.addRow(new Object[]{data, curso, alunoAnonimo, media});
        }
        if (modelo.getRowCount() == 0) {
            modelo.addRow(new Object[]{"Sem registros", "—", "—", "—"});
        }
    }

    private void mostrarDetalhes(int row) {
        linhaExpandida = row;
        Avaliacao a = cacheAvaliacoes.get(row);

        lblMedia.setText("Média: " + (a.getMedia() != null ? String.format(java.util.Locale.US, "%.2f", a.getMedia()) : "—"));
        String fb = a.getFeedback() != null && a.getFeedback().getComentario() != null && !a.getFeedback().getComentario().isBlank()
                ? a.getFeedback().getComentario() : "—";
        lblFeedback.setText("Feedback: " + fb);

        detalhesModelo.setRowCount(0);
        List<Nota> notas = a.getNotas();
        if (notas != null && !notas.isEmpty()) {
            for (Nota n : notas) {
                Pergunta p = n.getPergunta();
                String texto = p != null && p.getTexto() != null && !p.getTexto().isBlank() ? p.getTexto() : "Pergunta";
                String valor = n.getNota() != null ? String.valueOf(n.getNota()) : "—";
                detalhesModelo.addRow(new Object[]{texto, valor});
            }
        } else {
            detalhesModelo.addRow(new Object[]{"Sem notas", "—"});
        }

        detalhesPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void esconderDetalhes() {
        linhaExpandida = -1;
        detalhesPanel.setVisible(false);
        detalhesModelo.setRowCount(0);
        lblMedia.setText("Média: —");
        lblFeedback.setText("Feedback: —");
        revalidate();
        repaint();
    }
}
