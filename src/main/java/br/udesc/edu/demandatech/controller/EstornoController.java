package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.dto.criar.EstornoDemandaCriarDTO;
import br.udesc.edu.demandatech.model.dto.editar.EstornoDemandaEditarDTO;
import br.udesc.edu.demandatech.model.projection.EstornoProjection;
import br.udesc.edu.demandatech.service.EstornoDemandaService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/estornos")
public class EstornoController {

    private final EstornoDemandaService estornoDemandaService;

    @GetMapping
    public ResponseEntity<PagedModel<EstornoProjection>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long idDemanda,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            @PageableDefault(size = 10, sort = {"data", "hora"}, direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(new PagedModel<>(estornoDemandaService.listar(search, idDemanda, ano, mes, pageable, usuario)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstornoProjection> detalhe(
            @PathVariable Long id,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(estornoDemandaService.detalheVisivel(id, usuario));
    }

    @PostMapping
    public ResponseEntity<EstornoProjection> criar(
            @RequestBody EstornoDemandaCriarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(estornoDemandaService.criar(
                dto, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstornoProjection> atualizar(
            @PathVariable Long id,
            @RequestBody EstornoDemandaEditarDTO dto,
            @AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(estornoDemandaService.atualizar(
                id, dto, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable Long id,
            @AuthenticationPrincipal Funcionario usuario) {
        estornoDemandaService.removerPorId(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
