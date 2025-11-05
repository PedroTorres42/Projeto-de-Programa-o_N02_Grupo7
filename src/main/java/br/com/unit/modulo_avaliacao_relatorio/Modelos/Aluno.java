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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_id")
    private Curso cursoAtual;


    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes;

    @Override
    public String toString() {
        String base = (getNome() != null && !getNome().isBlank()) ? getNome() : ("Aluno#" + getId());
        return (matricula != null && !matricula.isBlank()) ? base + " (" + matricula + ")" : base;
    }
}