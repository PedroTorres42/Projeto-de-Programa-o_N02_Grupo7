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
		// Mantemos o tamanho compacto do menu inicial (sem padronização global de 900x600)
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		lblUsuario = new JLabel("Usuário: (não definido)");
		lblUsuario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(lblUsuario, BorderLayout.NORTH);

		panelBotoes = new JPanel();
		panelBotoes.setLayout(new GridLayout(0, 1, 10, 10));
		panelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panelBotoes.setBackground(UIConstants.BG);
		add(panelBotoes, BorderLayout.CENTER);

	JButton btnSair = UIUtils.dangerButton("Sair", this::sairParaLogin);
		JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		south.setBackground(UIConstants.BG);
		south.add(btnSair);
		add(south, BorderLayout.SOUTH);

		renderBotoes();
	}

	private void renderBotoes() {
		panelBotoes.removeAll();

        switch (usuarioAtual) {
            case null -> {
                lblUsuario.setText("Usuário: (não definido)");
                JLabel dica = new JLabel("Faça login/cadastro para ver as opções do seu perfil.");
                panelBotoes.add(dica);
            }
            case Aluno aluno -> {
                lblUsuario.setText("Aluno: " + safe(usuarioAtual.getNome()));
				JButton btnAvaliarInstrutor = UIUtils.primaryButton("Avaliar Instrutor", this::abrirAvaliacaoInstrutor);
                panelBotoes.add(btnAvaliarInstrutor);
				JButton btnAvaliarCurso = UIUtils.primaryButton("Avaliar Curso", this::abrirAvaliacaoCurso);
                panelBotoes.add(btnAvaliarCurso);
            }
            case Instrutor instrutor -> {
                lblUsuario.setText("Instrutor: " + safe(usuarioAtual.getNome()));

				JButton btnAvaliarAluno = UIUtils.primaryButton("Avaliar Aluno", this::abrirAvaliacaoView);
                panelBotoes.add(btnAvaliarAluno);

				JButton btnVisualizarRelatorio = UIUtils.primaryButton("Visualizar Relatório", this::abrirRelatoriosInstrutor);
                panelBotoes.add(btnVisualizarRelatorio);
            }
            case Administrador administrador -> {
                lblUsuario.setText("Administrador: " + safe(usuarioAtual.getNome()));

				JButton btnRelatorios = UIUtils.primaryButton("Ver Relatórios", this::verRelatorios);
                panelBotoes.add(btnRelatorios);

				JButton btnGerenciarFormularios = UIUtils.primaryButton("Gerenciar Formulários", this::abrirGerenciarFormularios);
                panelBotoes.add(btnGerenciarFormularios);

				JButton btnCadastrarUsuario = UIUtils.primaryButton("Cadastrar Usuário", this::abrirCadastroUsuario);
				panelBotoes.add(btnCadastrarUsuario);

				JButton btnCadastrarCurso = UIUtils.primaryButton("Cadastrar Curso", this::abrirCadastroCurso);
				panelBotoes.add(btnCadastrarCurso);

            }
            default -> {
                lblUsuario.setText("Usuário: " + safe(usuarioAtual.getNome()));
                panelBotoes.add(new JLabel("Perfil não reconhecido."));
            }
        }

		panelBotoes.revalidate();
		panelBotoes.repaint();
	}

	private void abrirAvaliacaoCurso() {
		try {
			AvaliacaoCursoView tela = ctx.getBean(AvaliacaoCursoView.class);
			if (usuarioAtual instanceof Aluno a) {
				tela.setAlunoAtual(a);
			}
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir AvaliacaoCursoView: " + ex.getMessage());
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

	private void abrirAvaliacaoInstrutor() {
		try {
			AvaliacaoInstrutorView tela = ctx.getBean(AvaliacaoInstrutorView.class);
			if (usuarioAtual instanceof Aluno a) {
				tela.setAlunoAtual(a);
			}
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir AvaliacaoInstrutorView: " + ex.getMessage());
		}
	}

	private void abrirRelatoriosInstrutor() {
		try {
			RelatoriosInstrutorView tela = ctx.getBean(RelatoriosInstrutorView.class);
			if (usuarioAtual instanceof Instrutor i) {
				tela.setInstrutorAtual(i);
			}
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir Relatórios do Instrutor: " + ex.getMessage());
		}
	}

	private void abrirGerenciarFormularios() {
		try {
			FormulariosView tela = ctx.getBean(FormulariosView.class);
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir Gerenciar Formulários: " + ex.getMessage());
		}
	}

	private void abrirCadastroUsuario() {
		try {
			CadastroView tela = ctx.getBean(CadastroView.class);
			tela.setMenuView(this);
			tela.exibir();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir Cadastro de Usuário: " + ex.getMessage());
		}
	}

	private void abrirCadastroCurso() {
		try {
			CadastroCursoView tela = ctx.getBean(CadastroCursoView.class);
			tela.setVisible(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir Cadastro de Curso: " + ex.getMessage());
		}
	}

	private void verRelatorios() {
		try {
			RelatorioView tela = ctx.getBean(RelatorioView.class);
            try {
                java.lang.reflect.Method m = tela.getClass().getMethod("exibir");
                m.invoke(tela);
            } catch (NoSuchMethodException | IllegalAccessException |
                     java.lang.reflect.InvocationTargetException ignored) {
                TelaRefreshFallback.forcar(tela);
            }
        } catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir Relatórios: " + ex.getMessage());
		}
	}

	private static class TelaRefreshFallback {
		static void forcar(RelatorioView v) {
			if (v == null) return;
			v.setVisible(true);
		}
	}

	private String safe(String s) {
		return (s != null && !s.isBlank()) ? s : "(sem nome)";
	}

	public void setUsuarioAtual(Usuario usuario) {
		this.usuarioAtual = usuario;
		renderBotoes();
	}

	private void sairParaLogin() {
		try {
			// limpa estado do usuário atual
			this.usuarioAtual = null;
			// abre tela de login via contexto do Spring
			LoginView login = ctx.getBean(LoginView.class);
			if (login != null) {
				login.exibir();
			}
			// esconde o menu atual
			this.setVisible(false);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Falha ao abrir tela de login: " + ex.getMessage());
		}
	}
}
