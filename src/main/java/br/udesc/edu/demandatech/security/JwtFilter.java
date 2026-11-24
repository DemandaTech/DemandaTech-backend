package br.udesc.edu.demandatech.security;

import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.exception.CookieNaoEncontrado;
import br.udesc.edu.demandatech.repository.FuncionarioRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final Set<String> ROTAS_PUBLICAS =
            Set.of("/login", "/logout", "/registrar", "/publico/departamentos", "/error");
    private static final String BEARER = "Bearer ";

    private final JWTUtil jwtUtil;
    private final FuncionarioRepository funcionarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (rotaPrivada(request)) {
            try {
                Funcionario usuario = funcionarioRepository.findById(jwtUtil.getMatricula(lerToken(request)))
                        .orElseThrow(() -> new JWTVerificationException("Usuário do token não existe"));

                String novoToken = jwtUtil.gerarToken(usuario);
                response.addCookie(CookieUtil.gerarCookie(novoToken));
                response.setHeader(HttpHeaders.AUTHORIZATION, BEARER + novoToken);

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
            } catch (JWTVerificationException | CookieNaoEncontrado | NumberFormatException e) {
                RespostaErro.escrever(response, HttpServletResponse.SC_UNAUTHORIZED, RespostaErro.NAO_AUTENTICADO);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String lerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) return header.substring(BEARER.length());
        return CookieUtil.getToken(request);
    }

    private boolean rotaPrivada(HttpServletRequest request) {
        return !HttpMethod.OPTIONS.matches(request.getMethod())
                && !ROTAS_PUBLICAS.contains(request.getRequestURI());
    }
}
