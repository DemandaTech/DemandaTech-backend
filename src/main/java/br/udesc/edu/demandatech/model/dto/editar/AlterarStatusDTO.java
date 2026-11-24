package br.udesc.edu.demandatech.model.dto.editar;

import br.udesc.edu.demandatech.model.enumerators.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlterarStatusDTO(
        @NotNull(message = "Campo status está vazio")
        Status status,
        @Size(max = 250, message = "Campo justificativa tem mais de 250 caracteres")
        String justificativa
) {
}
