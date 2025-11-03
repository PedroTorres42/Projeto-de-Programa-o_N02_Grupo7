package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import java.math.BigDecimal;

public record Metricas(Double mediaNota, double freq, BigDecimal pond, Sentimento sentimento, String feedbackResumo) { }
