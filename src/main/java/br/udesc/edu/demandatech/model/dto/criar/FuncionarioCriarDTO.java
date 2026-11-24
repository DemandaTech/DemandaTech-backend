package br.udesc.edu.demandatech.model.dto.criar;

import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record FuncionarioCriarDTO(
    @NotEmpty(message = "Campo nome está vazio")
    @Size(max = 100, message = "Campo nome tem mais de 100 caracteres")
    String nome,
    @NotEmpty(message = "Campo email está vazio")
    @Size(max = 255, message = "Campo email tem mais de 255 caracteres")
    String email,
    @NotEmpty(message = "Campo senha está vazio")
    @Size(max = 60, message = "Campo senha tem mais de 60 caracteres")
    String senha,
    Departamento departamento,
    Perfil perfil
)
{ }
