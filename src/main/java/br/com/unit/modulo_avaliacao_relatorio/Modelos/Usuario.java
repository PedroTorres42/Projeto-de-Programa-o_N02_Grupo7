package br.com.unit.modulo_avaliacao_relatorio.Modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING, length = 20)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    // ⚠️ NÃO mapear este campo à coluna — evitar conflito com @DiscriminatorColumn.
    // Se quiser manter no código para uso em DTOs/Views, deixe como não persistente:
    @Transient
    private TipoUsuario tipoUsuario; // apenas informativo/derivado

    public enum TipoUsuario {
        Administrador,
        Instrutor,
        Aluno
    }
}
