package br.udesc.edu.demandatech.repository;

import static br.udesc.edu.demandatech.model.acesso.FiltroVisibilidade.CONDICAO;
import static br.udesc.edu.demandatech.model.acesso.FiltroVisibilidade.JOINS;

import br.udesc.edu.demandatech.model.dto.relatorio.DezFuncionariosMaisProdutivos;
import br.udesc.edu.demandatech.model.dto.relatorio.QtdDemandasPorDepartamento;
import br.udesc.edu.demandatech.model.entity.Demanda;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.entity.FuncionarioEnvolvido;
import br.udesc.edu.demandatech.model.enumerators.Prioridade;
import br.udesc.edu.demandatech.model.enumerators.Status;
import br.udesc.edu.demandatech.model.projection.DemandaDetalheProjection;
import br.udesc.edu.demandatech.model.projection.DemandaProjection;
import br.udesc.edu.demandatech.model.projection.RelatorioDepartamentoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandaRepository extends JpaRepository<Demanda, Long> {

    @Query("""
        SELECT d FROM Demanda d
        WHERE
            d.idDemanda = ?1 AND
            (d.criador = ?2 OR d.responsavel = ?2)
    """)
    Optional<Demanda> getDemandasByIdAndUsuario(Long id, Funcionario funcionario);

    @Query(value = """
        SELECT f.* FROM demanda_tech.funcionarios f
        LEFT JOIN demanda_tech.demandas d ON d.responsavel = f.matricula
                                         AND d.status NOT IN ('RESOLVIDA', 'CANCELADA')
        WHERE f.id_departamento = :idDepartamento
          AND f.perfil = 'FUNCIONARIO'
          AND f.matricula <> :criador
        GROUP BY f.matricula
        ORDER BY COUNT(d.id_demanda) ASC, f.matricula ASC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Funcionario> findFuncionarioComMenosDemandas(@Param("idDepartamento") Long idDepartamento,
                                                          @Param("criador") Long criador);

    @Query("""
        SELECT
            new br.udesc.edu.demandatech.model.dto.relatorio.QtdDemandasPorDepartamento(
                dp.idDepartamento, dp.descricao, COUNT(dem)
            )
        FROM Demanda dem
        JOIN dem.tipo td
        JOIN td.departamento dp
        WHERE dem.status = br.udesc.edu.demandatech.model.enumerators.Status.ABERTA
        GROUP BY dp.idDepartamento, dp.descricao
    """)
    List<QtdDemandasPorDepartamento> relatorioQtdDemandasPorDepartamento();

    @Query("""
        SELECT
            new br.udesc.edu.demandatech.model.dto.relatorio.DezFuncionariosMaisProdutivos(
                f.matricula, f.nome, COUNT(dem)
            )
        FROM Demanda dem
        JOIN dem.responsavel f
        WHERE dem.status = br.udesc.edu.demandatech.model.enumerators.Status.RESOLVIDA
        GROUP BY f.matricula, f.nome
        ORDER BY COUNT(dem) DESC
        LIMIT 10
    """)
    List<DezFuncionariosMaisProdutivos> relatorioDezFuncionariosMaisProdutivos();

    @Query(value = """
            SELECT
                new br.udesc.edu.demandatech.model.projection.DemandaProjection(
                    d.idDemanda, d.titulo, d.status, d.prioridade, t.descricao, vr.nome, d.data
                )
            FROM Demanda d
            JOIN d.tipo t
            """ + JOINS + """
            WHERE (:status IS NULL OR d.status = :status)
              AND (:prioridade IS NULL OR d.prioridade = :prioridade)
              AND (:idTipo IS NULL OR t.idTipo = :idTipo)
              AND (CAST(d.idDemanda AS String) LIKE :termo OR LOWER(d.titulo) LIKE :termo)
              AND """ + CONDICAO,
        countQuery = """
            SELECT COUNT(d)
            FROM Demanda d
            JOIN d.tipo t
            """ + JOINS + """
            WHERE (:status IS NULL OR d.status = :status)
              AND (:prioridade IS NULL OR d.prioridade = :prioridade)
              AND (:idTipo IS NULL OR t.idTipo = :idTipo)
              AND (CAST(d.idDemanda AS String) LIKE :termo OR LOWER(d.titulo) LIKE :termo)
              AND """ + CONDICAO)
    Page<DemandaProjection> listarDemandas(
        @Param("status") Status status,
        @Param("prioridade") Prioridade prioridade,
        @Param("idTipo") Long idTipo,
        @Param("termo") String termo,
        @Param("escopo") String escopo,
        @Param("matricula") Long matricula,
        @Param("idDepartamento") Long idDepartamento,
        Pageable pageable
    );

    @Query("SELECT COUNT(d) > 0 FROM Demanda d " + JOINS + " WHERE d.idDemanda = :id AND " + CONDICAO)
    boolean visivel(
        @Param("id") Long id,
        @Param("escopo") String escopo,
        @Param("matricula") Long matricula,
        @Param("idDepartamento") Long idDepartamento
    );

    @Query("""
    SELECT new br.udesc.edu.demandatech.model.projection.DemandaDetalheProjection(
        d.idDemanda, d.titulo, d.descricao, d.justificativa, d.status, d.prioridade,
        r.matricula, r.nome, c.matricula, c.nome,
        d.data, d.hora, t.idTipo, t.descricao, dep.descricao, cdep.descricao
    )
    FROM Demanda d
    JOIN d.criador c
    LEFT JOIN c.departamento cdep
    JOIN d.tipo t
    JOIN t.departamento dep
    LEFT JOIN d.responsavel r
    WHERE d.idDemanda = :id
    """)
    Optional<DemandaDetalheProjection> detalheDemanda(@Param("id") Long id);

    @Query("""
        SELECT COUNT(d)
        FROM Demanda d
        JOIN d.criador c
        WHERE (:status IS NULL OR d.status = :status)
          AND (:idDepartamento IS NULL OR c.departamento.idDepartamento = :idDepartamento)
    """)
    long contarVisaoGeral(@Param("status") Status status, @Param("idDepartamento") Long idDepartamento);

    @Query("""
        SELECT
            new br.udesc.edu.demandatech.model.dto.relatorio.DezFuncionariosMaisProdutivos(
                f.matricula, f.nome, COUNT(dem)
            )
        FROM Funcionario f
        LEFT JOIN Demanda dem ON dem.responsavel = f
             AND dem.status IN (br.udesc.edu.demandatech.model.enumerators.Status.ABERTA,
                                br.udesc.edu.demandatech.model.enumerators.Status.AGUARDANDO)
        WHERE f.perfil = br.udesc.edu.demandatech.model.enumerators.Perfil.FUNCIONARIO
        GROUP BY f.matricula, f.nome
        ORDER BY COUNT(dem) DESC, f.nome
        LIMIT 10
    """)
    List<DezFuncionariosMaisProdutivos> rankingTecnicosPorPendentes();

    @Query("""
        SELECT
            new br.udesc.edu.demandatech.model.dto.relatorio.DezFuncionariosMaisProdutivos(
                f.matricula, f.nome, COUNT(dem)
            )
        FROM Funcionario f
        LEFT JOIN Demanda dem ON dem.criador = f
        WHERE f.departamento.idDepartamento = :idDepartamento
        GROUP BY f.matricula, f.nome
        ORDER BY COUNT(dem) DESC, f.nome
        LIMIT 10
    """)
    List<DezFuncionariosMaisProdutivos> rankingFuncionariosPorAbertas(@Param("idDepartamento") Long idDepartamento);

    @Query("SELECT COUNT(d) FROM Demanda d " + JOINS + " WHERE (:status IS NULL OR d.status = :status) AND " + CONDICAO)
    long contarVisiveis(
        @Param("status") Status status,
        @Param("escopo") String escopo,
        @Param("matricula") Long matricula,
        @Param("idDepartamento") Long idDepartamento
    );

    long countByStatus(Status status);

    @Query("""
        SELECT COUNT(d)
        FROM Demanda d
        JOIN d.criador c
        LEFT JOIN c.departamento dep
        WHERE d.data BETWEEN :inicio AND :fim
          AND (:status IS NULL OR d.status = :status)
          AND (:idDepartamento IS NULL OR dep.idDepartamento = :idDepartamento)
    """)
    long contarRelatorio(
            @Param("status") Status status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("idDepartamento") Long idDepartamento
    );

    @Query("""
        SELECT new br.udesc.edu.demandatech.model.projection.RelatorioDepartamentoProjection(
            dep.idDepartamento, dep.descricao, COUNT(d)
        )
        FROM Demanda d
        JOIN d.criador c
        JOIN c.departamento dep
        WHERE d.data BETWEEN :inicio AND :fim
          AND (:idDepartamento IS NULL OR dep.idDepartamento = :idDepartamento)
        GROUP BY dep.idDepartamento, dep.descricao
        ORDER BY COUNT(d) DESC
    """)
    List<RelatorioDepartamentoProjection> relatorioDemandasPorDepartamento(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("idDepartamento") Long idDepartamento
    );
}
