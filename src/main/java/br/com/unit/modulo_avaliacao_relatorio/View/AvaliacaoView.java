package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.AvaliacaoService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoView extends JFrame {

    private JComboBox<Aluno> comboAluno;
    private JComboBox<Curso> comboCurso;
    private JTextField campoNota;
    private JCheckBox checkPresenca;
    private JTextArea campoFeedback;
    private JButton botaoSalvar;

    private final AvaliacaoService avaliacaoService; // Simulação de service
    private final Instrutor instrutorLogado; // Simulação de instrutor logado

    public AvaliacaoView(AvaliacaoService service, Instrutor instrutorLogado) {
        this.avaliacaoService = service;
        this.instrutorLogado = instrutorLogado;
        configurarTela();
    }

    private void configurarTela() {
        setTitle("Registro de Notas, Presença e Feedback");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Aluno ---
        JLabel labelAluno = new JLabel("Aluno:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(labelAluno, gbc);

        comboAluno = new JComboBox<>();
        List<Aluno> alunos = criarAlunosSimulados();
        for (Aluno a : alunos) comboAluno.addItem(a);
        gbc.gridx = 1;
        gbc.gridy = 0;
        painel.add(comboAluno, gbc);

        // --- Curso ---
        JLabel labelCurso = new JLabel("Curso:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(labelCurso, gbc);

        comboCurso = new JComboBox<>();
        List<Curso> cursos = criarCursosSimulados();
        for (Curso c : cursos) comboCurso.addItem(c);
        gbc.gridx = 1;
        gbc.gridy = 1;
        painel.add(comboCurso, gbc);

        // --- Nota ---
        JLabel labelNota = new JLabel("Nota:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(labelNota, gbc);

        campoNota = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        painel.add(campoNota, gbc);

        // --- Presença ---
        JLabel labelPresenca = new JLabel("Presença:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        painel.add(labelPresenca, gbc);

        checkPresenca = new JCheckBox("Presente");
        gbc.gridx = 1;
        gbc.gridy = 3;
        painel.add(checkPresenca, gbc);

        // --- Feedback ---
        JLabel labelFeedback = new JLabel("Feedback individual:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        painel.add(labelFeedback, gbc);

        campoFeedback = new JTextArea(5,20);
        JScrollPane scroll = new JScrollPane(campoFeedback);
        gbc.gridx = 1;
        gbc.gridy = 4;
        painel.add(scroll, gbc);

        // --- Botão ---
        botaoSalvar = new JButton("Registrar Avaliação");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        painel.add(botaoSalvar, gbc);

        botaoSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarAvaliacao();
            }
        });

        add(painel);
    }

    private void registrarAvaliacao() {
        try {
            Aluno aluno = (Aluno) comboAluno.getSelectedItem();
            Curso curso = (Curso) comboCurso.getSelectedItem();
            int notaInt = Integer.parseInt(campoNota.getText());
            boolean presente = checkPresenca.isSelected();
            String feedbackTexto = campoFeedback.getText();

            // Criando Avaliacao e Feedback (simulação)
            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setAluno(aluno);
            avaliacao.setCurso(curso);
            avaliacao.setInstrutor(instrutorLogado);
            avaliacao.setData(LocalDate.now());
            avaliacao.setMedia((double) notaInt);

            Feedback feedback = new Feedback();
            feedback.setComentario(feedbackTexto);
            feedback.setAvaliacao(avaliacao);
            feedback.setUsuario(instrutorLogado);

            avaliacao.setFeedback(feedback);

            // Criando Nota
            Nota nota = new Nota();
            nota.setNota(notaInt);
            nota.setAvaliacao(avaliacao);
            List<Nota> notas = new ArrayList<>();
            notas.add(nota);
            avaliacao.setNotas(notas);

            // Simulação de persistência (chamaria AvaliacaoService real se disponível)
            if (avaliacaoService != null) {
                avaliacaoService.salvarAvaliacao(avaliacao);
            }

            JOptionPane.showMessageDialog(this,
                    "✅ Avaliação registrada com sucesso!\nAluno: "+aluno+"\nCurso: "+curso+"\nNota: "+notaInt+"\nPresença: "+(presente?"Presente":"Ausente")+"\nFeedback: "+feedbackTexto,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            campoNota.setText("");
            campoFeedback.setText("");
            checkPresenca.setSelected(false);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,"Insira uma nota válida (ex: 8)","Erro",JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Erro",JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Dados simulados ---
    private List<Aluno> criarAlunosSimulados() {
        List<Aluno> lista = new ArrayList<>();
        lista.add(new Aluno("1","João Silva", null, null, null));
        lista.add(new Aluno("2","Maria Souza", null, null, null));
        lista.add(new Aluno("3","Lucas Andrade", null, null, null));
        return lista;
    }

    private List<Curso> criarCursosSimulados() {
        List<Curso> lista = new ArrayList<>();
        lista.add(new Curso(1L,"POO","Programação Orientada a Objetos",60,null));
        lista.add(new Curso(2L,"BD","Banco de Dados",80,null));
        lista.add(new Curso(3L,"Engenharia","Engenharia de Software",100,null));
        return lista;
    }

    public static void main(String[] args) {
        Instrutor instrutor = new Instrutor();
        instrutor.setNome("Prof. Pedro");

        SwingUtilities.invokeLater(() -> new AvaliacaoView(null,instrutor).setVisible(true));
    }
}
