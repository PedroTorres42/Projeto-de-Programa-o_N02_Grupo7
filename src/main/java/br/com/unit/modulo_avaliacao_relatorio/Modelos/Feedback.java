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
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String comentario;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
