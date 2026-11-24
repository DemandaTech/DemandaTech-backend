package br.udesc.edu.demandatech.model.dto.editar;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstornoDemandaEditarDTO(
    @JsonAlias("justificativa")
    @NotBlank(message = "Campo justificativa está vazio")
    @Size(max = 50, message = "Campo justificativa tem mais de 50 caracteres")
    String descricao
)
{ }
