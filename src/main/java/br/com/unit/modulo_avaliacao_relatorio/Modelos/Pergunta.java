package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< Updated upstream
@Table(name = "perguntas")
=======
import java.util.List;

>>>>>>> Stashed changes
@Entity
@Table(name = "perguntas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String texto;

<<<<<<< Updated upstream
=======
    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pergunta", nullable = false)
    private TipoPergunta tipo;

    public enum TipoPergunta {
        FREQUENCIA,
        OUTRO
    }

>>>>>>> Stashed changes
    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private Formulario formulario;
}
