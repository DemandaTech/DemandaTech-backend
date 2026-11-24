package br.udesc.edu.demandatech.model.projection;

public record RelatorioDepartamentoProjection(
        Long idDepartamento,
        String departamento,
        Long qtdDemandas,
        double percentual
) {
    public RelatorioDepartamentoProjection(Long idDepartamento, String departamento, Long qtdDemandas) {
        this(idDepartamento, departamento, qtdDemandas, 0);
    }

    public RelatorioDepartamentoProjection comPercentual(long total) {
        double valor = total == 0 ? 0 : Math.round(qtdDemandas * 1000.0 / total) / 10.0;
        return new RelatorioDepartamentoProjection(idDepartamento, departamento, qtdDemandas, valor);
    }
}
