package br.udesc.edu.demandatech.model.projection;

public record ResumoDemandasProjection(
        long total,
        long abertas,
        long aguardando,
        long resolvidas
) {}
