package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.model.dto.RegistroDTO;
import br.udesc.edu.demandatech.model.dto.criar.FuncionarioCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.FuncionarioEditarDTO;
import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.exception.*;
import br.udesc.edu.demandatech.model.projection.ColaboradorProjection;
import br.udesc.edu.demandatech.model.projection.FuncionarioCadastroProjection;
import br.udesc.edu.demandatech.repository.DepartamentoRepository;
import br.udesc.edu.demandatech.repository.FuncionarioRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static br.udesc.edu.demandatech.model.enumerators.Perfil.*;

@Service
@AllArgsConstructor
public class FuncionarioService {
    private FuncionarioRepository funcionarioRepository;
    private DepartamentoRepository departamentoRepository;
    private Validator validator;
    private PasswordEncoder passwordEncoder;

    private void validar(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidacaoException(violations.iterator().next().getMessage());
        }
    }

    private void validarEmail(String email) {
        if (!email.contains("@") || !email.contains(".")) {
            throw new ValidacaoException("Campo email está em formato inválido");
        }
    }

    private void validarGestor(Funcionario usuario) {
        if (!usuario.temPerfil(ADMINISTRADOR, GERENTE)) {
            throw new PermissaoNegada("Apenas administradores e gerentes gerenciam funcionários.");
        }
    }

    private void validarMesmoDepartamento(Funcionario usuario, Funcionario alvo) {
        if (usuario.temPerfil(GERENTE) && !Objects.equals(alvo.idDoDepartamento(), usuario.idDoDepartamento())) {
            throw new PermissaoNegada("Gerentes só gerenciam funcionários do próprio departamento.");
        }
    }

    private void validarAlvoDoGerente(Funcionario usuario, Funcionario alvo) {
        validarMesmoDepartamento(usuario, alvo);
        if (usuario.temPerfil(GERENTE) && alvo.temPerfil(ADMINISTRADOR, GERENTE)) {
            throw new PermissaoNegada("Gerentes não podem alterar administradores ou outros gerentes.");
        }
    }

    private Perfil resolverPerfil(Funcionario usuario, Perfil solicitado, Perfil atual) {
        Perfil perfil = solicitado != null ? solicitado : atual;
        if (usuario.temPerfil(GERENTE) && perfil != FUNCIONARIO && perfil != USUARIO) {
            throw new PermissaoNegada("Gerentes só podem atribuir os perfis FUNCIONARIO ou USUARIO.");
        }
        return perfil;
    }

    private Departamento resolverDepartamento(Funcionario usuario, Departamento solicitado) {
        Long idSolicitado = solicitado == null ? null : solicitado.getIdDepartamento();
        if (usuario.temPerfil(GERENTE)) {
            if (idSolicitado != null && !idSolicitado.equals(usuario.idDoDepartamento())) {
                throw new PermissaoNegada("Gerentes só cadastram funcionários no próprio departamento.");
            }
            return usuario.getDepartamento();
        }
        if (idSolicitado == null) throw new ValidacaoException("Campo departamento está vazio");
        return buscarDepartamento(idSolicitado);
    }

    private Departamento buscarDepartamento(Long id) {
        return departamentoRepository.findById(id).orElseThrow(() -> new IdNaoEncontrado("departamento", id));
    }

    @Transactional
    public Funcionario criar(FuncionarioCriarDTO dto, Funcionario usuario) {
        validarGestor(usuario);
        validar(dto);
        validarEmail(dto.email());
        if (funcionarioRepository.existsByEmailIgnoreCase(dto.email().trim())) {
            throw new ValidacaoException("Já existe uma conta com este e-mail");
        }
        Funcionario novo = new Funcionario();
        novo.setNome(dto.nome().trim());
        novo.setEmail(dto.email().trim());
        novo.setSenha(passwordEncoder.encode(dto.senha()));
        novo.setPerfil(resolverPerfil(usuario, dto.perfil(), USUARIO));
        novo.setDepartamento(resolverDepartamento(usuario, dto.departamento()));
        return funcionarioRepository.save(novo);
    }

    @Transactional
    public Funcionario registrar(RegistroDTO dto) {
        validar(dto);
        validarEmail(dto.email());
        if (funcionarioRepository.existsByEmailIgnoreCase(dto.email().trim())) {
            throw new ValidacaoException("Já existe uma conta com este e-mail");
        }
        Funcionario novo = new Funcionario();
        novo.setNome(dto.nome().trim());
        novo.setEmail(dto.email().trim());
        novo.setSenha(passwordEncoder.encode(dto.senha()));
        novo.setPerfil(USUARIO);
        novo.setDepartamento(buscarDepartamento(dto.idDepartamento()));
        return funcionarioRepository.save(novo);
    }

    @Transactional
    public Funcionario atualizar(Long id, FuncionarioEditarDTO dto, Funcionario usuario) {
        validarGestor(usuario);
        validar(dto);
        validarEmail(dto.email());
        Funcionario existente = buscarPorId(id);
        validarAlvoDoGerente(usuario, existente);
        if (funcionarioRepository.existsByEmailIgnoreCaseAndMatriculaNot(dto.email().trim(), id)) {
            throw new ValidacaoException("Já existe uma conta com este e-mail");
        }

        Perfil perfil = resolverPerfil(usuario, dto.perfil(), existente.getPerfil());
        if (existente.getMatricula().equals(usuario.getMatricula()) && perfil != existente.getPerfil()) {
            throw new PermissaoNegada("Você não pode alterar o próprio perfil.");
        }

        existente.setNome(dto.nome().trim());
        existente.setEmail(dto.email().trim());
        existente.setPerfil(perfil);
        if (dto.departamento() != null) {
            existente.setDepartamento(resolverDepartamento(usuario, dto.departamento()));
        }
        if (dto.senha() != null && !dto.senha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(dto.senha()));
        }
        return funcionarioRepository.save(existente);
    }

    public Page<ColaboradorProjection> buscarTudo(Pageable pageable) {
        return funcionarioRepository.listarFuncionarios(pageable);
    }

    public Funcionario buscarPorId(Long id){
        return funcionarioRepository.findById(id)
            .orElseThrow(() -> new IdNaoEncontrado("funcionário", id));
    }

    public Funcionario buscarParaCadastro(Long id, Funcionario usuario) {
        validarGestor(usuario);
        Funcionario funcionario = buscarPorId(id);
        validarMesmoDepartamento(usuario, funcionario);
        return funcionario;
    }

    public void removerPorId(Long id, Funcionario usuario){
        validarGestor(usuario);
        Funcionario alvo = buscarPorId(id);
        if (alvo.getMatricula().equals(usuario.getMatricula())) {
            throw new PermissaoNegada("Você não pode remover a própria conta.");
        }
        validarAlvoDoGerente(usuario, alvo);
        if (departamentoRepository.existsByGerenteMatricula(id)) {
            throw new ReferenciaChaveEstrangeira("Não é possível remover o gerente de um departamento. Troque o gerente do departamento antes.");
        }
        try {
            funcionarioRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferenciaChaveEstrangeira();
        }
    }

    public Page<FuncionarioCadastroProjection> listarCadastros(String search, Perfil perfil, Long idDepartamento,
                                                               Pageable pageable, Funcionario usuario) {
        validarGestor(usuario);
        String termo = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        Long departamento = usuario.temPerfil(GERENTE) ? usuario.idDoDepartamento() : idDepartamento;
        return funcionarioRepository.listarCadastros(termo, perfil, departamento, pageable);
    }
}
