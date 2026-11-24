package br.udesc.edu.demandatech.repository;

import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.projection.DepartamentoCadastroProjection;
import br.udesc.edu.demandatech.model.projection.DepartamentoResumoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    Departamento getDepartamentoByDescricao(String descricao);

    @Query("""
        SELECT new br.udesc.edu.demandatech.model.projection.DepartamentoResumoProjection(d.idDepartamento, d.descricao)
        FROM Departamento d
        ORDER BY d.descricao
        """)
    List<DepartamentoResumoProjection> listarResumo();

    @Query(value = """
        SELECT new br.udesc.edu.demandatech.model.projection.DepartamentoCadastroProjection(
            d.idDepartamento, d.descricao, g.matricula, g.nome
        )
        FROM Departamento d
        LEFT JOIN d.gerente g
        WHERE LOWER(d.descricao) LIKE :termo OR CAST(d.idDepartamento AS String) LIKE :termo
        """,
            countQuery = """
        SELECT COUNT(d)
        FROM Departamento d
        WHERE LOWER(d.descricao) LIKE :termo OR CAST(d.idDepartamento AS String) LIKE :termo
        """)
    Page<DepartamentoCadastroProjection> listarCadastros(@Param("termo") String termo, Pageable pageable);

    boolean existsByGerenteMatricula(Long matricula);
}
