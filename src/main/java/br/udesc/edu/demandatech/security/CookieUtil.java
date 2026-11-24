package br.udesc.edu.demandatech.security;

import br.udesc.edu.demandatech.model.exception.CookieNaoEncontrado;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.WebUtils;

public class CookieUtil {

    public static final String NOME = "JWT";

    public static Cookie gerarCookie(String token) {
        return montar(token, JWTUtil.EXPIRACAO_SEGUNDOS);
    }

    public static Cookie removerCookie() {
        return montar("", 0);
    }

    public static String getToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, NOME);
        if (cookie == null || cookie.getValue().isBlank()) throw new CookieNaoEncontrado();
        return cookie.getValue();
    }

    private static Cookie montar(String valor, int maxAge) {
        Cookie cookie = new Cookie(NOME, valor);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
