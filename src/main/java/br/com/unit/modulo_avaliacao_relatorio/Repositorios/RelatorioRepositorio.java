package br.com.unit.modulo_avaliacao_relatorio.Repositorios;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelatorioRepositorio extends JpaRepository<Relatorio, Long> {
    
    @Query("SELECT r FROM Relatorio r WHERE r.tipo = :tipo ORDER BY r.data DESC")
    List<Relatorio> findByTipoOrderByDataDesc(@Param("tipo") Relatorio.TipoRelatorio tipo);
    
    
    @Query("SELECT r FROM Relatorio r ORDER BY r.data DESC")
    List<Relatorio> findAllOrderByDataDesc();
}
