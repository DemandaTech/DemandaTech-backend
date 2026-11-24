package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.model.dto.criar.TipoDemandaCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.TipoDemandaEditarDTO;
import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.entity.TipoDemanda;
import br.udesc.edu.demandatech.model.exception.IdNaoEncontrado;
import br.udesc.edu.demandatech.model.exception.PermissaoNegada;
import br.udesc.edu.demandatech.model.exception.ReferenciaChaveEstrangeira;
import br.udesc.edu.demandatech.model.exception.ValidacaoException;
import br.udesc.edu.demandatech.model.projection.TipoDemandaCadastroProjection;
import br.udesc.edu.demandatech.repository.DepartamentoRepository;
import br.udesc.edu.demandatech.repository.TipoDemandaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class TipoDemandaService {
    private TipoDemandaRepository tipoDemandaRepository;
    private DepartamentoRepository departamentoRepository;
    private Validator validator;

    private void validar(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidacaoException(violations.iterator().next().getMessage());
        }
    }

    public TipoDemanda criar(TipoDemandaCriarDTO tipoDemandaCriarDTO, Funcionario funcionario) {
        if(!funcionario.temPerfil(Perfil.ADMINISTRADOR)) { throw new PermissaoNegada(); }
        validar(tipoDemandaCriarDTO);
        TipoDemanda tipoDemanda = new TipoDemanda();
        BeanUtils.copyProperties(tipoDemandaCriarDTO, tipoDemanda);
        return tipoDemandaRepository.save(tipoDemanda);
    }

    public TipoDemanda atualizar(Long id, TipoDemandaEditarDTO tipoDemandaEditarDTO, Funcionario funcionario) {
        if(!funcionario.temPerfil(Perfil.ADMINISTRADOR)) { throw new PermissaoNegada(); }
        validar(tipoDemandaEditarDTO);
        Optional<TipoDemanda> opt = tipoDemandaRepository.findById(id);
        if(opt.isPresent()) {
            TipoDemanda t = opt.get();
            // Só altera o que veio preenchido; campo ausente mantém o valor atual
            String descricao = tipoDemandaEditarDTO.descricao();
            if (descricao != null && !descricao.isBlank()) {
                t.setDescricao(descricao.trim());
            }
            Departamento departamento = tipoDemandaEditarDTO.departamento();
            if (departamento != null && departamento.getIdDepartamento() != null) {
                t.setDepartamento(departamentoRepository.findById(departamento.getIdDepartamento())
                        .orElseThrow(() -> new IdNaoEncontrado("departamento", departamento.getIdDepartamento())));
            }
            return tipoDemandaRepository.save(t);
        }
        throw new IdNaoEncontrado("tipo demanda", id);
    }

    public Page<TipoDemandaCadastroProjection> listarCadastros(String search, Pageable pageable) {
        String termo = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        return tipoDemandaRepository.listarCadastros(termo, pageable);
    }

    public TipoDemanda buscarPorId(Long id) {
        Optional<TipoDemanda> opt = tipoDemandaRepository.findById(id);
        if(opt.isPresent()) { return opt.get(); }
        throw new IdNaoEncontrado("tipo demanda", id);
    }

    public void removerPorId(Long id, Funcionario funcionario){
        if(!funcionario.temPerfil(Perfil.ADMINISTRADOR)) {
            throw new PermissaoNegada();
        }
        Optional<TipoDemanda> opcionalTipoDemanda = tipoDemandaRepository.findById(id);
        if(opcionalTipoDemanda.isPresent()) {
            try {
                tipoDemandaRepository.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                throw new ReferenciaChaveEstrangeira();
            }
            return;
        }
        throw new IdNaoEncontrado("tipo demanda", id);
    }
}
