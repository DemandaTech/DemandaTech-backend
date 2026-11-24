package br.udesc.edu.demandatech.model.exception;

public class CookieNaoEncontrado extends RuntimeException {
    public CookieNaoEncontrado() {
        super("Cookie de autenticação não encontrado");
    }
}
