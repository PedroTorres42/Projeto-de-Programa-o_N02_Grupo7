package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Table(name = "cursos")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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

        // Evitar carregar coleções LAZY em renderizações de UI (toString em combos)
        @Override
        public String toString() {
            return (nome != null && !nome.isBlank()) ? nome : ("Curso#" + id);
        }
}
