package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import com.itextpdf.text.Document;
import java.io.ByteArrayOutputStream;

public record Doc(Document doc, ByteArrayOutputStream baos, Fontes fonts) { }
