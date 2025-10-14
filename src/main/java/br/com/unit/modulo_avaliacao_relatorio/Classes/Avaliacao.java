package br.com.unit.moduloavaliacao.model.avaliacao;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import br.com.unit.moduloavaliacao.model.usuario.Aluno;
import br.com.unit.moduloavaliacao.model.usuario.Instrutor;


@Entity
@Table(name = "avaliacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    private LocalDate data;
    private Double nota;
    private String comentario;


    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;


    @ManyToOne
    @JoinColumn(name = "instrutor_id")
    private Instrutor instrutor;


    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private Formulario formulario;


    public void enviar() {  }
    public void editar() { }
}