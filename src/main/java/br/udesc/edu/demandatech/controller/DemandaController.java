package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.dto.criar.DemandaCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.AlterarStatusDTO;
import br.udesc.edu.demandatech.model.dto.editar.DemandaEditarDTO;
import br.udesc.edu.demandatech.model.enumerators.Prioridade;
import br.udesc.edu.demandatech.model.enumerators.Status;
import br.udesc.edu.demandatech.model.projection.ColaboradorProjection;
import br.udesc.edu.demandatech.model.projection.DemandaDetalheProjection;
import br.udesc.edu.demandatech.model.projection.DemandaProjection;
import br.udesc.edu.demandatech.model.projection.ResumoDemandasProjection;
import br.udesc.edu.demandatech.service.DemandaService;
import br.udesc.edu.demandatech.service.FuncionarioService;
import lombok.AllArgsConstructor;

import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@AllArgsConstructor
@RequestMapping("/demanda")
public class DemandaController {

    private final DemandaService demandaService;
    private final FuncionarioService funcionarioService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PagedModel<DemandaProjection>> getAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long idTipo,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = {"data", "idDemanda"}, direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(new PagedModel<>(demandaService.buscarTudo(
                status, prioridade, idTipo, search, pageable, usuario)));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/criar")
    public ResponseEntity<DemandaDetalheProjection> criarDemanda(
            @RequestBody DemandaCriarDTO demandaDTO,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(demandaService.criar(demandaDTO, usuario));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/criar")
    public ResponseEntity<PagedModel<ColaboradorProjection>> listarColabodores (
            @PageableDefault(size = 10, sort = {"nome", "matricula"}, direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(funcionarioService.buscarTudo(pageable)));
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoDemandasProjection> resumo(@AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(demandaService.resumo(usuario));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{idDemanda}")
    public ResponseEntity<DemandaDetalheProjection> detalheDemanda(
            @PathVariable Long idDemanda,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(demandaService.detalheVisivel(idDemanda, usuario));
    }

    @PatchMapping("/{idDemanda}/status")
    public ResponseEntity<DemandaDetalheProjection> alterarStatus(
            @PathVariable Long idDemanda,
            @RequestBody AlterarStatusDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(demandaService.alterarStatus(idDemanda, dto, usuario));
    }

    @PutMapping("/{idDemanda}")
    public ResponseEntity<DemandaDetalheProjection> editarDemanda(
            @PathVariable Long idDemanda,
            @RequestBody DemandaEditarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(demandaService.atualizar(
                idDemanda, dto, usuario));
    }

    @DeleteMapping("/{idDemanda}")
    public ResponseEntity<Void> excluirDemanda(
            @PathVariable Long idDemanda,
            @AuthenticationPrincipal Funcionario usuario) {
        demandaService.removerPorId(usuario, idDemanda);
        return ResponseEntity.noContent().build();
    }
}


