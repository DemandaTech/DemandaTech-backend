package br.udesc.edu.demandatech.repository;

import br.udesc.edu.demandatech.model.entity.TipoDemanda;
import br.udesc.edu.demandatech.model.projection.TipoDemandaCadastroProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDemandaRepository extends JpaRepository<TipoDemanda, Long> {

    @Query(value = """
        SELECT new br.udesc.edu.demandatech.model.projection.TipoDemandaCadastroProjection(
            t.idTipo, t.descricao, d.idDepartamento, d.descricao
        )
        FROM TipoDemanda t
        JOIN t.departamento d
        WHERE LOWER(t.descricao) LIKE :termo OR LOWER(d.descricao) LIKE :termo
           OR CAST(t.idTipo AS String) LIKE :termo
        """,
            countQuery = """
        SELECT COUNT(t)
        FROM TipoDemanda t
        JOIN t.departamento d
        WHERE LOWER(t.descricao) LIKE :termo OR LOWER(d.descricao) LIKE :termo
           OR CAST(t.idTipo AS String) LIKE :termo
        """)
    Page<TipoDemandaCadastroProjection> listarCadastros(@Param("termo") String termo, Pageable pageable);

    boolean existsByDepartamentoIdDepartamento(Long idDepartamento);
}