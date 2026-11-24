package br.udesc.edu.demandatech.security;

import br.udesc.edu.demandatech.model.entity.Funcionario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JWTUtil {

    public static final int EXPIRACAO_SEGUNDOS = 1800;
    private static final String EMISSOR = "DemandaTech";

    private final Algorithm algoritmo;
    private final JWTVerifier verificador;

    public JWTUtil(@Value("${jwt.secret}") String segredo) {
        this.algoritmo = Algorithm.HMAC256(segredo);
        this.verificador = JWT.require(algoritmo).withIssuer(EMISSOR).build();
    }

    public String gerarToken(Funcionario funcionario) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(funcionario.getMatricula().toString())
                .withClaim("perfil", funcionario.getPerfil().name())
                .withClaim("idDepartamento", funcionario.idDoDepartamento())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plusSeconds(EXPIRACAO_SEGUNDOS))
                .sign(algoritmo);
    }

    public Long getMatricula(String token) {
        return Long.valueOf(verificador.verify(token).getSubject());
    }
}
