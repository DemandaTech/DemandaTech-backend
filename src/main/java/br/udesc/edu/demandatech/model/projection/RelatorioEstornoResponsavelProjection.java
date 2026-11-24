package br.udesc.edu.demandatech.model.projection;

public record RelatorioEstornoResponsavelProjection(
        Long matricula,
        String nome,
        Long qtdEstornos
) {}
