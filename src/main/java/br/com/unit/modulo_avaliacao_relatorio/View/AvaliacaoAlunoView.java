package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Lazy
@Component
public class AvaliacaoAlunoView extends JFrame {

	private final UsuarioService usuarioService;
	private final CursoService cursoService;
	private final AvaliacaoService avaliacaoService;
	private final FormularioService formularioService;

	private JComboBox<Aluno> comboAluno;
	private JComboBox<Curso> comboCurso;
	private JComboBox<Instrutor> comboInstrutor;
	private JTextArea campoFeedback;
	private final List<JComboBox<Integer>> combosNotas = new ArrayList<>();

	private static final List<String> QUESTOES = Arrays.asList(
			"Qualidade do conteúdo",
			"Didática do instrutor",
			"Carga horária",
			"Organização",
			"Avaliação geral"
	);

	public AvaliacaoAlunoView(UsuarioService usuarioService,
						  CursoService cursoService,
						  AvaliacaoService avaliacaoService,
						  FormularioService formularioService) {
		this.usuarioService = usuarioService;
		this.cursoService = cursoService;
		this.avaliacaoService = avaliacaoService;
		this.formularioService = formularioService;

		setTitle("Avaliação de Curso/Instrutor (Aluno)");
		setSize(600, 520);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
	}

	private void initComponents() {
		comboAluno = new JComboBox<>();
		comboCurso = new JComboBox<>();
		comboInstrutor = new JComboBox<>();
		campoFeedback = new JTextArea(4, 20);

		// Popular combos
		usuarioService.listarAlunos().forEach(comboAluno::addItem);
		cursoService.listarCursos().forEach(comboCurso::addItem);
		usuarioService.listarInstrutores().forEach(comboInstrutor::addItem);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int row = 0;

		// Seleções
		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Aluno:"), gbc);
		gbc.gridx = 1; panel.add(comboAluno, gbc); row++;

		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Curso:"), gbc);
		gbc.gridx = 1; panel.add(comboCurso, gbc); row++;

		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Instrutor:"), gbc);
		gbc.gridx = 1; panel.add(comboInstrutor, gbc); row++;

		// Questões 1..5
		for (String q : QUESTOES) {
			gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel(q + ":"), gbc);
			JComboBox<Integer> cb = new JComboBox<>(new Integer[]{1,2,3,4,5});
			cb.setSelectedItem(5);
			combosNotas.add(cb);
			gbc.gridx = 1; panel.add(cb, gbc);
			row++;
		}

		// Feedback
		gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Feedback (opcional):"), gbc);
		gbc.gridx = 1; panel.add(new JScrollPane(campoFeedback), gbc); row++;

		// Botão salvar
		JButton btnSalvar = new JButton("Salvar Avaliação");
		btnSalvar.addActionListener(this::salvar);
		gbc.gridx = 1; gbc.gridy = row; panel.add(btnSalvar, gbc);

		add(panel);
	}

	private void salvar(ActionEvent e) {
		try {
			Aluno aluno = (Aluno) comboAluno.getSelectedItem();
			Curso curso = (Curso) comboCurso.getSelectedItem();
			Instrutor instrutor = (Instrutor) comboInstrutor.getSelectedItem();
			if (aluno == null || curso == null || instrutor == null) {
				JOptionPane.showMessageDialog(this, "Selecione aluno, curso e instrutor.");
				return;
			}

			// Garante existir um formulário padrão ALUNO com as questões
			Formulario formulario = formularioService.obterOuCriarFormularioAlunoPadrao();

			// Mapa de perguntas por texto (para vincular às notas)
			Map<String, Pergunta> porTexto = formulario.getPerguntas().stream()
					.collect(Collectors.toMap(Pergunta::getTexto, Function.identity()));

			List<Nota> notas = new ArrayList<>();
			for (int i = 0; i < QUESTOES.size(); i++) {
				String texto = QUESTOES.get(i);
				Pergunta p = porTexto.get(texto);
				if (p == null) continue; // degradar graciosamente
				Integer valor = (Integer) combosNotas.get(i).getSelectedItem();
				Nota n = new Nota();
				n.setNota(valor);
				n.setPergunta(p);
				notas.add(n);
			}

			Avaliacao avaliacao = new Avaliacao();
			avaliacao.setAluno(aluno);
			avaliacao.setCurso(curso);
			avaliacao.setInstrutor(instrutor);
			avaliacao.setFormulario(formulario);
			avaliacao.setNotas(notas);
			// Link inverso das notas
			notas.forEach(n -> n.setAvaliacao(avaliacao));

			String fbTxt = campoFeedback.getText();
			if (fbTxt != null && !fbTxt.isBlank()) {
				Feedback fb = new Feedback();
				fb.setComentario(fbTxt.trim());
				fb.setAvaliacao(avaliacao);
				avaliacao.setFeedback(fb);
			}

			Avaliacao salva = avaliacaoService.salvarAvaliacao(avaliacao);
			JOptionPane.showMessageDialog(this, "Avaliação salva com ID: " + (salva != null ? salva.getId() : "—"));

			// Reset simples
			combosNotas.forEach(cb -> cb.setSelectedItem(5));
			campoFeedback.setText("");

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
		}
	}
}
