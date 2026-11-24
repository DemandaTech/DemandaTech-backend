package br.udesc.edu.demandatech.model.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public record EstornoProjection(
        Long idEstorno,
        String descricao,
        LocalDate data,
        LocalTime hora,
        Long idDemanda,
        String tituloDemanda,
        Long matriculaResponsavel,
        String responsavel,
        Long matriculaCriador,
        String criador,
        Long matriculaRegistradoPor,
        String registradoPor
) {
    @JsonProperty("justificativa")
    public String justificativa() {
        return descricao;
    }
}
