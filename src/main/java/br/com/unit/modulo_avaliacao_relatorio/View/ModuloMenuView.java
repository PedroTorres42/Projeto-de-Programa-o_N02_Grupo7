package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Aluno;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Instrutor;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Service.UsuarioService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.util.List;

@SpringBootApplication(scanBasePackages = "br.com.unit.modulo_avaliacao_relatorio")
public class ModuloMenuView {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ModuloMenuView.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            try {
                MenuView menu = ctx.getBean(MenuView.class);
                UsuarioService us = ctx.getBean(UsuarioService.class);

                // Simples: escolhe um usuário para demonstrar o menu
                Usuario u = null;
                List<Aluno> alunos = us.listarAlunos();
                if (!alunos.isEmpty()) u = alunos.get(0);
                if (u == null) {
                    List<Instrutor> instrs = us.listarInstrutores();
                    if (!instrs.isEmpty()) u = instrs.get(0);
                }
                // Se nenhum, deixa sem usuário para mostrar mensagem padrão
                if (u != null) menu.setUsuarioAtual(u);

                menu.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Falha ao abrir o Menu: " + ex.getMessage());
            }
        });
    }
}
