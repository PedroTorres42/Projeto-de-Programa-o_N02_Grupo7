package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.stereotype.Component;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Service.CursoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@Component
public class CadastroCursoView extends JFrame {
    
    private JTextField nomeField;
    private JTextField descricaoField;
    private JTextField cargaHorariaField;
    private JButton btnSalvar;
    private JButton btnLimpar;
    private JButton btnVoltar;
    private JTable tabelaCursos;
    private DefaultTableModel modeloTabela;
    
    private final CursoService cursoService;
    private boolean carregando = false;
    
    public CadastroCursoView(CursoService cursoService) {
        this.cursoService = cursoService;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Avaliação - Cadastro de Curso");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painelPrincipal = UIUtils.paddedBorderLayout(20);

        JPanel painelTitulo = new JPanel();
        painelTitulo.setBackground(UIConstants.BG);
        JLabel lblTitulo = UIUtils.titleLabel("Cadastro de Curso");
        painelTitulo.add(lblTitulo);

        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBackground(UIConstants.BG);
        painelFormulario.setBorder(BorderFactory.createTitledBorder("Novo Curso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(new Font("Arial", Font.BOLD, 12));
        painelFormulario.add(lblNome, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nomeField = new JTextField(30);
        painelFormulario.add(nomeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setFont(new Font("Arial", Font.BOLD, 12));
        painelFormulario.add(lblDescricao, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        descricaoField = new JTextField(30);
        painelFormulario.add(descricaoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel lblCargaHoraria = new JLabel("Carga Horária:");
        lblCargaHoraria.setFont(new Font("Arial", Font.BOLD, 12));
        painelFormulario.add(lblCargaHoraria, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cargaHorariaField = new JTextField(30);
        painelFormulario.add(cargaHorariaField, gbc);

        JPanel painelBotoesForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoesForm.setBackground(UIConstants.BG);

        btnSalvar = UIUtils.successButton("Salvar", this::salvarCurso);
        btnSalvar.setPreferredSize(new Dimension(120, 35));

        btnLimpar = UIUtils.warningButton("Limpar", this::limparCampos);
        btnLimpar.setPreferredSize(new Dimension(120, 35));

        painelBotoesForm.add(btnSalvar);
        painelBotoesForm.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        painelFormulario.add(painelBotoesForm, gbc);

        JPanel painelTabela = new JPanel(new BorderLayout());
        painelTabela.setBackground(UIConstants.BG);
        painelTabela.setBorder(BorderFactory.createTitledBorder("Cursos Cadastrados"));

        String[] colunas = {"ID", "Nome", "Descrição", "Carga Horária"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaCursos = new JTable(modeloTabela);
        tabelaCursos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCursos.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tabelaCursos);
        scrollPane.setPreferredSize(new Dimension(650, 200));
        painelTabela.add(scrollPane, BorderLayout.CENTER);

        JPanel painelBotaoVoltar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotaoVoltar.setBackground(UIConstants.BG);
        
        btnVoltar = UIUtils.dangerButton("Voltar", this::voltar);
        btnVoltar.setPreferredSize(new Dimension(120, 35));
        painelBotaoVoltar.add(btnVoltar);

        JPanel painelCentro = new JPanel();
        painelCentro.setLayout(new BoxLayout(painelCentro, BoxLayout.Y_AXIS));
        painelCentro.setBackground(UIConstants.BG);
        painelCentro.add(painelFormulario);
        painelCentro.add(Box.createRigidArea(new Dimension(0, 10)));
        painelCentro.add(painelTabela);

        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipal.add(painelCentro, BorderLayout.CENTER);
        painelPrincipal.add(painelBotaoVoltar, BorderLayout.SOUTH);

        add(painelPrincipal);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                carregarCursos();
            }
        });
    }
    
    private void salvarCurso() {
        try {
            String nome = nomeField.getText().trim();
            String descricao = descricaoField.getText().trim();
            String cargaHorariaStr = cargaHorariaField.getText().trim();
            
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor, informe o nome do curso!",
                    "Campo obrigatório",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Integer cargaHoraria = null;
            if (!cargaHorariaStr.isEmpty()) {
                try {
                    cargaHoraria = Integer.parseInt(cargaHorariaStr);
                    if (cargaHoraria <= 0) {
                        JOptionPane.showMessageDialog(this,
                            "A carga horária deve ser um número positivo!",
                            "Valor inválido",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                        "Por favor, informe um número válido para a carga horária!",
                        "Valor inválido",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            Curso curso = new Curso();
            curso.setNome(nome);
            curso.setDescricao(descricao);
            curso.setCargaHoraria(cargaHoraria);
            
            cursoService.criarCurso(curso);
            
            JOptionPane.showMessageDialog(this,
                "Curso cadastrado com sucesso!\n\n" +
                "Nome: " + nome,
                "Cadastro realizado",
                JOptionPane.INFORMATION_MESSAGE);
            
            limparCampos();
            carregarCursos();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao cadastrar curso: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarCursos() {
        if (carregando) {
            System.out.println("[CadastroCursoView] Carregamento já em andamento, ignorando...");
            return;
        }
        
        carregando = true;
        
        SwingWorker<List<Curso>, Void> worker = new SwingWorker<List<Curso>, Void>() {
            @Override
            protected List<Curso> doInBackground() throws Exception {
                System.out.println("[CadastroCursoView] Carregando cursos em background...");
                return cursoService.listarCursos();
            }
            
            @Override
            protected void done() {
                try {
                    List<Curso> cursos = get();
                    System.out.println("[CadastroCursoView] Cursos obtidos. Total: " + cursos.size());
                    
                    modeloTabela.setRowCount(0);
                    
                    for (Curso curso : cursos) {
                        Object[] row = {
                            curso.getId(),
                            curso.getNome(),
                            curso.getDescricao() != null ? curso.getDescricao() : "",
                            curso.getCargaHoraria() != null ? curso.getCargaHoraria() + "h" : ""
                        };
                        modeloTabela.addRow(row);
                    }
                    
                    System.out.println("[CadastroCursoView] Tabela atualizada com sucesso!");
                    
                } catch (Exception e) {
                    System.err.println("[CadastroCursoView] ERRO ao carregar cursos: " + e.getMessage());
                    e.printStackTrace();
                    
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(CadastroCursoView.this,
                            "Erro ao carregar cursos: " + e.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    carregando = false;
                }
            }
        };
        
        worker.execute();
    }
    
    private void limparCampos() {
        nomeField.setText("");
        descricaoField.setText("");
        cargaHorariaField.setText("");
        nomeField.requestFocus();
    }
    
    private void voltar() {
        this.dispose();
    }
    
    public void recarregarCursos() {
        carregarCursos();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(this::carregarCursos);
        }
    }
}
