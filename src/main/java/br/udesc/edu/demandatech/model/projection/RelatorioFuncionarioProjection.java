package br.udesc.edu.demandatech.model.projection;

public record RelatorioFuncionarioProjection(
        int posicao,
        Long matricula,
        String nome,
        String departamento,
        Long concluidas,
        Long pendencias,
        Long estornos
) {
    public RelatorioFuncionarioProjection(Long matricula, String nome, String departamento,
                                          Long concluidas, Long pendencias, Long estornos) {
        this(0, matricula, nome, departamento, concluidas, pendencias, estornos);
    }

    public RelatorioFuncionarioProjection comPosicao(int posicao) {
        return new RelatorioFuncionarioProjection(posicao, matricula, nome, departamento,
                concluidas, pendencias, estornos);
    }
}
