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
public class Formulario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @OneToMany(mappedBy = "formulario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pergunta> perguntas;

    @Enumerated(EnumType.STRING)
    private TipoFormulario tipo;

    public enum TipoFormulario {
        INSTRUTOR,
        ALUNO,
        CURSO
    }

    // Exibir apenas o título em componentes de UI sem tocar nas perguntas (LAZY)
    @Override
    public String toString() {
        return (titulo != null && !titulo.isBlank()) ? titulo : ("Formulario#" + id);
    }
}