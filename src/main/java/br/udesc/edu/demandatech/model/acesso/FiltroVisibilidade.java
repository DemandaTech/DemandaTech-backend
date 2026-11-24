package br.udesc.edu.demandatech.model.acesso;

public final class FiltroVisibilidade {

    private FiltroVisibilidade() {
    }

    public static final String JOINS = """
             JOIN d.criador vc
             LEFT JOIN vc.departamento vcd
             LEFT JOIN d.responsavel vr
            """;

    public static final String CONDICAO = """
             (:escopo = 'TODOS'
              OR (:escopo = 'DEPARTAMENTO' AND vcd.idDepartamento = :idDepartamento)
              OR (:escopo = 'TECNICO' AND (vr.matricula = :matricula OR vc.matricula = :matricula))
              OR (:escopo = 'USUARIO' AND (vc.matricula = :matricula
                  OR EXISTS (SELECT 1 FROM FuncionarioEnvolvido fe
                             WHERE fe.demanda = d AND fe.funcionario.matricula = :matricula))))
            """;
}
