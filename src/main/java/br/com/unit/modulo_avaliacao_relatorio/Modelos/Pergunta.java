package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Table(name = "perguntas")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"notas", "formulario"})
@ToString(exclude = {"notas", "formulario"})
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String texto;


    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pergunta", nullable = false)
    private TipoPergunta tipo;

    public enum TipoPergunta {
        FREQUENCIA,
        DIDATICA,
        PONTUALIDADE,
        ORGANIZACAO,
        CONTEUDO,
        CARGA_HORARIA,
        SATISFACAO,
        RECOMENDACAO,
        OUTRO
    }

    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private Formulario formulario;
}
