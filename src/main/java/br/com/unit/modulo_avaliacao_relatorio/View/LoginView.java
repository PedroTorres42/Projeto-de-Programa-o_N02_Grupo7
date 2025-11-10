package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Service.UsuarioService;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

@Component
public class LoginView extends JFrame {
    
    private JTextField usuarioField;
    private JPasswordField senhaField;
    private final UsuarioService usuarioService;
    
    @Autowired
    private InicialView inicialView;
    
    @Autowired
    private MenuView menuView;

    public LoginView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Avaliação e Relatório - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel painelPrincipal = UIUtils.paddedBorderLayout(20);
            
        JPanel painelTitulo = new JPanel();
        JLabel lblTitulo = UIUtils.titleLabel("Sistema de Avaliação");
        painelTitulo.add(lblTitulo);
            
        JPanel painelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUsuario = new JLabel("Email/Matrícula:");
        painelCampos.add(lblUsuario, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usuarioField = new JTextField(20);
        painelCampos.add(usuarioField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblSenha = new JLabel("Senha:");
        painelCampos.add(lblSenha, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        senhaField = new JPasswordField(20);
        painelCampos.add(senhaField, gbc);
        
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnLogin = UIUtils.primaryButton("Entrar", this::fazerLogin);
        btnLogin.setPreferredSize(new Dimension(100, 30));


    JButton btnVoltar = UIUtils.dangerButton("Voltar", this::voltar);
    btnVoltar.setToolTipText("Fechar esta janela e retornar ao menu");
        btnVoltar.setPreferredSize(new Dimension(100, 30));
        
        painelBotoes.add(btnLogin);
        painelBotoes.add(btnVoltar);
        
        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipal.add(painelCampos, BorderLayout.CENTER);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        
        add(painelPrincipal);
        
        getRootPane().setDefaultButton(btnLogin);
    }
    
    private void fazerLogin() {
        String identificador = usuarioField.getText().trim();
        String senha = new String(senhaField.getPassword());
        
        if (identificador.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, preencha todos os campos!",
                "Campos vazios",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Optional<Usuario> usuarioOptional = usuarioService.buscarPorEmailOuMatricula(identificador);
            
            if (usuarioOptional.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Usuário não encontrado!\nVerifique o email ou matrícula informados.",
                    "Erro de autenticação",
                    JOptionPane.ERROR_MESSAGE);
                senhaField.setText("");
                return;
            }
            
            Usuario usuario = usuarioOptional.get();
            
            if (!usuario.getSenha().equals(senha)) {
                JOptionPane.showMessageDialog(this,
                    "Senha incorreta!",
                    "Erro de autenticação",
                    JOptionPane.ERROR_MESSAGE);
                senhaField.setText("");
                senhaField.requestFocus();
                return;
            }
            
            String tipoUsuario = identificarTipoUsuario(usuario);
            String infoIdentificador = identificador.contains("@") ? "Email: " + usuario.getEmail() : "Matrícula: " + identificador;
            
            JOptionPane.showMessageDialog(this,
                "Login realizado com sucesso!\n\n" +
                "Bem-vindo(a): " + usuario.getNome() + "\n" +
                infoIdentificador + "\n" +
                "Tipo: " + tipoUsuario,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            
            limparCampos();
            this.setVisible(false);
            if (menuView != null) {
                menuView.setUsuarioAtual(usuario);
                menuView.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Menu não disponível. Não foi possível abrir o menu após o login.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao realizar login: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }    private String identificarTipoUsuario(Usuario usuario) {
        String className = usuario.getClass().getSimpleName();
        return switch (className) {
            case "Instrutor" -> "Instrutor";
            case "Aluno" -> "Aluno";
            default -> "Usuário";
        };
    }
    
    
    private void voltar() {
        limparCampos();
        this.setVisible(false);
        if (inicialView != null) {
            inicialView.exibir();
        } else {
            System.exit(0);
        }
    }
    
    private void limparCampos() {
        usuarioField.setText("");
        senhaField.setText("");
        usuarioField.requestFocus();
    }
    
    public void exibir() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }

    
    
    public String getUsuario() {
        return usuarioField.getText();
    }
}
