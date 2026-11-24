package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.projection.RelatorioProjection;
import br.udesc.edu.demandatech.service.RelatorioService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping
    public ResponseEntity<RelatorioProjection> gerar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long idDepartamento) {
        return ResponseEntity.ok(relatorioService.gerar(inicio, fim, idDepartamento));
    }
}
