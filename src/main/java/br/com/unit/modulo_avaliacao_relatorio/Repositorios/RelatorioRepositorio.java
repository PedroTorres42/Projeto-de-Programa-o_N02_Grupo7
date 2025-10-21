package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatorioRepositorio extends JpaRepository<Relatorio, Long> {}
