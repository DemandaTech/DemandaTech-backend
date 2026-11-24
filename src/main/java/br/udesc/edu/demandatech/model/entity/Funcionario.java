package br.udesc.edu.demandatech.model.entity;

import br.udesc.edu.demandatech.model.enumerators.Perfil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = "departamento")
@Table(name = "funcionarios", schema = "demanda_tech")
@JsonIgnoreProperties({"username", "password", "authorities",
        "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"})
public class Funcionario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funcionarios_seq")
    @SequenceGenerator(name = "funcionarios_seq", sequenceName = "demanda_tech.funcionarios_matricula_seq", allocationSize = 1)
    private Long matricula;

    private String nome;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(perfil.name()));
    }

    public boolean temPerfil(Perfil... perfis) {
        return perfil != null && Arrays.asList(perfis).contains(perfil);
    }

    public Long idDoDepartamento() {
        return departamento == null ? null : departamento.getIdDepartamento();
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return String.valueOf(matricula);
    }
}
