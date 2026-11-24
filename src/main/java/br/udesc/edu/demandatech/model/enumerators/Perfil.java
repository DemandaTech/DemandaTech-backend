package br.udesc.edu.demandatech.model.enumerators;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Perfil {
    ADMINISTRADOR("Administrador"),
    GERENTE("Gerente"),
    FUNCIONARIO("Funcionário"),
    USUARIO("Usuário");

    private final String descricao;
}
