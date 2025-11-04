package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Table(name = "relatorios")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Relatorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum TipoRelatorio {
        CURSO,
        INSTRUTOR,
        ALUNO
    }

    @Enumerated(EnumType.STRING)
    private TipoRelatorio tipo;

    @CreatedDate
    private LocalDate data;


    @Lob
    private byte[] documento;
}
