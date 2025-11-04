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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    // Painel de detalhes (expansível)
    private JPanel detalhesPanel;
    private JTable detalhesTabela;
    private DefaultTableModel detalhesModelo;
    private JLabel lblMedia;
    private JLabel lblFeedback;

    // Cache de avaliações carregadas (mesma ordem das linhas da tabela)
    private List<Avaliacao> cacheAvaliacoes = Collections.emptyList();
    private int linhaExpandida = -1;

    public RelatoriosInstrutorView(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
        setTitle("Relatórios do Instrutor");
        setSize(800, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        lblInstrutor = new JLabel("Instrutor: (não definido)");
        lblInstrutor.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(lblInstrutor, BorderLayout.NORTH);

        // Tabela principal
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

        // Painel de detalhes (inicialmente oculto)
        detalhesPanel = new JPanel(new BorderLayout());
        detalhesPanel.setBorder(BorderFactory.createTitledBorder("Detalhes da Avaliação"));

        // Topo do detalhe com média e feedback (se houver)
        JPanel topoDetalhe = new JPanel(new GridLayout(0,1));
        lblMedia = new JLabel("Média: —");
        lblFeedback = new JLabel("Feedback: —");
        topoDetalhe.add(lblMedia);
        topoDetalhe.add(lblFeedback);
        detalhesPanel.add(topoDetalhe, BorderLayout.NORTH);

        // Tabela de perguntas x notas
        detalhesModelo = new DefaultTableModel(new String[]{"Pergunta", "Nota"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        detalhesTabela = new JTable(detalhesModelo);
        detalhesPanel.add(new JScrollPane(detalhesTabela), BorderLayout.CENTER);
        detalhesPanel.setVisible(false);

        // Centro com tabela principal e detalhes abaixo
        JPanel center = new JPanel(new BorderLayout());
        center.add(scroll, BorderLayout.CENTER);
        center.add(detalhesPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Rodapé
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        south.add(btnFechar);
        add(south, BorderLayout.SOUTH);

        // Listener de seleção para "expandir" detalhes
        tabela.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int row = tabela.getSelectedRow();
                if (row < 0 || row >= cacheAvaliacoes.size()) {
                    esconderDetalhes();
                    return;
                }
                if (linhaExpandida == row && detalhesPanel.isVisible()) {
                    // Se clicar novamente na mesma linha, recolhe
                    esconderDetalhes();
                } else {
                    mostrarDetalhes(row);
                }
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
            String alunoAnonimo = "Anônimo"; // Nunca exibir nome do aluno
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

        // Média e feedback
        lblMedia.setText("Média: " + (a.getMedia() != null ? String.format(java.util.Locale.US, "%.2f", a.getMedia()) : "—"));
        String fb = a.getFeedback() != null && a.getFeedback().getComentario() != null && !a.getFeedback().getComentario().isBlank()
                ? a.getFeedback().getComentario() : "—";
        lblFeedback.setText("Feedback: " + fb);

        // Perguntas e notas
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
