package br.udesc.edu.demandatech.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class RespostaErro {

    public static final String NAO_AUTENTICADO = "Sessão expirada ou não autenticada. Faça login novamente.";
    public static final String PERFIL_SEM_ACESSO = "Seu perfil não tem acesso a este recurso.";

    private RespostaErro() {
    }

    public static void escrever(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"erro\":\"" + mensagem + "\"}");
    }
}
