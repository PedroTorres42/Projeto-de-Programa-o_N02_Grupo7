package br.com.unit.modulo_avaliacao_relatorio;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import br.com.unit.modulo_avaliacao_relatorio.View.InicialView;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class ModuloAvaliacaoView {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false"); // Habilita GUI

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ModuloAvaliacaoView.class)
                        .headless(false)
                        .web(WebApplicationType.NONE) // Desativa o servidor web
                        .run(args);

        SwingUtilities.invokeLater(() -> {
            try {
                // Opcional: aplicar Look & Feel do sistema para uma aparência nativa
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception lnf) {
                    Logger.getLogger(ModuloAvaliacaoView.class.getName())
                          .log(Level.FINE, "Falha ao aplicar LookAndFeel do sistema", lnf);
                }

                InicialView view = context.getBean(InicialView.class);
                view.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erro ao iniciar a aplicação: " + ex.getMessage(),
                        "Erro de inicialização",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
