package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "formularios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formulario {

    // TODO: Criar classes Pergunta e Resposta para o formulario
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @ElementCollection
    private List<String> perguntas;
}