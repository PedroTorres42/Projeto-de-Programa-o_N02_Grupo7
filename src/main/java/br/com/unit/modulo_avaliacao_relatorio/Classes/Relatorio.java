package br.com.unit.modulo_avaliacao_relatorio.Classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "relatorios")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Relatorio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    public enum TipoRelatorio {
        CURSO,
        INSTRUTOR,
        ALUNO
    }

    private TipoRelatorio tipo;
    private LocalDate data;
}
