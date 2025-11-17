package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class InicialView extends JFrame {

    private LoginView loginView;

    public InicialView() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Avaliação - Bem-vindo");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painelPrincipal = UIUtils.paddedBorderLayout(30);

        JPanel painelCabecalho = UIUtils.verticalBox(p -> {
            JLabel lblTitulo = UIUtils.titleLabel("Bem-vindo ao Sistema de Avaliação");
            lblTitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            p.add(lblTitulo);
            p.add(Box.createRigidArea(new Dimension(0,10)));
            JLabel lblSubtitulo = UIUtils.subtitleLabel("Módulo de Avaliação e Relatório");
            lblSubtitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            p.add(lblSubtitulo);
        });
        
        JPanel painelCentro = new JPanel(new GridBagLayout());
        painelCentro.setBackground(UIConstants.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        
        gbc.gridy = 0;
        JButton btnLogin = UIUtils.primaryButton("Login", this::abrirLogin);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setPreferredSize(new Dimension(250,50));
        painelCentro.add(btnLogin, gbc);
            
        JPanel painelRodape = new JPanel();
        painelRodape.setBackground(UIConstants.BG);
        JLabel lblRodape = new JLabel("© 2025 - Sistema de Avaliação");
        lblRodape.setFont(new Font("Arial", Font.PLAIN, 10));
        lblRodape.setForeground(new Color(153,153,153));
        painelRodape.add(lblRodape);
        
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        painelPrincipal.add(painelCentro, BorderLayout.CENTER);
        painelPrincipal.add(painelRodape, BorderLayout.SOUTH);
        
        add(painelPrincipal);
        
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
    
    public void exibir() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
