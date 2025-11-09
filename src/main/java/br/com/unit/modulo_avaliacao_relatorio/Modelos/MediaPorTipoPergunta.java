package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaPorTipoPergunta {
    private Pergunta.TipoPergunta tipo;
    private Double media;
    
    public MediaPorTipoPergunta(String tipo, Double media) {
        this.tipo = tipo != null ? Pergunta.TipoPergunta.valueOf(tipo) : null;
        this.media = media;
    }
}
