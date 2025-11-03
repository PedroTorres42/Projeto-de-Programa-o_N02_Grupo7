package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PerguntaRepositorio extends JpaRepository<Pergunta, Long> {}
