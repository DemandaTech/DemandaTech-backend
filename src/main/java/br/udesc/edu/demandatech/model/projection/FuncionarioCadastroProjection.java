package br.udesc.edu.demandatech.model.projection;

import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;

public record FuncionarioCadastroProjection(
        Long matricula,
        String nome,
        String email,
        Perfil perfil,
        Long idDepartamento,
        String departamento
) {
    public static FuncionarioCadastroProjection de(Funcionario f) {
        return new FuncionarioCadastroProjection(
                f.getMatricula(), f.getNome(), f.getEmail(), f.getPerfil(),
                f.getDepartamento() != null ? f.getDepartamento().getIdDepartamento() : null,
                f.getDepartamento() != null ? f.getDepartamento().getDescricao() : null
        );
    }
}
