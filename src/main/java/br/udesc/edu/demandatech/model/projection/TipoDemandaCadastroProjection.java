package br.udesc.edu.demandatech.model.projection;

import br.udesc.edu.demandatech.model.entity.TipoDemanda;

public record TipoDemandaCadastroProjection(
        Long idTipo,
        String descricao,
        Long idDepartamento,
        String departamento
) {
    public static TipoDemandaCadastroProjection de(TipoDemanda t) {
        return new TipoDemandaCadastroProjection(
                t.getIdTipo(), t.getDescricao(),
                t.getDepartamento().getIdDepartamento(), t.getDepartamento().getDescricao()
        );
    }
}