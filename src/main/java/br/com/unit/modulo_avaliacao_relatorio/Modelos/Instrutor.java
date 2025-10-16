package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@DiscriminatorValue("INSTRUTOR")
@Data
@EqualsAndHashCode(callSuper=true)
public class Instrutor extends Usuario {


    private String especialidade;


    @ManyToMany(mappedBy = "instrutores")
    private List<String> cursos;


    public void criarFormularioAvaliacao() {  }
    public void consultarFeedbacks() {  }
    public void consultarRelatorioCurso() {  }


    @OneToMany(mappedBy = "instrutor")
    private List<Avaliacao> avaliacoesCriadas;
}