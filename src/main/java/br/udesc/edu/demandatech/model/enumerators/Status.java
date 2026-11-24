package br.udesc.edu.demandatech.model.enumerators;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    ABERTA("Aberta", 0),
    AGUARDANDO("Aguardando", 1),
    RESOLVIDA("Resolvida", 2),
    CANCELADA("Cancelada", 2);

    private final String descricao;
    private final int etapa;

    public boolean finalizada() {
        return this == RESOLVIDA || this == CANCELADA;
    }

    public boolean podeAvancarPara(Status novo) {
        return !finalizada() && novo.etapa > this.etapa;
    }
}
