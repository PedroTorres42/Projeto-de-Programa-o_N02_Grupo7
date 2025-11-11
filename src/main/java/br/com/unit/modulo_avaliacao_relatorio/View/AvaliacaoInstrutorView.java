package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Lazy
@Component
public class AvaliacaoInstrutorView extends JFrame {

	private final AvaliacaoService avaliacaoService;
	private final FormularioService formularioService;
	private final RelatorioService relatorioService;

	private JLabel labelAluno;
	private Aluno alunoAtual;
	private JComboBox<Instrutor> comboInstrutor;
	private JTextArea campoFeedback;
	private final List<JComboBox<Integer>> combosNotas = new ArrayList<>();
	private Formulario formularioCarregado;

	public AvaliacaoInstrutorView(
								  AvaliacaoService avaliacaoService,
								  FormularioService formularioService,
								  RelatorioService relatorioService) {
		this.avaliacaoService = avaliacaoService;
		this.formularioService = formularioService;
		this.relatorioService = relatorioService;

		setTitle("Avaliação de Instrutor (Aluno)");
		setSize(600, 520);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
	}

	private void initComponents() {
		comboInstrutor = new JComboBox<>();
		campoFeedback = new JTextArea(4, 20);
		labelAluno = new JLabel("(não definido)");
		
		formularioCarregado = formularioService.obterOuCriarFormularioInstrutorPadrao();
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(UIConstants.BG);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int row = 0;

		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Aluno:"), gbc);
		gbc.gridx = 1; panel.add(labelAluno, gbc); row++;

		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Instrutor:"), gbc);
		gbc.gridx = 1; panel.add(comboInstrutor, gbc); row++;

		if (formularioCarregado != null && formularioCarregado.getPerguntas() != null) {
			for (Pergunta pergunta : formularioCarregado.getPerguntas()) {
				gbc.gridwidth = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				
				gbc.gridx = 0; 
				gbc.gridy = row;
				panel.add(new JLabel(pergunta.getTexto() + ":"), gbc);
				
				JComboBox<Integer> cb = new JComboBox<>();
				cb.addItem(1);
				cb.addItem(2);
				cb.addItem(3);
				cb.addItem(4);
				cb.addItem(5);
				cb.setSelectedIndex(4); 
				combosNotas.add(cb);
				
				gbc.gridx = 1;
				panel.add(cb, gbc);
				row++;
			}
		}

		gbc.gridwidth = 1;
		gbc.gridx = 0; gbc.gridy = row; 
		panel.add(new JLabel("Feedback (opcional):"), gbc);
		
		gbc.gridx = 1; 
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.3;
		panel.add(new JScrollPane(campoFeedback), gbc); 
		row++;

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		botoes.setBackground(UIConstants.BG);
		JButton btnVoltar = UIUtils.dangerButton("Voltar", this::dispose);
		btnVoltar.setToolTipText("Fechar esta janela e retornar ao menu");
		JButton btnSalvar = UIUtils.successButton("Salvar Avaliação", this::salvar);
		botoes.add(btnVoltar);
		botoes.add(btnSalvar);

		gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
		panel.add(botoes, gbc);

		add(panel);
	}

	private void salvar() {
		try {
			Aluno aluno = alunoAtual;
			Instrutor instrutor = (Instrutor) comboInstrutor.getSelectedItem();
			if (aluno == null) {
				JOptionPane.showMessageDialog(this, "Aluno não definido. Abra esta tela a partir do menu após realizar login como Aluno.");
				return;
			}
			if (instrutor == null) {
				JOptionPane.showMessageDialog(this, "Selecione o instrutor.");
				return;
			}

			Formulario formulario = formularioCarregado;

			List<Nota> notas = new ArrayList<>();
			List<Pergunta> perguntas = formulario.getPerguntas();
			for (int i = 0; i < perguntas.size() && i < combosNotas.size(); i++) {
				Pergunta p = perguntas.get(i);
				Integer valor = (Integer) combosNotas.get(i).getSelectedItem();
				Nota n = new Nota();
				n.setNota(valor);
				n.setPergunta(p);
				notas.add(n);
			}

			Avaliacao avaliacao = new Avaliacao();
			avaliacao.setAluno(aluno);
			if (aluno.getCursoAtual() != null) {
				avaliacao.setCurso(aluno.getCursoAtual());
			}
			avaliacao.setInstrutor(instrutor);
			avaliacao.setFormulario(formulario);
			avaliacao.setNotas(notas);
			notas.forEach(n -> n.setAvaliacao(avaliacao));

			String fbTxt = campoFeedback.getText();
			if (fbTxt != null && !fbTxt.isBlank()) {
				Feedback fb = new Feedback();
				fb.setComentario(fbTxt.trim());
				fb.setAvaliacao(avaliacao);
				avaliacao.setFeedback(fb);
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
                        if (ow == JOptionPane.YES_OPTION) {
                            relatorioService.exportarPdfDaAvaliacao(salva.getId(), f);
                            int open = JOptionPane.showConfirmDialog(this, "PDF salvo. Deseja abrir agora?", "Abrir PDF", JOptionPane.YES_NO_OPTION);
                            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(f);
                            }
                        }
                    } else {
						relatorioService.exportarPdfDaAvaliacao(salva.getId(), f);
						int open = JOptionPane.showConfirmDialog(this, "PDF salvo. Deseja abrir agora?", "Abrir PDF", JOptionPane.YES_NO_OPTION);
						if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
							Desktop.getDesktop().open(f);
						}
                    }
                    dispose();
                    return;
                }
			}

			dispose();

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
		}
	}

	public void setAlunoAtual(Aluno aluno) {
		this.alunoAtual = aluno;
		if (labelAluno != null) {
			labelAluno.setText(aluno != null ? aluno.getNome() : "(não definido)");
		}
		comboInstrutor.removeAllItems();
		if (aluno != null && aluno.getCursoAtual() != null && aluno.getCursoAtual().getInstrutores() != null) {
			aluno.getCursoAtual().getInstrutores().forEach(comboInstrutor::addItem);
		}
	}
}
