Formulário: 
package br.com.unit.moduloavaliacao.model.avaliacao;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "formularios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formulario {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @ElementCollection
    private List<String> perguntas;


    public void adicionarPergunta(String p) { perguntas.add(p); }
    public void removerPergunta(String p) { perguntas.remove(p); }
    public void aplicarAvaliacao() {  }
}