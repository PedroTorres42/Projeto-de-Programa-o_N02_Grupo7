package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Table(name = "cursos")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"instrutores"})
@ToString(exclude = {"instrutores"})
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private Integer cargaHoraria;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "curso_instrutor")
    private List<Instrutor> instrutores;
}
