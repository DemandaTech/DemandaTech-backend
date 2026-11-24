package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.model.acesso.Escopo;
import br.udesc.edu.demandatech.model.dto.criar.EstornoDemandaCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.EstornoDemandaEditarDTO;
import br.udesc.edu.demandatech.model.dto.relatorio.QtdEstornosPorResponsavel;
import br.udesc.edu.demandatech.model.entity.Demanda;
import br.udesc.edu.demandatech.model.entity.EstornoDemanda;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.enumerators.Status;
import br.udesc.edu.demandatech.model.exception.IdNaoEncontrado;
import br.udesc.edu.demandatech.model.exception.PermissaoNegada;
import br.udesc.edu.demandatech.model.exception.ReferenciaChaveEstrangeira;
import br.udesc.edu.demandatech.model.exception.ValidacaoException;
import br.udesc.edu.demandatech.model.projection.EstornoProjection;
import br.udesc.edu.demandatech.repository.DemandaRepository;
import br.udesc.edu.demandatech.repository.EstornoDemandaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class EstornoDemandaService {
    private EstornoDemandaRepository estornoDemandaRepository;
    private DemandaRepository demandaRepository;
    private DemandaService demandaService;
    private Validator validator;

    private void validar(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidacaoException(violations.iterator().next().getMessage());
        }
    }

    private boolean criouADemanda(Demanda demanda, Funcionario usuario) {
        return demanda.getCriador().getMatricula().equals(usuario.getMatricula());
    }

    private EstornoDemanda buscarParaAlterar(Long id, Funcionario usuario) {
        EstornoDemanda estorno = estornoDemandaRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontrado("estorno demanda", id));
        if (!usuario.temPerfil(Perfil.ADMINISTRADOR) && !criouADemanda(estorno.getDemanda(), usuario)) {
            throw new PermissaoNegada("Somente o administrador ou quem criou a demanda pode alterar este estorno.");
        }
        return estorno;
    }

    public EstornoProjection criar(EstornoDemandaCriarDTO dto, Funcionario usuario) {
        validar(dto);
        Demanda demanda = demandaRepository.findById(dto.idDemanda())
                .orElseThrow(() -> new IdNaoEncontrado("demandas", dto.idDemanda()));

        if (!criouADemanda(demanda, usuario)) {
            throw new PermissaoNegada("Somente quem criou a demanda pode registrar um estorno.");
        }
        if (demanda.getStatus() != Status.RESOLVIDA) {
            throw new ValidacaoException("Só é possível registrar estorno de uma demanda resolvida.");
        }

        EstornoDemanda estorno = new EstornoDemanda();
        estorno.setDescricao(dto.descricao().trim());
        estorno.setDemanda(demanda);
        estorno.setData(LocalDate.now());
        estorno.setHora(LocalTime.now());
        estorno.setRegistradoPor(usuario);
        estorno = estornoDemandaRepository.save(estorno);

        return detalhe(estorno.getIdEstorno());
    }

    public EstornoProjection atualizar(Long id, EstornoDemandaEditarDTO dto, Funcionario usuario) {
        validar(dto);
        EstornoDemanda estorno = buscarParaAlterar(id, usuario);
        estorno.setDescricao(dto.descricao().trim());
        estornoDemandaRepository.save(estorno);
        return detalhe(id);
    }

    public Page<EstornoProjection> listar(String search, Long idDemanda, Integer ano, Integer mes, Pageable pageable, Funcionario usuario) {
        String termo = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        LocalDate inicio = LocalDate.of(1900, 1, 1);
        LocalDate fim = LocalDate.of(9999, 12, 31);
        if (ano != null && mes != null) {
            YearMonth periodo = YearMonth.of(ano, mes);
            inicio = periodo.atDay(1);
            fim = periodo.atEndOfMonth();
        }
        if (idDemanda != null) demandaService.validarVisivel(idDemanda, usuario);
        Escopo escopo = Escopo.de(usuario);
        return estornoDemandaRepository.listarEstornos(termo, idDemanda, inicio, fim,
                escopo.tipo(), escopo.matricula(), escopo.idDepartamento(), pageable);
    }

    public EstornoProjection detalhe(Long id) {
        return estornoDemandaRepository.detalheEstorno(id)
                .orElseThrow(() -> new IdNaoEncontrado("estorno demanda", id));
    }

    public EstornoProjection detalheVisivel(Long id, Funcionario usuario) {
        EstornoProjection estorno = detalhe(id);
        demandaService.validarVisivel(estorno.idDemanda(), usuario);
        return estorno;
    }

    public void removerPorId(Long id, Funcionario usuario) {
        buscarParaAlterar(id, usuario);
        try {
            estornoDemandaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferenciaChaveEstrangeira();
        }
    }

    public String relatorioQtdEstornosPorResponsavel(){
        List<QtdEstornosPorResponsavel> relatorio =
                estornoDemandaRepository.relatorioQtdEstornosPorResponsavel();
        String cabecalho = """
        --------------------------------------------------------------------
        *** RELATÓRIO: Quantidade de estornos por responsável da demanda ***
        --------------------------------------------------------------------
        """;
        String conteudo = "";
        for (QtdEstornosPorResponsavel item : relatorio){
            conteudo += "| ID: " + item.getIdResponsavel() + " | Responsável: " + item.getResponsavel() + " | Qtd. Estornos: " + item.getQtdEstornos() + " |\n";
        }
        String footer = "--------------------------------------------------------------------";
        return cabecalho + conteudo + footer;
    }
}
