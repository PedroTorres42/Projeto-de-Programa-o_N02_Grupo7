package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import com.itextpdf.text.Font;

public record Fontes(Font h1, Font h2, Font normal) {
    public Fontes() {
        this(new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD),
             new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD),
             new Font(Font.FontFamily.HELVETICA, 10));
    }
}
