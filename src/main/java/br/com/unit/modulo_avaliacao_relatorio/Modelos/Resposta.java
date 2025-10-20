package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "respostas")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resposta {
    @EmbeddedId
    private RespostaId id;

    private Integer nota;

    @MapsId("avaliacao_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id")
    private Avaliacao avaliacao;

    @MapsId("pergunta_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id")
    private Pergunta pergunta;
}
