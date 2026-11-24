package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.model.dto.criar.DepartamentoCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.DepartamentoEditarDTO;
import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.exception.*;
import br.udesc.edu.demandatech.model.projection.DepartamentoCadastroProjection;
import br.udesc.edu.demandatech.model.projection.DepartamentoResumoProjection;
import br.udesc.edu.demandatech.repository.DepartamentoRepository;
import br.udesc.edu.demandatech.repository.FuncionarioRepository;
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
public class DepartamentoService {
    private DepartamentoRepository departamentoRepository;
    private FuncionarioRepository funcionarioRepository;
    private TipoDemandaRepository tipoDemandaRepository;
    private Validator validator;

    private void validar(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidacaoException(violations.iterator().next().getMessage());
        }
    }

    public Departamento criar(DepartamentoCriarDTO departamentoCriarDTO, Funcionario funcionario) {
        if(!funcionario.temPerfil(Perfil.ADMINISTRADOR)) {
            throw new PermissaoNegada();
        }
        validar(departamentoCriarDTO);
        Departamento departamento = new Departamento();
        BeanUtils.copyProperties(departamentoCriarDTO, departamento);
        return departamentoRepository.save(departamento);
    }

    public Departamento atualizar(Long id, DepartamentoEditarDTO departamentoEditarDTO, Funcionario funcionario) {
        if(!funcionario.temPerfil(Perfil.ADMINISTRADOR)) {
            throw new PermissaoNegada();
        }
        validar(departamentoEditarDTO);
        Optional<Departamento> opcionalDepartamento = departamentoRepository.findById(id);
        if(opcionalDepartamento.isPresent()) {
            Departamento departamento = opcionalDepartamento.get();
            // Só altera o que veio preenchido; campo ausente mantém o valor atual
            String descricao = departamentoEditarDTO.descricao();
            if (descricao != null && !descricao.isBlank()) {
                departamento.setDescricao(descricao.trim());
            }
            Funcionario gerente = departamentoEditarDTO.gerente();
            if (gerente != null && gerente.getMatricula() != null) {
                departamento.setGerente(funcionarioRepository.findById(gerente.getMatricula())
                        .orElseThrow(() -> new IdNaoEncontrado("funcionário", gerente.getMatricula())));
            }
            return departamentoRepository.save(departamento);
        }
        throw new IdNaoEncontrado("departamento", id);
    }

    public List<DepartamentoResumoProjection> listarResumo() {
        return departamentoRepository.listarResumo();
    }

    public Page<DepartamentoCadastroProjection> listarCadastros(String search, Pageable pageable) {
        String termo = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        return departamentoRepository.listarCadastros(termo, pageable);
    }

    public Departamento buscarPorId(Long id) {
        Optional<Departamento> opcionalDepartamento = departamentoRepository.findById(id);
        if(opcionalDepartamento.isPresent()) {
            return opcionalDepartamento.get();
        }
        throw new IdNaoEncontrado("departamento", id);
    }

    public Departamento buscarPorName(String descricao) {
        Departamento departamento = departamentoRepository.getDepartamentoByDescricao(descricao);
        if(departamento != null) {
            return departamento;
        }
        throw new ItemNaoEncontrado(descricao);
    }

    public void removerPorId(Long id, Funcionario funcionario) {
        if(!funcionario.temPerfil(Perfil.ADMINISTRADOR)) {
            throw new PermissaoNegada();
        }
        Optional<Departamento> optionalDepartamento = departamentoRepository.findById(id);
        if(optionalDepartamento.isPresent()) {
            if (funcionarioRepository.existsByDepartamentoIdDepartamento(id)) {
                throw new ReferenciaChaveEstrangeira("Não é possível remover um departamento que possui funcionários.");
            }
            if (tipoDemandaRepository.existsByDepartamentoIdDepartamento(id)) {
                throw new ReferenciaChaveEstrangeira("Não é possível remover um departamento que possui tipos de demanda.");
            }
            try {
                departamentoRepository.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                throw new ReferenciaChaveEstrangeira();
            }
            return;
        }
        throw new IdNaoEncontrado("departamento", id);
    }
}
