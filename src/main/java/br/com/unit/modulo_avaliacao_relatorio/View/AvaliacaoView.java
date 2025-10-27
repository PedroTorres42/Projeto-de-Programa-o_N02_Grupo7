package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AvaliacaoView extends JFrame {

    private JTextField campoMedia;
    private JTextArea campoComentario;
    private JComboBox<Aluno> comboAluno;
    private JComboBox<Instrutor> comboInstrutor;
    private JComboBox<Curso> comboCurso;
    private JComboBox<Formulario> comboFormulario;
    private JButton btnSalvar;

    private final AvaliacaoService avaliacaoService;
    private final AlunoService alunoService;
    private final InstrutorService instrutorService;
    private final CursoService cursoService;
    private final FormularioService formularioService;

    public AvaliacaoView(AvaliacaoService avaliacaoService,
                         AlunoService alunoService,
                         InstrutorService instrutorService,
                         CursoService cursoService,
                         FormularioService formularioService) {

        this.avaliacaoService = avaliacaoService;
        this.alunoService = alunoService;
        this.instrutorService = instrutorService;
        this.cursoService = cursoService;
        this.formularioService = formularioService;

        setTitle("Cadastro de Avaliação");
        setSize(500, 400);
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

        // Popular combos
        popularCombos();

        JLabel lblMedia = new JLabel("Média:");
        JLabel lblComentario = new JLabel("Comentário:");
        JLabel lblAluno = new JLabel("Aluno:");
        JLabel lblInstrutor = new JLabel("Instrutor:");
        JLabel lblCurso = new JLabel("Curso:");
        JLabel lblFormulario = new JLabel("Formulário:");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblMedia, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(campoMedia, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblComentario, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(new JScrollPane(campoComentario), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblAluno, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(comboAluno, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblInstrutor, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(comboInstrutor, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(lblCurso, gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(comboCurso, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(lblFormulario, gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(comboFormulario, gbc);

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
        List<Aluno> alunos = alunoService.listarAlunos();
        alunos.forEach(comboAluno::addItem);

        List<Instrutor> instrutores = instrutorService.listarInstrutores();
        instrutores.forEach(comboInstrutor::addItem);

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
            avaliacao.setComentario(comentario);
            avaliacao.setAluno(aluno);
            avaliacao.setInstrutor(instrutor);
            avaliacao.setCurso(curso);
            avaliacao.setFormulario(formulario);

            Avaliacao salva = avaliacaoService.salvarAvaliacao(avaliacao);
            JOptionPane.showMessageDialog(this, "Avaliação salva com ID: " + salva.getId());

            // Resetar campos
            campoMedia.setText("");
            campoComentario.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Informe um número válido para média.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
        }
    }
}
