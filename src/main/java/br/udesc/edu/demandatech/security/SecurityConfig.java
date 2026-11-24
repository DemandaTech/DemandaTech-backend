package br.udesc.edu.demandatech.security;

import br.udesc.edu.demandatech.model.enumerators.Perfil;
import br.udesc.edu.demandatech.repository.FuncionarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final FuncionarioRepository funcionarioRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String admin = Perfil.ADMINISTRADOR.name();
        String gerente = Perfil.GERENTE.name();

        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login", "/logout", "/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publico/departamentos").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cadastros/tipos-demanda", "/cadastros/tipos-demanda/*").authenticated()
                        .requestMatchers("/cadastros/funcionarios", "/cadastros/funcionarios/**").hasAnyAuthority(admin, gerente)
                        .requestMatchers("/visao-geral", "/visao-geral/**").hasAnyAuthority(admin, gerente)
                        .requestMatchers("/cadastros/**", "/relatorios", "/relatorios/**").hasAuthority(admin)
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> RespostaErro.escrever(
                                response, HttpStatus.UNAUTHORIZED.value(), RespostaErro.NAO_AUTENTICADO))
                        .accessDeniedHandler((request, response, e) -> RespostaErro.escrever(
                                response, HttpStatus.FORBIDDEN.value(), RespostaErro.PERFIL_SEM_ACESSO)))
                .addFilterBefore(new JwtFilter(jwtUtil, funcionarioRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origens}") List<String> origens) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origens);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
