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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	private static final List<String> QUESTOES = Arrays.asList(
			"Didática",
			"Domínio do conteúdo",
			"Clareza",
			"Assiduidade",
			"Interação com a turma",
			"Capacidade de resolver dúvidas"
	);

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
        UIUtils.padronizarJanela(this);
	}

	private void initComponents() {
		comboInstrutor = new JComboBox<>();
		campoFeedback = new JTextArea(4, 20);
		labelAluno = new JLabel("(não definido)");
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

		for (String q : QUESTOES) {
			gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel(q + ":"), gbc);
			JComboBox<Integer> cb = new JComboBox<>(new Integer[]{1,2,3,4,5});
			cb.setSelectedItem(5);
			combosNotas.add(cb);
			gbc.gridx = 1; panel.add(cb, gbc);
			row++;
		}

		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Feedback (opcional):"), gbc);
		gbc.gridx = 1; panel.add(new JScrollPane(campoFeedback), gbc); row++;

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

			Formulario formulario = formularioService.obterOuCriarFormularioInstrutorPadrao();

			Map<String, Pergunta> porTexto = formulario.getPerguntas().stream()
					.collect(Collectors.toMap(Pergunta::getTexto, Function.identity()));

			List<Nota> notas = new ArrayList<>();
			for (int i = 0; i < QUESTOES.size(); i++) {
				String texto = QUESTOES.get(i);
				Pergunta p = porTexto.get(texto);
				if (p == null) continue;
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
