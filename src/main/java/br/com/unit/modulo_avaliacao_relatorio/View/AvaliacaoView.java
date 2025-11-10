package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import javax.swing.*;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.util.List;

@Lazy
@Component
public class AvaliacaoView extends JFrame {

    private JTextField campoMedia;
    private JTextArea campoComentario;
    private JComboBox<Aluno> comboAluno;
    private JComboBox<Instrutor> comboInstrutor;
    private JComboBox<Curso> comboCurso;
    private JSpinner spinnerFrequencia;

    private final AvaliacaoService avaliacaoService;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final FormularioService formularioService;
    private final RelatorioService relatorioService;

    private Instrutor instrutorLogado;

    public AvaliacaoView(
            AvaliacaoService avaliacaoService,
            UsuarioService usuarioService,
            CursoService cursoService,
            FormularioService formularioService,
            RelatorioService relatorioService) {

        this.avaliacaoService = avaliacaoService;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.formularioService = formularioService;
        this.relatorioService = relatorioService;

        List<Instrutor> instrutores = usuarioService.listarInstrutores();
        if (!instrutores.isEmpty()) {
            instrutorLogado = instrutores.getFirst();
        }

        setTitle("Cadastro de Avaliação");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        campoMedia = new JTextField();
        campoComentario = new JTextArea(5, 20);

        comboAluno = new JComboBox<>();
        comboInstrutor = new JComboBox<>();
        comboCurso = new JComboBox<>();
    JButton btnSalvar = new JButton("Salvar");
    JButton btnVoltar = new JButton("Voltar");
    btnVoltar.setToolTipText("Fechar esta janela e retornar ao menu");
        spinnerFrequencia = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));

        // Renderer para exibir somente o nome do curso (evitar toString padrão)
        comboCurso.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                java.awt.Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String texto;
                if (value instanceof Curso c) {
                    texto = (c.getNome() != null && !c.getNome().isBlank()) ? c.getNome() : ("Curso#" + c.getId());
                } else {
                    texto = value != null ? value.toString() : "";
                }
                setText(texto);
                return comp;
            }
        });

        popularCombos();

        JLabel lblMedia = new JLabel("Média:");
        JLabel lblComentario = new JLabel("Comentário:");
        JLabel lblAluno = new JLabel("Aluno:");
        JLabel lblCurso = new JLabel("Curso:");
        JLabel lblFreq = new JLabel("Frequência (%):");

        JLabel lblInstrutorLogado = new JLabel("Instrutor: " + (instrutorLogado != null ? instrutorLogado.getNome() + " (ID: " + instrutorLogado.getId() + ")" : "Nenhum"));
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblInstrutorLogado, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblAluno, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(comboAluno, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblCurso, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(comboCurso, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblMedia, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(campoMedia, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(lblComentario, gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(new JScrollPane(campoComentario), gbc);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(btnVoltar);
        botoes.add(btnSalvar);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        panel.add(lblFreq, gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(spinnerFrequencia, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        panel.add(botoes, gbc);

        add(panel);

    btnVoltar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarAvaliacao());
    }

    private void popularCombos() {
        comboAluno.removeAllItems();
        comboCurso.removeAllItems();
        comboInstrutor.removeAllItems();

        if (instrutorLogado != null) {
            comboInstrutor.addItem(instrutorLogado);
            comboInstrutor.setSelectedItem(instrutorLogado);
            comboInstrutor.setEnabled(false);

        String instrutorId = instrutorLogado.getId();
        // Deduplicar cursos por ID e ordenar alfabeticamente
        java.util.Map<Long, Curso> cursosMap = new java.util.LinkedHashMap<>();
        for (Curso c : cursoService.listarCursos()) {
            if (c != null && c.getId() != null && c.getInstrutores() != null &&
                    c.getInstrutores().stream().anyMatch(i -> i != null && instrutorId.equals(i.getId()))) {
                cursosMap.putIfAbsent(c.getId(), c);
            }
        }
        java.util.List<Curso> cursosOrdenados = cursosMap.values().stream()
                .sorted(java.util.Comparator.comparing(cc -> cc.getNome() != null ? cc.getNome() : ""))
                .toList();
        cursosOrdenados.forEach(comboCurso::addItem);

        // Deduplicar alunos por ID, apenas dos cursos do instrutor
        java.util.Map<String, Aluno> alunosMap = new java.util.LinkedHashMap<>();
        for (Aluno a : usuarioService.listarAlunos()) {
            if (a != null && a.getId() != null && a.getCursoAtual() != null && a.getCursoAtual().getId() != null &&
                    cursosMap.containsKey(a.getCursoAtual().getId())) {
                alunosMap.putIfAbsent(a.getId(), a);
            }
        }
        alunosMap.values().forEach(comboAluno::addItem);
        } else {
            usuarioService.listarAlunos().forEach(comboAluno::addItem);
            usuarioService.listarInstrutores().forEach(comboInstrutor::addItem);
            cursoService.listarCursos().stream()
                    .sorted(java.util.Comparator.comparing(c -> c.getNome() != null ? c.getNome() : ""))
                    .forEach(comboCurso::addItem);
        }
    }

    private void salvarAvaliacao() {
        try {
            double media = Double.parseDouble(campoMedia.getText());
            if (media < 0.0 || media > 10.0) {
                JOptionPane.showMessageDialog(this, "A média deve estar entre 0 e 10.");
                return;
            }
            String comentario = campoComentario.getText();
            Aluno aluno = (Aluno) comboAluno.getSelectedItem();
            Instrutor instrutor = (Instrutor) comboInstrutor.getSelectedItem();
            Curso curso = (Curso) comboCurso.getSelectedItem();
            // Define automaticamente um formulário padrão (sem escolha pelo usuário)
            Formulario formulario = formularioService.obterOuCriarFormularioAlunoPadrao();

            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setMedia(media);
            Feedback feedback = new Feedback();
            feedback.setComentario(comentario);
            feedback.setAvaliacao(avaliacao);
            avaliacao.setFeedback(feedback);
            avaliacao.setAluno(aluno);
            avaliacao.setInstrutor(instrutor);
            avaliacao.setCurso(curso);
            avaliacao.setFormulario(formulario);

            // Registrar frequência (%) como Nota vinculada à Pergunta do tipo FREQUENCIA
            List<Nota> notas = new java.util.ArrayList<>();
            try {
                Pergunta perguntaFreq = formularioService.obterOuCriarPerguntaFrequencia();
                Nota nFreq = new Nota();
                nFreq.setPergunta(perguntaFreq);
                nFreq.setNota((Integer) spinnerFrequencia.getValue());
                nFreq.setAvaliacao(avaliacao);
                notas.add(nFreq);
            } catch (Exception ignore) { }
            if (!notas.isEmpty()) {
                avaliacao.setNotas(notas);
            }

            Avaliacao salva = avaliacaoService.salvarAvaliacao(avaliacao);

            Object[] options = {"OK", "Salvar PDF..."};
            int opt = JOptionPane.showOptionDialog(this,
                    "Avaliação salva com ID: " + (salva != null ? salva.getId() : "—"),
                    "Sucesso",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (opt == 1 && salva != null && salva.getId() != null) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Salvar PDF da avaliação");
                fc.setSelectedFile(new File("avaliacao-" + salva.getId() + ".pdf"));
                int r = fc.showSaveDialog(this);
                if (r == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    if (!f.getName().toLowerCase().endsWith(".pdf")) {
                        f = new File(f.getParentFile(), f.getName() + ".pdf");
                    }
                    if (f.exists()) {
                        int ow = JOptionPane.showConfirmDialog(this,
                                "O arquivo já existe. Deseja sobrescrever?",
                                "Confirmar",
                                JOptionPane.YES_NO_OPTION);
                        if (ow != JOptionPane.YES_OPTION) return;
                    }
                    relatorioService.exportarPdfDaAvaliacao(salva.getId(), f);
                    int open = JOptionPane.showConfirmDialog(this, "PDF salvo. Deseja abrir agora?", "Abrir PDF", JOptionPane.YES_NO_OPTION);
                    if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(f);
                    }
                }
            }

            campoMedia.setText("");
            campoComentario.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Informe um número válido para média.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
        }
    }
}
