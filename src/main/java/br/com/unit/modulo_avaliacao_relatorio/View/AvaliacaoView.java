package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import javax.swing.*;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Desktop;
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
    private JComboBox<Formulario> comboFormulario;
    private JButton btnSalvar;
    private JLabel lblInstrutorLogado;

    private final AvaliacaoService avaliacaoService;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final FormularioService formularioService;
    private final RelatorioService relatorioService;

    // Simulação: Instrutor “logado”
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

        // Pega o primeiro instrutor da lista para simular login
        List<Instrutor> instrutores = usuarioService.listarInstrutores();
        if (!instrutores.isEmpty()) {
            instrutorLogado = instrutores.get(0);
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
        comboFormulario = new JComboBox<>();
        btnSalvar = new JButton("Salvar");

        popularCombos();

        JLabel lblMedia = new JLabel("Média:");
        JLabel lblComentario = new JLabel("Comentário:");
        JLabel lblAluno = new JLabel("Aluno:");
        JLabel lblCurso = new JLabel("Curso:");

        lblInstrutorLogado = new JLabel("Instrutor: " + (instrutorLogado != null ? instrutorLogado.getNome() + " (ID: " + instrutorLogado.getId() + ")" : "Nenhum"));
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 0: Instrutor logado
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblInstrutorLogado, gbc);

        // Linha 1: Aluno
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblAluno, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(comboAluno, gbc);

        // Linha 2: Curso
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblCurso, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(comboCurso, gbc);

        // Linha 3: Média
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblMedia, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(campoMedia, gbc);

        // Linha 4: Comentário
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(lblComentario, gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(new JScrollPane(campoComentario), gbc);

        // Linha 5: Formulário
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Formulário:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(comboFormulario, gbc);

        // Linha 6: Botão salvar
        gbc.gridx = 1; gbc.gridy = 6;
        panel.add(btnSalvar, gbc);

        add(panel);

        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarAvaliacao();
            }
        });
    }

    private void popularCombos() {
        List<Aluno> alunos = usuarioService.listarAlunos();
        alunos.forEach(comboAluno::addItem);

        // Preenche o combo de instrutores, mas seleciona o logado
        List<Instrutor> instrutores = usuarioService.listarInstrutores();
        instrutores.forEach(comboInstrutor::addItem);
        if (instrutorLogado != null) {
            comboInstrutor.setSelectedItem(instrutorLogado);
            comboInstrutor.setEnabled(false); // bloqueia edição
        }

        List<Curso> cursos = cursoService.listarCursos();
        cursos.forEach(comboCurso::addItem);

        List<Formulario> formularios = formularioService.listarFormularios();
        formularios.forEach(comboFormulario::addItem);
    }

    private void salvarAvaliacao() {
        try {
            double media = Double.parseDouble(campoMedia.getText());
            String comentario = campoComentario.getText();
            Aluno aluno = (Aluno) comboAluno.getSelectedItem();
            Instrutor instrutor = (Instrutor) comboInstrutor.getSelectedItem();
            Curso curso = (Curso) comboCurso.getSelectedItem();
            Formulario formulario = (Formulario) comboFormulario.getSelectedItem();

            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setMedia(media);
            // Garante que Feedback seja criado antes de acessar
            Feedback feedback = new Feedback();
            feedback.setComentario(comentario);
            feedback.setAvaliacao(avaliacao);
            avaliacao.setFeedback(feedback);
            avaliacao.setAluno(aluno);
            avaliacao.setInstrutor(instrutor);
            avaliacao.setCurso(curso);
            avaliacao.setFormulario(formulario);

            Avaliacao salva = avaliacaoService.salvarAvaliacao(avaliacao);

            // Dialogo com opção de salvar PDF
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
