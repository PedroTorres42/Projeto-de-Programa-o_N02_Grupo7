package br.com.unit.modulo_avaliacao_relatorio.Classes;


import jakarta.persistence.*;
import lombok.*;
import br.com.unit.moduloavaliacao.model.curso.Curso;
import br.com.unit.moduloavaliacao.model.avaliacao.Avaliacao;
import java.util.List;


@Entity
@DiscriminatorValue("ALUNO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aluno extends Usuario {


    private String matricula;


    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso cursoAtual;


    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes;


    public void preencherAvaliacao(Avaliacao avaliacao) {
    }


    public void visualizarRelatoriosIndividuais() {
    }
}