Tentei instrutor:
package br.com.unit.moduloavaliacao.model.usuario;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import br.com.unit.moduloavaliacao.model.curso.Curso;
import br.com.unit.moduloavaliacao.model.avaliacao.Avaliacao;


@Entity
@DiscriminatorValue("INSTRUTOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instrutor extends Usuario {


    private String especialidade;


    @ManyToMany(mappedBy = "instrutores")
    private List<Curso> cursos;


    public void criarFormularioAvaliacao() {  }
    public void consultarFeedbacks() {  }
    public void consultarRelatorioCurso() {  }


    @OneToMany(mappedBy = "instrutor")
    private List<Avaliacao> avaliacoesCriadas;
}