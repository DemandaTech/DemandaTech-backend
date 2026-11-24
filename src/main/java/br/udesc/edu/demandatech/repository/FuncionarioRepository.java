package br.udesc.edu.demandatech.repository;


import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.projection.ColaboradorProjection;
import br.udesc.edu.demandatech.model.projection.FuncionarioCadastroProjection;
import br.udesc.edu.demandatech.model.projection.RelatorioFuncionarioProjection;
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
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query(value = """
        SELECT new br.udesc.edu.demandatech.model.projection.ColaboradorProjection(
            f.matricula, f.nome
        )
        FROM Funcionario f
    """,
            countQuery = """
        SELECT COUNT(f)
        FROM Funcionario f
    """)
    Page<ColaboradorProjection> listarFuncionarios(Pageable pageable);

    @Query(value = """
    SELECT new br.udesc.edu.demandatech.model.projection.FuncionarioCadastroProjection(
        f.matricula, f.nome, f.email, f.perfil, d.idDepartamento, d.descricao
    )
    FROM Funcionario f
    LEFT JOIN f.departamento d
    WHERE (:perfil IS NULL OR f.perfil = :perfil)
      AND (:idDepartamento IS NULL OR d.idDepartamento = :idDepartamento)
      AND (LOWER(f.nome) LIKE :termo OR CAST(f.matricula AS String) LIKE :termo)
    """,
            countQuery = """
    SELECT COUNT(f)
    FROM Funcionario f
    LEFT JOIN f.departamento d
    WHERE (:perfil IS NULL OR f.perfil = :perfil)
      AND (:idDepartamento IS NULL OR d.idDepartamento = :idDepartamento)
      AND (LOWER(f.nome) LIKE :termo OR CAST(f.matricula AS String) LIKE :termo)
    """)
    Page<FuncionarioCadastroProjection> listarCadastros(
            @Param("termo") String termo,
            @Param("perfil") Perfil perfil,
            @Param("idDepartamento") Long idDepartamento,
            Pageable pageable
    );

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDepartamentoIdDepartamento(Long idDepartamento);

    boolean existsByEmailIgnoreCaseAndMatriculaNot(String email, Long matricula);

    @Query("""
        SELECT new br.udesc.edu.demandatech.model.projection.RelatorioFuncionarioProjection(
            f.matricula, f.nome, dep.descricao,
            (SELECT COUNT(d) FROM Demanda d
                WHERE d.responsavel = f
                  AND d.status = br.udesc.edu.demandatech.model.enumerators.Status.RESOLVIDA
                  AND d.data BETWEEN :inicio AND :fim),
            (SELECT COUNT(p) FROM Demanda p
                WHERE p.responsavel = f
                  AND p.status IN (
                      br.udesc.edu.demandatech.model.enumerators.Status.ABERTA,
                      br.udesc.edu.demandatech.model.enumerators.Status.AGUARDANDO)),
            (SELECT COUNT(ed) FROM EstornoDemanda ed
                WHERE ed.demanda.responsavel = f
                  AND ed.data BETWEEN :inicio AND :fim)
        )
        FROM Funcionario f
        JOIN f.departamento dep
        WHERE f.perfil = br.udesc.edu.demandatech.model.enumerators.Perfil.FUNCIONARIO
          AND (:idDepartamento IS NULL OR dep.idDepartamento = :idDepartamento)
    """)
    List<RelatorioFuncionarioProjection> relatorioProdutividade(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("idDepartamento") Long idDepartamento
    );
}


