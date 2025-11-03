package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class InicialView extends JFrame {
    
    private JButton btnLogin;
    private JButton btnCadastrar;
    
    @Autowired
    private LoginView loginView;
    
    @Autowired
    private CadastroView cadastroView;
    
    public InicialView() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Avaliação - Bem-vindo");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        painelPrincipal.setBackground(new Color(240, 240, 245));
        
        JPanel painelCabecalho = new JPanel();
        painelCabecalho.setLayout(new BoxLayout(painelCabecalho, BoxLayout.Y_AXIS));
        painelCabecalho.setBackground(new Color(240, 240, 245));
        
        JLabel lblTitulo = new JLabel("Bem-vindo ao Sistema de Avaliação");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(51, 51, 51));
        lblTitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        JLabel lblSubtitulo = new JLabel("Módulo de Avaliação e Relatório");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(102, 102, 102));
        lblSubtitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        painelCabecalho.add(lblTitulo);
        painelCabecalho.add(Box.createRigidArea(new Dimension(0, 10)));
        painelCabecalho.add(lblSubtitulo);
        
        JPanel painelCentro = new JPanel();
        painelCentro.setLayout(new GridBagLayout());
        painelCentro.setBackground(new Color(240, 240, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        
        gbc.gridy = 0;
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setPreferredSize(new Dimension(250, 50));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> abrirLogin());
        painelCentro.add(btnLogin, gbc);
        
        gbc.gridy = 1;
        btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.setFont(new Font("Arial", Font.BOLD, 16));
        btnCadastrar.setPreferredSize(new Dimension(250, 50));
        btnCadastrar.setBackground(new Color(60, 179, 113));
        btnCadastrar.setForeground(Color.WHITE);
        btnCadastrar.setFocusPainted(false);
        btnCadastrar.setBorderPainted(false);
        btnCadastrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCadastrar.addActionListener(e -> abrirCadastro());
        painelCentro.add(btnCadastrar, gbc);
        
        JPanel painelRodape = new JPanel();
        painelRodape.setBackground(new Color(240, 240, 245));
        JLabel lblRodape = new JLabel("© 2025 - Sistema de Avaliação");
        lblRodape.setFont(new Font("Arial", Font.PLAIN, 10));
        lblRodape.setForeground(new Color(153, 153, 153));
        painelRodape.add(lblRodape);
        
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        painelPrincipal.add(painelCentro, BorderLayout.CENTER);
        painelPrincipal.add(painelRodape, BorderLayout.SOUTH);
        
        add(painelPrincipal);
        
        adicionarEfeitosHover();
    }
    
    private void adicionarEfeitosHover() {
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(70, 130, 180));
            }
        });
        
        btnCadastrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCadastrar.setBackground(new Color(46, 139, 87));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCadastrar.setBackground(new Color(60, 179, 113));
            }
        });
    }
    
    private void abrirLogin() {
        try {
            if (loginView != null) {
                loginView.exibir();
                this.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erro ao carregar tela de login. LoginView não inicializado.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao abrir tela de login: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirCadastro() {
        try {
            if (cadastroView != null) {
                cadastroView.exibir();
                this.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erro ao carregar tela de cadastro. CadastroView não inicializado.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao abrir tela de cadastro: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void exibir() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}
