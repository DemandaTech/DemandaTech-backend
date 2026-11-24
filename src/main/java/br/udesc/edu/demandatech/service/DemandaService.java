package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.model.acesso.Escopo;
import br.udesc.edu.demandatech.model.dto.criar.DemandaCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.AlterarStatusDTO;
import br.udesc.edu.demandatech.model.dto.editar.DemandaEditarDTO;
import br.udesc.edu.demandatech.model.dto.relatorio.DezFuncionariosMaisProdutivos;
import br.udesc.edu.demandatech.model.dto.relatorio.QtdDemandasPorDepartamento;
import br.udesc.edu.demandatech.model.entity.Demanda;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.entity.FuncionarioEnvolvido;
import br.udesc.edu.demandatech.model.entity.TipoDemanda;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.enumerators.Prioridade;
import br.udesc.edu.demandatech.model.enumerators.Status;
import br.udesc.edu.demandatech.model.exception.IdNaoEncontrado;
import br.udesc.edu.demandatech.model.exception.PermissaoNegada;
import br.udesc.edu.demandatech.model.exception.SemFuncionarioDisponivel;
import br.udesc.edu.demandatech.model.exception.ValidacaoException;
import br.udesc.edu.demandatech.model.projection.DemandaDetalheProjection;
import br.udesc.edu.demandatech.model.projection.DemandaProjection;
import br.udesc.edu.demandatech.model.projection.ResumoDemandasProjection;
import br.udesc.edu.demandatech.model.projection.VisaoGeralProjection;
import br.udesc.edu.demandatech.repository.DemandaRepository;
import br.udesc.edu.demandatech.repository.EstornoDemandaRepository;
import br.udesc.edu.demandatech.repository.FuncionarioEnvolvidoRepository;
import br.udesc.edu.demandatech.repository.FuncionarioRepository;
import br.udesc.edu.demandatech.repository.TipoDemandaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class DemandaService {
    private DemandaRepository demandaRepository;
    private FuncionarioEnvolvidoRepository funcionarioEnvolvidoRepository;
    private EstornoDemandaRepository estornoDemandaRepository;
    private TipoDemandaRepository tipoDemandaRepository;
    private FuncionarioRepository funcionarioRepository;
    private Validator validator;

    private void validar(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidacaoException(violations.iterator().next().getMessage());
        }
    }

    private TipoDemanda buscarTipo(Long idTipo) {
        if (idTipo == null) throw new ValidacaoException("Campo tipo está vazio");
        return tipoDemandaRepository.findById(idTipo)
                .orElseThrow(() -> new IdNaoEncontrado("tipos_de_demanda", idTipo));
    }

    private void validarCriador(Demanda demanda, Funcionario usuario, String acao) {
        if (!demanda.getCriador().getMatricula().equals(usuario.getMatricula())) {
            throw new PermissaoNegada("Somente quem criou a demanda pode " + acao + ".");
        }
    }

    public void validarVisivel(Long idDemanda, Funcionario usuario) {
        if (!demandaRepository.existsById(idDemanda)) throw new IdNaoEncontrado("demandas", idDemanda);
        Escopo escopo = Escopo.de(usuario);
        if (!demandaRepository.visivel(idDemanda, escopo.tipo(), escopo.matricula(), escopo.idDepartamento())) {
            throw new PermissaoNegada("Você não tem acesso a esta demanda.");
        }
    }

    @Transactional
    public DemandaDetalheProjection criar(DemandaCriarDTO dto, Funcionario criador){
        validar(dto);
        TipoDemanda tipo = buscarTipo(dto.tipo().getIdTipo());

        Funcionario responsavel = demandaRepository.findFuncionarioComMenosDemandas(
                tipo.getDepartamento().getIdDepartamento(), criador.getMatricula()
        ).orElseThrow(SemFuncionarioDisponivel::new);

        Demanda demanda = new Demanda();
        demanda.setTitulo(dto.titulo().trim());
        demanda.setDescricao(dto.descricao() == null ? null : dto.descricao().trim());
        demanda.setPrioridade(dto.prioridade());
        demanda.setTipo(tipo);
        demanda.setData(LocalDate.now());
        demanda.setHora(LocalTime.now());
        demanda.setCriador(criador);
        demanda.setResponsavel(responsavel);
        demanda.setStatus(Status.ABERTA);
        demanda = demandaRepository.save(demanda);

        Set<Long> envolvidos = new LinkedHashSet<>();
        for (Funcionario funcionario : dto.usuariosEnvolvidos()) {
            if (funcionario != null && funcionario.getMatricula() != null
                    && !funcionario.getMatricula().equals(criador.getMatricula())) {
                envolvidos.add(funcionario.getMatricula());
            }
        }
        for (Long matricula : envolvidos) {
            Funcionario envolvido = funcionarioRepository.findById(matricula)
                    .orElseThrow(() -> new IdNaoEncontrado("funcionário", matricula));
            funcionarioEnvolvidoRepository.save(new FuncionarioEnvolvido(envolvido, demanda));
        }

        return detalheDemanda(demanda.getIdDemanda());
    }

    @Transactional
    public DemandaDetalheProjection atualizar(Long id, DemandaEditarDTO dto, Funcionario usuario){
        validar(dto);
        Demanda demanda = buscarPorId(id);
        validarCriador(demanda, usuario, "editá-la");

        if (dto.titulo() != null && !dto.titulo().isBlank()) demanda.setTitulo(dto.titulo().trim());
        if (dto.descricao() != null) demanda.setDescricao(dto.descricao().trim());
        if (dto.prioridade() != null) demanda.setPrioridade(dto.prioridade());
        if (dto.idTipo() != null) demanda.setTipo(buscarTipo(dto.idTipo()));

        demandaRepository.save(demanda);
        return detalheDemanda(id);
    }

    @Transactional
    public DemandaDetalheProjection alterarStatus(Long id, AlterarStatusDTO dto, Funcionario usuario) {
        validar(dto);
        Demanda demanda = buscarPorId(id);
        if (demanda.getResponsavel() == null
                || !demanda.getResponsavel().getMatricula().equals(usuario.getMatricula())) {
            throw new PermissaoNegada("Somente o responsável pela demanda pode alterar o status.");
        }

        Status atual = demanda.getStatus();
        Status novo = dto.status();
        if (atual.finalizada()) {
            throw new ValidacaoException("A demanda já está " + atual.getDescricao().toLowerCase() + " e não pode mudar de status.");
        }
        if (!atual.podeAvancarPara(novo)) {
            throw new ValidacaoException("O status não pode voltar de " + atual.getDescricao() + " para " + novo.getDescricao() + ".");
        }

        boolean temJustificativa = dto.justificativa() != null && !dto.justificativa().isBlank();
        if (temJustificativa && novo != Status.RESOLVIDA) {
            throw new ValidacaoException("A justificativa só pode ser enviada ao marcar a demanda como resolvida.");
        }

        demanda.setStatus(novo);
        demanda.setJustificativa(temJustificativa ? dto.justificativa().trim() : null);
        demandaRepository.save(demanda);
        return detalheDemanda(id);
    }

    public Page<DemandaProjection> buscarTudo(Status status, Prioridade prioridade, Long idTipo, String search,
                                              Pageable pageable, Funcionario usuario) {
        String termo = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        Escopo escopo = Escopo.de(usuario);
        return demandaRepository.listarDemandas(status, prioridade, idTipo, termo,
                escopo.tipo(), escopo.matricula(), escopo.idDepartamento(), pageable);
    }

    public Demanda buscarPorId(Long id){
        return demandaRepository.findById(id).orElseThrow(() -> new IdNaoEncontrado("demandas", id));
    }

    @Transactional
    public void removerPorId(Funcionario usuario, Long id){
        Demanda demanda = buscarPorId(id);
        validarCriador(demanda, usuario, "excluí-la");
        estornoDemandaRepository.deleteAll(estornoDemandaRepository.findByDemanda(demanda));
        funcionarioEnvolvidoRepository.deleteAll(funcionarioEnvolvidoRepository.findByDemanda(demanda));
        demandaRepository.delete(demanda);
    }

    public String relatorioQtdDemandasPorDepartamento(){
        List<QtdDemandasPorDepartamento> relatorio =
                demandaRepository.relatorioQtdDemandasPorDepartamento();
        String cabecalho = """
        --------------------------------------------------------------------
        *** RELATÓRIO: Quantidade de demandas ativas por departamento ***
        --------------------------------------------------------------------
        """;
        String conteudo = "";
        for (QtdDemandasPorDepartamento item : relatorio){
            conteudo += "| ID: " + item.getIdDepartamento() + " | Departamento: " + item.getDepartamento() + " | Qtd. Demandas: " + item.getQtdDemanda() + " |\n";
        }
        String footer = "--------------------------------------------------------------------";
        return cabecalho + conteudo + footer;
    }

    public String relatorioDezFuncionariosMaisProdutivos(){
        List<DezFuncionariosMaisProdutivos> relatorio =
                demandaRepository.relatorioDezFuncionariosMaisProdutivos();
        String cabecalho = """
        --------------------------------------------------------------------
        *** RELATÓRIO: Top 10 funcionários mais produtivos ***
        --------------------------------------------------------------------
        """;
        String conteudo = "";
        for (DezFuncionariosMaisProdutivos item : relatorio){
            conteudo += "| Matrícula: " + item.getMatricula() + " | Nome: " + item.getNome() + " | Qtd. Demandas: " + item.getQtdDemanda() + " |\n";
        }
        String footer = "--------------------------------------------------------------------";
        return cabecalho + conteudo + footer;
    }

    @Transactional(readOnly = true)
    public VisaoGeralProjection visaoGeral(Funcionario usuario) {
        if (!usuario.temPerfil(Perfil.ADMINISTRADOR, Perfil.GERENTE)) {
            throw new PermissaoNegada("A visão geral é exclusiva de administradores e gerentes.");
        }
        Long idDepartamento = usuario.temPerfil(Perfil.GERENTE) ? usuario.idDoDepartamento() : null;

        long total = demandaRepository.contarVisaoGeral(null, idDepartamento);
        long resolvidas = demandaRepository.contarVisaoGeral(Status.RESOLVIDA, idDepartamento);
        double taxaConclusao = total == 0 ? 0 : Math.round(resolvidas * 1000.0 / total) / 10.0;

        List<DemandaProjection> chamadosRecentes = buscarTudo(null, null, null, null,
                PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "data", "idDemanda")), usuario).getContent();

        return new VisaoGeralProjection(
                total,
                demandaRepository.contarVisaoGeral(Status.ABERTA, idDepartamento),
                demandaRepository.contarVisaoGeral(Status.AGUARDANDO, idDepartamento),
                taxaConclusao,
                chamadosRecentes,
                idDepartamento == null
                        ? demandaRepository.rankingTecnicosPorPendentes()
                        : demandaRepository.rankingFuncionariosPorAbertas(idDepartamento)
        );
    }

    @Transactional(readOnly = true)
    public ResumoDemandasProjection resumo(Funcionario usuario) {
        Escopo escopo = Escopo.de(usuario);
        return new ResumoDemandasProjection(
                demandaRepository.contarVisiveis(null, escopo.tipo(), escopo.matricula(), escopo.idDepartamento()),
                demandaRepository.contarVisiveis(Status.ABERTA, escopo.tipo(), escopo.matricula(), escopo.idDepartamento()),
                demandaRepository.contarVisiveis(Status.AGUARDANDO, escopo.tipo(), escopo.matricula(), escopo.idDepartamento()),
                demandaRepository.contarVisiveis(Status.RESOLVIDA, escopo.tipo(), escopo.matricula(), escopo.idDepartamento())
        );
    }

    @Transactional(readOnly = true)
    public DemandaDetalheProjection detalheDemanda(Long id) {
        DemandaDetalheProjection detalhe = demandaRepository.detalheDemanda(id)
                .orElseThrow(() -> new IdNaoEncontrado("demandas", id));
        return detalhe.comFuncionariosEnvolvidos(funcionarioEnvolvidoRepository.listarEnvolvidos(id));
    }

    @Transactional(readOnly = true)
    public DemandaDetalheProjection detalheVisivel(Long id, Funcionario usuario) {
        validarVisivel(id, usuario);
        return detalheDemanda(id);
    }
}
