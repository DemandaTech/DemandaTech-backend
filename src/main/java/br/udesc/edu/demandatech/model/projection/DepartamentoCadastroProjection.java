package br.udesc.edu.demandatech.model.projection;

import br.udesc.edu.demandatech.model.entity.Departamento;

public record DepartamentoCadastroProjection(
        Long idDepartamento,
        String descricao,
        Long matriculaGerente,
        String gerente
) {
    public static DepartamentoCadastroProjection de(Departamento d) {
        return new DepartamentoCadastroProjection(
                d.getIdDepartamento(), d.getDescricao(),
                d.getGerente() != null ? d.getGerente().getMatricula() : null,
                d.getGerente() != null ? d.getGerente().getNome() : null
        );
    }
}