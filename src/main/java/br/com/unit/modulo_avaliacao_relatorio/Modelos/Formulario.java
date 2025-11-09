package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "formularios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(exclude = {"perguntas"})
@ToString(exclude = {"perguntas"})
public class Formulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @OneToMany(mappedBy = "formulario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Pergunta> perguntas;

    @Enumerated(EnumType.STRING)
    private TipoFormulario tipo;

    public enum TipoFormulario {
        INSTRUTOR,
        ALUNO,
        CURSO
    }
}