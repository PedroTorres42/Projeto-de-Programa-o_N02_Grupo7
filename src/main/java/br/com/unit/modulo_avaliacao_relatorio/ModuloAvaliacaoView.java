package br.com.unit.modulo_avaliacao_relatorio;

import br.com.unit.modulo_avaliacao_relatorio.View.InicialView;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

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
            InicialView view = context.getBean(InicialView.class);
            view.setVisible(true);
        });
    }
}
