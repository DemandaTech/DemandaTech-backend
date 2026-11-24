package br.udesc.edu.demandatech.model.projection;

import br.udesc.edu.demandatech.model.dto.relatorio.DezFuncionariosMaisProdutivos;

import java.util.List;

public record VisaoGeralProjection(
        long totalDemandas,
        long abertas,
        long aguardando,
        double taxaConclusao,
        List<DemandaProjection> chamadosRecentes,
        List<DezFuncionariosMaisProdutivos> melhoresFuncionarios
) {}
