package br.udesc.edu.demandatech.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroDTO(
        @NotEmpty(message = "Campo nome está vazio")
        @Size(max = 255, message = "Campo nome tem mais de 255 caracteres")
        String nome,
        @NotEmpty(message = "Campo email está vazio")
        @Size(max = 255, message = "Campo email tem mais de 255 caracteres")
        String email,
        @NotNull(message = "Campo departamento está vazio")
        Long idDepartamento,
        @NotEmpty(message = "Campo senha está vazio")
        @Size(min = 6, max = 60, message = "A senha deve ter entre 6 e 60 caracteres")
        String senha
) {
}
