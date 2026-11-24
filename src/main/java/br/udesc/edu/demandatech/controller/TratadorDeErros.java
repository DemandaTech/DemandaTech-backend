package br.udesc.edu.demandatech.controller;

import br.udesc.edu.demandatech.model.exception.*;
import br.udesc.edu.demandatech.security.RespostaErro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(PermissaoNegada.class)
    public ResponseEntity<Map<String, Object>> semPermissao(PermissaoNegada e) {
        return erro(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> perfilSemAcesso(AccessDeniedException e) {
        return erro(HttpStatus.FORBIDDEN, RespostaErro.PERFIL_SEM_ACESSO);
    }

    @ExceptionHandler({IdNaoEncontrado.class, ItemNaoEncontrado.class})
    public ResponseEntity<Map<String, Object>> naoEncontrado(RuntimeException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<Map<String, Object>> validacao(ValidacaoException e) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> formatoInvalido(Exception e) {
        return erro(HttpStatus.BAD_REQUEST, "Dados enviados em formato inválido.");
    }

    @ExceptionHandler({ReferenciaChaveEstrangeira.class, SemFuncionarioDisponivel.class})
    public ResponseEntity<Map<String, Object>> conflito(RuntimeException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PermissaoLogin.class)
    public ResponseEntity<Map<String, Object>> loginInvalido(PermissaoLogin e) {
        return erro(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "erro", mensagem));
    }
}
