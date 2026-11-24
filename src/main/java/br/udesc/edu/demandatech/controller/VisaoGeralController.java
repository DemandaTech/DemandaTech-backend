package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.projection.VisaoGeralProjection;
import br.udesc.edu.demandatech.service.DemandaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/visao-geral")
public class VisaoGeralController {

    private final DemandaService demandaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<VisaoGeralProjection> visaoGeral(@AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(demandaService.visaoGeral(usuario));
    }
}
