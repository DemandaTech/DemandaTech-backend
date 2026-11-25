package br.udesc.edu.demandatech.model.projection;

import br.udesc.edu.demandatech.model.enumerators.Prioridade;
import br.udesc.edu.demandatech.model.enumerators.Status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DemandaDetalheProjection (
        Long idDemanda,
        String titulo,
        String descricao,
        String justificativa,
        Status status,
        Prioridade prioridade,
        Long matriculaResponsavel,
        String responsavel,
        Long matriculaCriador,
        String criador,
        LocalDate dataAbertura,
        LocalTime horaAbertura,
        Long idTipo,
        String tipoDemanda,
        String departamento,
        String departamentoCriador,
        List<ColaboradorProjection> funcionariosEnvolvidos
) {
    public DemandaDetalheProjection(Long idDemanda, String titulo, String descricao, String justificativa,
                                    Status status, Prioridade prioridade, Long matriculaResponsavel,
                                    String responsavel, Long matriculaCriador, String criador,
                                    LocalDate dataAbertura, LocalTime horaAbertura,
                                    Long idTipo, String tipoDemanda, String departamento,
                                    String departamentoCriador) {
        this(idDemanda, titulo, descricao, justificativa, status, prioridade, matriculaResponsavel, responsavel,
             matriculaCriador, criador, dataAbertura, horaAbertura, idTipo, tipoDemanda, departamento,
             departamentoCriador, List.of());
    }

    public DemandaDetalheProjection comFuncionariosEnvolvidos(List<ColaboradorProjection> envolvidos) {
        return new DemandaDetalheProjection(idDemanda, titulo, descricao, justificativa, status, prioridade,
                matriculaResponsavel, responsavel, matriculaCriador, criador, dataAbertura, horaAbertura,
                idTipo, tipoDemanda, departamento, departamentoCriador, envolvidos);
    }
}
