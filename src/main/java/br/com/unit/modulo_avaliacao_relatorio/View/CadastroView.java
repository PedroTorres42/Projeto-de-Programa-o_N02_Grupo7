package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Administrador;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Aluno;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Instrutor;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Service.UsuarioService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class CadastroView extends JFrame {
    
    private JTextField nomeField;
    private JTextField emailField;
    private JPasswordField senhaField;
    private JPasswordField confirmarSenhaField;
    private JComboBox<String> tipoUsuarioCombo;
    private JTextField matriculaField;
    private JTextField especialidadeField;
    private JLabel lblMatricula;
    private JLabel lblEspecialidade;
    private JPanel painelCamposEspecificos;
    private JButton btnCadastrar;
    private JButton btnLimpar;
    private JButton btnVoltar;
    
    private final UsuarioService usuarioService;
    
    @Autowired
    private InicialView inicialView;
    
    @Autowired
    private MenuView menuView;
    
    public CadastroView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Avaliação - Cadastro de Usuário");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painelPrincipal = UIUtils.paddedBorderLayout(20);

        JPanel painelTitulo = new JPanel();
        painelTitulo.setBackground(UIConstants.BG);
        JLabel lblTitulo = UIUtils.titleLabel("Cadastro de Usuário");
        painelTitulo.add(lblTitulo);

        JPanel painelCampos = new JPanel(new GridBagLayout());
        painelCampos.setBackground(UIConstants.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(new Font("Arial", Font.BOLD, 12));
        painelCampos.add(lblNome, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nomeField = new JTextField(25);
        painelCampos.add(nomeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 12));
        painelCampos.add(lblEmail, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField(25);
        painelCampos.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(new Font("Arial", Font.BOLD, 12));
        painelCampos.add(lblSenha, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        senhaField = new JPasswordField(25);
        painelCampos.add(senhaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel lblConfirmarSenha = new JLabel("Confirmar Senha:");
        lblConfirmarSenha.setFont(new Font("Arial", Font.BOLD, 12));
        painelCampos.add(lblConfirmarSenha, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        confirmarSenhaField = new JPasswordField(25);
        painelCampos.add(confirmarSenhaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        JLabel lblTipoUsuario = new JLabel("Tipo de Usuário:");
        lblTipoUsuario.setFont(new Font("Arial", Font.BOLD, 12));
        painelCampos.add(lblTipoUsuario, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String[] tipos = {"Aluno", "Instrutor"};
        tipoUsuarioCombo = new JComboBox<>(tipos);
        tipoUsuarioCombo.addActionListener(e -> atualizarCamposEspecificos());
        painelCampos.add(tipoUsuarioCombo, gbc);

        painelCamposEspecificos = new JPanel(new GridBagLayout());
        painelCamposEspecificos.setBackground(UIConstants.BG);

        GridBagConstraints gbcEsp = new GridBagConstraints();
        gbcEsp.insets = new Insets(8, 8, 8, 8);
        gbcEsp.fill = GridBagConstraints.HORIZONTAL;

        gbcEsp.gridx = 0;
        gbcEsp.gridy = 0;
        gbcEsp.weightx = 0.3;
        lblMatricula = new JLabel("Matrícula:");
        lblMatricula.setFont(new Font("Arial", Font.BOLD, 12));
        painelCamposEspecificos.add(lblMatricula, gbcEsp);

        gbcEsp.gridx = 1;
        gbcEsp.weightx = 0.7;
        matriculaField = new JTextField(25);
        painelCamposEspecificos.add(matriculaField, gbcEsp);

        gbcEsp.gridx = 0;
        gbcEsp.gridy = 1;
        gbcEsp.weightx = 0.3;
        lblEspecialidade = new JLabel("Especialidade:");
        lblEspecialidade.setFont(new Font("Arial", Font.BOLD, 12));
        lblEspecialidade.setVisible(false);
        painelCamposEspecificos.add(lblEspecialidade, gbcEsp);

        gbcEsp.gridx = 1;
        gbcEsp.weightx = 0.7;
        especialidadeField = new JTextField(25);
        especialidadeField.setVisible(false);
        painelCamposEspecificos.add(especialidadeField, gbcEsp);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        painelCampos.add(painelCamposEspecificos, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoes.setBackground(UIConstants.BG);

        btnCadastrar = UIUtils.successButton("Cadastrar", this::cadastrarUsuario);
        btnCadastrar.setPreferredSize(new Dimension(120, 35));

        btnLimpar = UIUtils.warningButton("Limpar", this::limparCampos);
        btnLimpar.setPreferredSize(new Dimension(120, 35));

        btnVoltar = UIUtils.dangerButton("Voltar", this::voltar);
        btnVoltar.setPreferredSize(new Dimension(120, 35));
        btnVoltar.setForeground(Color.WHITE);
        btnVoltar.setFont(new Font("Arial", Font.BOLD, 12));
        btnVoltar.setFocusPainted(false);
        btnVoltar.setBorderPainted(false);
        btnVoltar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVoltar.addActionListener(e -> voltar());

        painelBotoes.add(btnCadastrar);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnVoltar);

        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipal.add(painelCampos, BorderLayout.CENTER);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal);

        adicionarEfeitosHover();
    }
    
    private void adicionarEfeitosHover() {
        btnCadastrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnCadastrar.setBackground(new Color(46, 139, 87));
            }
            public void mouseExited(MouseEvent evt) {
                btnCadastrar.setBackground(new Color(60, 179, 113));
            }
        });

        btnLimpar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnLimpar.setBackground(new Color(235, 140, 0));
            }
            public void mouseExited(MouseEvent evt) {
                btnLimpar.setBackground(new Color(255, 165, 0));
            }
        });
        
        btnVoltar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnVoltar.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(MouseEvent evt) {
                btnVoltar.setBackground(new Color(220, 53, 69));
            }
        });
    }
    
    private void atualizarCamposEspecificos() {
        String tipoSelecionado = (String) tipoUsuarioCombo.getSelectedItem();
        
        lblMatricula.setVisible(false);
        matriculaField.setVisible(false);
        lblEspecialidade.setVisible(false);
        especialidadeField.setVisible(false);

        switch (tipoSelecionado) {
            case "Aluno":
                lblMatricula.setVisible(true);
                matriculaField.setVisible(true);
                break;
            case "Instrutor":
                lblEspecialidade.setVisible(true);
                especialidadeField.setVisible(true);
                break;
            case "Administrador":
            case null:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tipoSelecionado);
        }
        
        painelCamposEspecificos.revalidate();
        painelCamposEspecificos.repaint();
    }
    
    private void cadastrarUsuario() {
        try {
            String nome = nomeField.getText().trim();
            String email = emailField.getText().trim();
            String senha = new String(senhaField.getPassword());
            String confirmarSenha = new String(confirmarSenhaField.getPassword());
            String tipoUsuario = (String) tipoUsuarioCombo.getSelectedItem();
            
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor, preencha todos os campos obrigatórios!",
                    "Campos vazios",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this,
                    "Por favor, insira um email válido!",
                    "Email inválido",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (usuarioService.buscarPorEmail(email).isPresent()) {
                JOptionPane.showMessageDialog(this,
                    "Este email já está cadastrado no sistema!",
                    "Email duplicado",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (senha.length() < 4) {
                JOptionPane.showMessageDialog(this,
                    "A senha deve ter no mínimo 4 caracteres!",
                    "Senha fraca",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                JOptionPane.showMessageDialog(this,
                    "As senhas não coincidem!",
                    "Erro de senha",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Usuario usuario = null;

            switch (tipoUsuario) {
                case "Aluno":
                    String matricula = matriculaField.getText().trim();
                    if (matricula.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                            "Por favor, informe a matrícula do aluno!",
                            "Campo obrigatório",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Aluno aluno = new Aluno();
                    aluno.setNome(nome);
                    aluno.setEmail(email);
                    aluno.setSenha(senha);
                    aluno.setMatricula(matricula);
                    usuario = aluno;
                    break;
                    
                case "Instrutor":
                    String especialidade = especialidadeField.getText().trim();
                    if (especialidade.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                            "Por favor, informe a especialidade do instrutor!",
                            "Campo obrigatório",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Instrutor instrutor = new Instrutor();
                    instrutor.setNome(nome);
                    instrutor.setEmail(email);
                    instrutor.setSenha(senha);
                    instrutor.setEspecialidade(especialidade);
                    usuario = instrutor;
                    break;
                    
                case "Administrador":
                    Administrador admin = new Administrador();
                    admin.setNome(nome);
                    admin.setEmail(email);
                    admin.setSenha(senha);
                    usuario = admin;
                    break;
                case null:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + tipoUsuario);
            }

            usuarioService.salvarUsuario(usuario);

            JOptionPane.showMessageDialog(this,
                "Usuário cadastrado com sucesso!\n\n" +
                "Nome: " + nome + "\n" +
                "Email: " + email + "\n" +
                "Tipo: " + tipoUsuario,
                "Cadastro realizado",
                JOptionPane.INFORMATION_MESSAGE);

            limparCampos();
            this.setVisible(false);
            if (menuView != null) {
                menuView.setUsuarioAtual(usuario);
                menuView.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Menu não disponível. Não foi possível abrir o menu após o cadastro.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao cadastrar usuário: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limparCampos() {
        nomeField.setText("");
        emailField.setText("");
        senhaField.setText("");
        confirmarSenhaField.setText("");
        matriculaField.setText("");
        especialidadeField.setText("");
        tipoUsuarioCombo.setSelectedIndex(0);
        atualizarCamposEspecificos();
        nomeField.requestFocus();
    }
    
    private void voltar() {
        int resposta = JOptionPane.showConfirmDialog(this,
            "Deseja realmente voltar? Os dados não salvos serão perdidos.",
            "Confirmar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (resposta == JOptionPane.YES_OPTION) {
            limparCampos();
            this.setVisible(false);
            if (inicialView != null) {
                inicialView.exibir();
            } else {
                System.exit(0);
            }
        }
    }
    
    public void exibir() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}

