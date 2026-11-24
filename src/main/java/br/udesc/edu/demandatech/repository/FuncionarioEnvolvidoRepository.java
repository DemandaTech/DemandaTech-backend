package br.udesc.edu.demandatech.repository;

import br.udesc.edu.demandatech.model.entity.Demanda;
import br.udesc.edu.demandatech.model.entity.FuncionarioEnvolvido;
import br.udesc.edu.demandatech.model.entity.FuncionarioEnvolvidoId;
import br.udesc.edu.demandatech.model.projection.ColaboradorProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioEnvolvidoRepository extends JpaRepository<FuncionarioEnvolvido, FuncionarioEnvolvidoId> {
    List<FuncionarioEnvolvido> findByDemanda(Demanda demanda);

    @Query("""
        SELECT new br.udesc.edu.demandatech.model.projection.ColaboradorProjection(f.matricula, f.nome)
        FROM FuncionarioEnvolvido fe
        JOIN fe.funcionario f
        WHERE fe.demanda.idDemanda = :idDemanda
        ORDER BY f.nome
    """)
    List<ColaboradorProjection> listarEnvolvidos(@Param("idDemanda") Long idDemanda);
}
