package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@DiscriminatorValue("ALUNO")
@Data
@EqualsAndHashCode(callSuper=true)
public class Aluno extends Usuario {
    private String matricula;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso cursoAtual;


    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes;
}