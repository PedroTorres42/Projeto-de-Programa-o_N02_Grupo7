package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


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