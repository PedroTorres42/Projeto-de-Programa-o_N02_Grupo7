package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "avaliacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private LocalDate data;

    @Column(nullable = false)
    private Double media;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_id")
    private Instrutor instrutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas;

    @OneToOne(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Feedback feedback;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "formulario_id", nullable = false)
    private Formulario formulario;
}
