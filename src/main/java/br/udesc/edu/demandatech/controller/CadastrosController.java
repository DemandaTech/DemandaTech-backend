package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.dto.criar.DepartamentoCriarDTO;
import br.udesc.edu.demandatech.model.dto.criar.FuncionarioCriarDTO;
import br.udesc.edu.demandatech.model.dto.criar.TipoDemandaCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.DepartamentoEditarDTO;
import br.udesc.edu.demandatech.model.dto.editar.FuncionarioEditarDTO;
import br.udesc.edu.demandatech.model.dto.editar.TipoDemandaEditarDTO;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.model.projection.DepartamentoCadastroProjection;
import br.udesc.edu.demandatech.model.projection.FuncionarioCadastroProjection;
import br.udesc.edu.demandatech.model.projection.TipoDemandaCadastroProjection;
import br.udesc.edu.demandatech.service.DepartamentoService;
import br.udesc.edu.demandatech.service.FuncionarioService;
import br.udesc.edu.demandatech.service.TipoDemandaService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/cadastros")
public class CadastrosController {

    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;
    private final TipoDemandaService tipoDemandaService;

    @GetMapping("/funcionarios")
    public ResponseEntity<PagedModel<FuncionarioCadastroProjection>> listarFuncionarios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Perfil perfil,
            @RequestParam(required = false) Long idDepartamento,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(new PagedModel<>(
                funcionarioService.listarCadastros(search, perfil, idDepartamento, pageable, usuario)));
    }

    @GetMapping("/funcionarios/{matricula}")
    public ResponseEntity<FuncionarioCadastroProjection> buscarFuncionario(
            @PathVariable Long matricula,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(FuncionarioCadastroProjection.de(funcionarioService.buscarParaCadastro(matricula, usuario)));
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<FuncionarioCadastroProjection> criarFuncionario(
            @RequestBody FuncionarioCriarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(FuncionarioCadastroProjection.de(
                funcionarioService.criar(dto, usuario)));
    }

    @PutMapping("/funcionarios/{matricula}")
    public ResponseEntity<FuncionarioCadastroProjection> atualizarFuncionario(
            @PathVariable Long matricula,
            @RequestBody FuncionarioEditarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(FuncionarioCadastroProjection.de(
                funcionarioService.atualizar(matricula, dto, usuario)));
    }

    @DeleteMapping("/funcionarios/{matricula}")
    public ResponseEntity<Void> removerFuncionario(
            @PathVariable Long matricula,
            @AuthenticationPrincipal Funcionario usuario) {
        funcionarioService.removerPorId(matricula, usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departamentos")
    public ResponseEntity<PagedModel<DepartamentoCadastroProjection>> listarDepartamentos(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "idDepartamento", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(departamentoService.listarCadastros(search, pageable)));
    }

    @GetMapping("/departamentos/{id}")
    public ResponseEntity<DepartamentoCadastroProjection> buscarDepartamento(@PathVariable Long id) {
        return ResponseEntity.ok(DepartamentoCadastroProjection.de(departamentoService.buscarPorId(id)));
    }

    @PostMapping("/departamentos")
    public ResponseEntity<DepartamentoCadastroProjection> criarDepartamento(
            @RequestBody DepartamentoCriarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(DepartamentoCadastroProjection.de(
                departamentoService.criar(dto, usuario)));
    }

    @PutMapping("/departamentos/{id}")
    public ResponseEntity<DepartamentoCadastroProjection> atualizarDepartamento(
            @PathVariable Long id,
            @RequestBody DepartamentoEditarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(DepartamentoCadastroProjection.de(
                departamentoService.atualizar(id, dto, usuario)));
    }

    @DeleteMapping("/departamentos/{id}")
    public ResponseEntity<Void> removerDepartamento(
            @PathVariable Long id,
            @AuthenticationPrincipal Funcionario usuario) {
        departamentoService.removerPorId(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipos-demanda")
    public ResponseEntity<PagedModel<TipoDemandaCadastroProjection>> listarTiposDemanda(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "idTipo", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(tipoDemandaService.listarCadastros(search, pageable)));
    }

    @GetMapping("/tipos-demanda/{id}")
    public ResponseEntity<TipoDemandaCadastroProjection> buscarTipoDemanda(@PathVariable Long id) {
        return ResponseEntity.ok(TipoDemandaCadastroProjection.de(tipoDemandaService.buscarPorId(id)));
    }

    @PostMapping("/tipos-demanda")
    public ResponseEntity<TipoDemandaCadastroProjection> criarTipoDemanda(
            @RequestBody TipoDemandaCriarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(TipoDemandaCadastroProjection.de(
                tipoDemandaService.criar(dto, usuario)));
    }

    @PutMapping("/tipos-demanda/{id}")
    public ResponseEntity<TipoDemandaCadastroProjection> atualizarTipoDemanda(
            @PathVariable Long id,
            @RequestBody TipoDemandaEditarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(TipoDemandaCadastroProjection.de(
                tipoDemandaService.atualizar(id, dto, usuario)));
    }

    @DeleteMapping("/tipos-demanda/{id}")
    public ResponseEntity<Void> removerTipoDemanda(
            @PathVariable Long id,
            @AuthenticationPrincipal Funcionario usuario) {
        tipoDemandaService.removerPorId(id, usuario);
        return ResponseEntity.noContent().build();
    }
}