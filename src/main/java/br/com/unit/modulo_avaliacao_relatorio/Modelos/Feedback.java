package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "feedbacks")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"avaliacao", "usuario"})
@ToString(exclude = {"avaliacao", "usuario"})
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comentario;

    @OneToOne
    @JoinColumn(name = "avaliacao_id")
    private Avaliacao avaliacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
