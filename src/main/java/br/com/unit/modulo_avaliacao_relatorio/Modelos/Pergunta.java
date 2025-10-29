package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Table(name = "perguntas")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String texto;

    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas;

    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private Formulario formulario;

}
