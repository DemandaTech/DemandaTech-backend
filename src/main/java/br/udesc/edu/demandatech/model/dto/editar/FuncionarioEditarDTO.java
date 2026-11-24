package br.udesc.edu.demandatech.model.dto.editar;

import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record FuncionarioEditarDTO(
        @NotEmpty(message = "Campo nome está vazio")
        @Size(max = 100, message = "Campo nome tem mais de 100 caracteres")
        String nome,
        @NotEmpty(message = "Campo email está vazio")
        String email,
        @Size(max = 60, message = "Campo senha tem mais de 60 caracteres")
        String senha,
        Departamento departamento,
        Perfil perfil
){}
