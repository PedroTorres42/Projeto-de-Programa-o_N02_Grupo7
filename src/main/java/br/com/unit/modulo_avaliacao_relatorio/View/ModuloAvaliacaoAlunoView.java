package br.com.unit.modulo_avaliacao_relatorio.View;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
// (Sem import necessário: mesma package)

@SpringBootApplication(scanBasePackages = "br.com.unit.modulo_avaliacao_relatorio")
public class ModuloAvaliacaoAlunoView {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false"); // Habilita GUI

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ModuloAvaliacaoAlunoView.class)
                        .headless(false)
                        .web(WebApplicationType.NONE) // Desativa o servidor web
                        .run(args);

        SwingUtilities.invokeLater(() -> {
                AvaliacaoAlunoView view = context.getBean("avaliacaoAlunoView", AvaliacaoAlunoView.class);
            view.setVisible(true);
        });
    }
}
