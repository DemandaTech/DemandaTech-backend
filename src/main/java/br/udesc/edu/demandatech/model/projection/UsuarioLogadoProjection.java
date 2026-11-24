package br.udesc.edu.demandatech.model.projection;

import br.udesc.edu.demandatech.model.entity.Departamento;
import br.udesc.edu.demandatech.model.entity.Funcionario;
import br.udesc.edu.demandatech.model.enumerators.Perfil;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioLogadoProjection(
        Long matricula,
        String nome,
        String email,
        Perfil perfil,
        Long idDepartamento,
        String departamento,
        String token
) {
    public static UsuarioLogadoProjection de(Funcionario f) {
        return de(f, null);
    }

    public static UsuarioLogadoProjection de(Funcionario f, String token) {
        Departamento d = f.getDepartamento();
        return new UsuarioLogadoProjection(
                f.getMatricula(),
                f.getNome(),
                f.getEmail(),
                f.getPerfil(),
                d == null ? null : d.getIdDepartamento(),
                d == null ? null : d.getDescricao(),
                token);
    }
}
