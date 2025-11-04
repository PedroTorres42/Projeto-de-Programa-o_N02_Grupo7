package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Administrador;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Aluno;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Instrutor;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Service.UsuarioService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Lazy
@Component
public class MenuView extends JFrame {

	private final ApplicationContext ctx;

	private Usuario usuarioAtual;
	private JLabel lblUsuario;
	private JPanel panelBotoes;

	public MenuView(UsuarioService usuarioService, ApplicationContext ctx) {
		this.ctx = ctx;

		setTitle("Menu Principal");
		setSize(420, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		lblUsuario = new JLabel("Usuário: (não definido)");
		lblUsuario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(lblUsuario, BorderLayout.NORTH);

		panelBotoes = new JPanel();
		panelBotoes.setLayout(new GridLayout(0, 1, 10, 10));
		panelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(panelBotoes, BorderLayout.CENTER);

		JButton btnSair = new JButton("Sair");
		btnSair.addActionListener(e -> dispose());
		JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		south.add(btnSair);
		add(south, BorderLayout.SOUTH);

		renderBotoes();
	}

	private void renderBotoes() {
		panelBotoes.removeAll();

		if (usuarioAtual == null) {
			lblUsuario.setText("Usuário: (não definido)");
			JLabel dica = new JLabel("Faça login/cadastro para ver as opções do seu perfil.");
			panelBotoes.add(dica);
		} else if (usuarioAtual instanceof Aluno) {
			lblUsuario.setText("Aluno: " + safe(usuarioAtual.getNome()));

			JButton btnAvaliarInstrutor = new JButton("Avaliar Instrutor");
			btnAvaliarInstrutor.addActionListener(e -> abrirAvaliacaoAluno());
			panelBotoes.add(btnAvaliarInstrutor);

			JButton btnAvaliarCurso = new JButton("Avaliar Curso");
			btnAvaliarCurso.addActionListener(e -> abrirAvaliacaoCurso());
			panelBotoes.add(btnAvaliarCurso);

		} else if (usuarioAtual instanceof Instrutor) {
			lblUsuario.setText("Instrutor: " + safe(usuarioAtual.getNome()));

			JButton btnAvaliarAluno = new JButton("Avaliar Aluno");
			btnAvaliarAluno.addActionListener(e -> abrirAvaliacaoView());
			panelBotoes.add(btnAvaliarAluno);

		} else if (usuarioAtual instanceof Administrador) {
			lblUsuario.setText("Administrador: " + safe(usuarioAtual.getNome()));

			JButton btnRelatorios = new JButton("Ver Relatórios");
			btnRelatorios.addActionListener(e -> verRelatorios());
			panelBotoes.add(btnRelatorios);

		} else {
			lblUsuario.setText("Usuário: " + safe(usuarioAtual.getNome()));
			panelBotoes.add(new JLabel("Perfil não reconhecido."));
		}

		panelBotoes.revalidate();
		panelBotoes.repaint();
	}

	private void abrirAvaliacaoAluno() {
		try {
			AvaliacaoAlunoView tela = ctx.getBean("avaliacaoAlunoView", AvaliacaoAlunoView.class);
			if (usuarioAtual instanceof Aluno a) {
				tela.setAlunoAtual(a);
			}
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir AvaliacaoAlunoView: " + ex.getMessage());
		}
	}

	private void abrirAvaliacaoView() {
		try {
			AvaliacaoView tela = ctx.getBean(AvaliacaoView.class);
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir AvaliacaoView: " + ex.getMessage());
		}
	}

	private void abrirAvaliacaoCurso() {
		JOptionPane.showMessageDialog(this, "Tela de Avaliação de Curso (em breve).");
	}

	private void verRelatorios() {
		JOptionPane.showMessageDialog(this, "Módulo de Relatórios será implementado em breve.");
	}

	private String safe(String s) {
		return (s != null && !s.isBlank()) ? s : "(sem nome)";
	}

	public void setUsuarioAtual(Usuario usuario) {
		this.usuarioAtual = usuario;
		renderBotoes();
	}
}
