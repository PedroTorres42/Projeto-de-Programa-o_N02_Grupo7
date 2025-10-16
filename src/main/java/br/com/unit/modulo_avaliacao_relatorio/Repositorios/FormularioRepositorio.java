package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormularioRepositorio extends JpaRepository<Formulario, String> {}
