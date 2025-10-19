package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RespostaId {
    private Long avaliacaoId;
    private Long perguntaId;
}
