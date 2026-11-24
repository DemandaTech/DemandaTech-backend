package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.dto.LoginDTO;
import br.udesc.edu.demandatech.model.dto.RegistroDTO;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.exception.PermissaoLogin;
import br.udesc.edu.demandatech.model.projection.DepartamentoResumoProjection;
import br.udesc.edu.demandatech.model.projection.UsuarioLogadoProjection;
import br.udesc.edu.demandatech.security.CookieUtil;
import br.udesc.edu.demandatech.security.JWTUtil;
import br.udesc.edu.demandatech.service.DepartamentoService;
import br.udesc.edu.demandatech.service.FuncionarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;

    @PostMapping("/login")
    public ResponseEntity<UsuarioLogadoProjection> login(@RequestBody LoginDTO login, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(String.valueOf(login.matricula()), login.senha()));

            Funcionario usuario = (Funcionario) authentication.getPrincipal();
            String token = jwtUtil.gerarToken(usuario);
            response.addCookie(CookieUtil.gerarCookie(token));
            return ResponseEntity.ok(UsuarioLogadoProjection.de(usuario, token));
        } catch (AuthenticationException e) {
            throw new PermissaoLogin();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addCookie(CookieUtil.removerCookie());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioLogadoProjection> usuarioLogado(@AuthenticationPrincipal Funcionario usuario) {
        return ResponseEntity.ok(UsuarioLogadoProjection.de(usuario));
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioLogadoProjection> registrar(@RequestBody RegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioLogadoProjection.de(funcionarioService.registrar(dto)));
    }

    @GetMapping("/publico/departamentos")
    public ResponseEntity<List<DepartamentoResumoProjection>> departamentos() {
        return ResponseEntity.ok(departamentoService.listarResumo());
    }
}
