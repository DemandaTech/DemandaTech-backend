package br.udesc.edu.demandatech.model.dto.criar;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstornoDemandaCriarDTO(
    @JsonAlias("justificativa")
    @NotBlank(message = "Campo justificativa está vazio")
    @Size(max = 50, message = "Campo justificativa tem mais de 50 caracteres")
    String descricao,
    @NotNull(message = "Campo demanda está vazio")
    Long idDemanda
)
{ }
