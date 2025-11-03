package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import java.time.LocalDate;

public record AvaliacaoCsvRow(
        Long avaliacaoId,
        String cursoNome,
        String instrutorNome,
        String alunoNome,
        Double notaMedia,
        LocalDate data
) {}
