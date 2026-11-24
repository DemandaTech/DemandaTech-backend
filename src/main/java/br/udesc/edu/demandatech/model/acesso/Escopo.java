package br.udesc.edu.demandatech.model.acesso;

import br.udesc.edu.demandatech.model.entity.Funcionario;

public record Escopo(String tipo, Long matricula, Long idDepartamento) {

    public static Escopo de(Funcionario usuario) {
        String tipo = switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> "TODOS";
            case GERENTE -> "DEPARTAMENTO";
            case FUNCIONARIO -> "TECNICO";
            case USUARIO -> "USUARIO";
        };
        return new Escopo(tipo, usuario.getMatricula(), usuario.idDoDepartamento());
    }
}
