package br.com.unit.modulo_avaliacao_relatorio;

import br.com.unit.modulo_avaliacao_relatorio.View.AvaliacaoView;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class ModuloAvaliacaoRelatorioApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false"); // Habilita GUI

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ModuloAvaliacaoRelatorioApplication.class)
                        .headless(false)
                        .web(WebApplicationType.NONE) // Desativa o servidor web
                        .run(args);

        SwingUtilities.invokeLater(() -> {
            AvaliacaoView view = context.getBean(AvaliacaoView.class);
            view.setVisible(true);
        });
    }
}
