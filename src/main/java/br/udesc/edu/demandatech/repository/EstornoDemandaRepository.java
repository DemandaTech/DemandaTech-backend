package br.udesc.edu.demandatech.repository;

import static br.udesc.edu.demandatech.model.acesso.FiltroVisibilidade.CONDICAO;
import static br.udesc.edu.demandatech.model.acesso.FiltroVisibilidade.JOINS;

import br.udesc.edu.demandatech.model.dto.relatorio.QtdEstornosPorResponsavel;
import br.udesc.edu.demandatech.model.entity.Demanda;
import br.udesc.edu.demandatech.model.entity.EstornoDemanda;
import br.udesc.edu.demandatech.model.projection.EstornoProjection;
import br.udesc.edu.demandatech.model.projection.RelatorioEstornoResponsavelProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstornoDemandaRepository extends JpaRepository<EstornoDemanda, Long> {

    List<EstornoDemanda> findByDemanda(Demanda demanda);

    @Query(value = """
        SELECT new br.udesc.edu.demandatech.model.projection.EstornoProjection(
            ed.idEstorno, ed.descricao, ed.data, ed.hora,
            d.idDemanda, d.titulo, vr.matricula, vr.nome, vc.matricula, vc.nome, rp.matricula, rp.nome
        )
        FROM EstornoDemanda ed
        JOIN ed.demanda d
        LEFT JOIN ed.registradoPor rp
        """ + JOINS + """
        WHERE ed.data BETWEEN :inicio AND :fim
          AND (:idDemanda IS NULL OR d.idDemanda = :idDemanda)
          AND (CAST(ed.idEstorno AS String) LIKE :termo
               OR CAST(d.idDemanda AS String) LIKE :termo
               OR LOWER(d.titulo) LIKE :termo)
          AND """ + CONDICAO,
        countQuery = """
        SELECT COUNT(ed)
        FROM EstornoDemanda ed
        JOIN ed.demanda d
        """ + JOINS + """
        WHERE ed.data BETWEEN :inicio AND :fim
          AND (:idDemanda IS NULL OR d.idDemanda = :idDemanda)
          AND (CAST(ed.idEstorno AS String) LIKE :termo
               OR CAST(d.idDemanda AS String) LIKE :termo
               OR LOWER(d.titulo) LIKE :termo)
          AND """ + CONDICAO)
    Page<EstornoProjection> listarEstornos(
            @Param("termo") String termo,
            @Param("idDemanda") Long idDemanda,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("escopo") String escopo,
            @Param("matricula") Long matricula,
            @Param("idDepartamento") Long idDepartamento,
            Pageable pageable
    );

    @Query("""
        SELECT new br.udesc.edu.demandatech.model.projection.EstornoProjection(
            ed.idEstorno, ed.descricao, ed.data, ed.hora,
            d.idDemanda, d.titulo, r.matricula, r.nome, c.matricula, c.nome, rp.matricula, rp.nome
        )
        FROM EstornoDemanda ed
        JOIN ed.demanda d
        JOIN d.criador c
        LEFT JOIN d.responsavel r
        LEFT JOIN ed.registradoPor rp
        WHERE ed.idEstorno = :id
        """)
    Optional<EstornoProjection> detalheEstorno(@Param("id") Long id);

    @Query("""
        SELECT
            new br.udesc.edu.demandatech.model.dto.relatorio.QtdEstornosPorResponsavel(
                dem.responsavel.matricula, dem.responsavel.nome, COUNT(ed)
            )
        FROM EstornoDemanda ed
        JOIN ed.demanda dem
        GROUP BY dem.responsavel.matricula, dem.responsavel.nome
        ORDER BY COUNT(ed) DESC
    """)
    List<QtdEstornosPorResponsavel> relatorioQtdEstornosPorResponsavel();

    @Query("""
        SELECT COUNT(ed)
        FROM EstornoDemanda ed
        JOIN ed.demanda d
        LEFT JOIN d.responsavel r
        LEFT JOIN r.departamento dep
        WHERE ed.data BETWEEN :inicio AND :fim
          AND (:idDepartamento IS NULL OR dep.idDepartamento = :idDepartamento)
    """)
    long contarRelatorio(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("idDepartamento") Long idDepartamento
    );

    @Query("""
        SELECT new br.udesc.edu.demandatech.model.projection.RelatorioEstornoResponsavelProjection(
            r.matricula, r.nome, COUNT(ed)
        )
        FROM EstornoDemanda ed
        JOIN ed.demanda d
        JOIN d.responsavel r
        JOIN r.departamento dep
        WHERE ed.data BETWEEN :inicio AND :fim
          AND (:idDepartamento IS NULL OR dep.idDepartamento = :idDepartamento)
        GROUP BY r.matricula, r.nome
        ORDER BY COUNT(ed) DESC, r.nome
    """)
    List<RelatorioEstornoResponsavelProjection> relatorioEstornosPorResponsavel(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("idDepartamento") Long idDepartamento
    );
}
