package br.com.unit.modulo_avaliacao_relatorio.Modelos;


import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.*;


@Entity
@DiscriminatorValue("ADMINISTRADOR")
@Data
@EqualsAndHashCode(callSuper=true)
public class Administrador extends Usuario {}