package br.udesc.edu.demandatech.model.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RelatorioProjection(
        LocalDateTime geradoEm,
        LocalDate inicio,
        LocalDate fim,
        Long idDepartamento,
        long totalDemandas,
        long demandasConcluidas,
        long estornosRegistrados,
        long emAndamento,
        List<RelatorioDepartamentoProjection> demandasPorDepartamento,
        List<RelatorioEstornoResponsavelProjection> estornosPorResponsavel,
        List<RelatorioFuncionarioProjection> maisProdutivos
) {}
