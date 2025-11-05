package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication(scanBasePackages = "br.com.unit.modulo_avaliacao_relatorio")
public class ModuloAvaliacaoAlunoView {

    public static void main(String[] args) {
    System.setProperty("java.awt.headless", "false");

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ModuloAvaliacaoAlunoView.class)
                        .headless(false)
                        .web(WebApplicationType.NONE)
                        .run(args);

        SwingUtilities.invokeLater(() -> {
                AvaliacaoCursoView view = context.getBean(AvaliacaoCursoView.class);
            view.setVisible(true);
        });
    }
}
