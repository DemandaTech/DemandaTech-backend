package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.model.enumerators.Status;
import br.udesc.edu.demandatech.model.exception.ValidacaoException;
import br.udesc.edu.demandatech.model.projection.RelatorioDepartamentoProjection;
import br.udesc.edu.demandatech.model.projection.RelatorioFuncionarioProjection;
import br.udesc.edu.demandatech.model.projection.RelatorioProjection;
import br.udesc.edu.demandatech.repository.DemandaRepository;
import br.udesc.edu.demandatech.repository.EstornoDemandaRepository;
import br.udesc.edu.demandatech.repository.FuncionarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class RelatorioService {

    private static final LocalDate SEM_INICIO = LocalDate.of(1900, 1, 1);
    private static final LocalDate SEM_FIM = LocalDate.of(9999, 12, 31);

    private DemandaRepository demandaRepository;
    private EstornoDemandaRepository estornoDemandaRepository;
    private FuncionarioRepository funcionarioRepository;

    @Transactional(readOnly = true)
    public RelatorioProjection gerar(LocalDate inicio, LocalDate fim, Long idDepartamento) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new ValidacaoException("Data inicial maior que a data final");
        }
        LocalDate de = inicio == null ? SEM_INICIO : inicio;
        LocalDate ate = fim == null ? SEM_FIM : fim;

        long total = demandaRepository.contarRelatorio(null, de, ate, idDepartamento);
        long concluidas = demandaRepository.contarRelatorio(Status.RESOLVIDA, de, ate, idDepartamento);
        long emAndamento = demandaRepository.contarRelatorio(Status.ABERTA, de, ate, idDepartamento)
                + demandaRepository.contarRelatorio(Status.AGUARDANDO, de, ate, idDepartamento);
        long estornos = estornoDemandaRepository.contarRelatorio(de, ate, idDepartamento);

        List<RelatorioDepartamentoProjection> porDepartamento = demandaRepository
                .relatorioDemandasPorDepartamento(de, ate, idDepartamento)
                .stream()
                .map(d -> d.comPercentual(total))
                .toList();

        List<RelatorioFuncionarioProjection> ranking = funcionarioRepository
                .relatorioProdutividade(de, ate, idDepartamento)
                .stream()
                .filter(f -> f.concluidas() > 0)
                .sorted(Comparator.comparing(RelatorioFuncionarioProjection::concluidas).reversed()
                        .thenComparing(RelatorioFuncionarioProjection::estornos)
                        .thenComparing(RelatorioFuncionarioProjection::nome))
                .limit(10)
                .toList();
        List<RelatorioFuncionarioProjection> maisProdutivos = IntStream.range(0, ranking.size())
                .mapToObj(i -> ranking.get(i).comPosicao(i + 1))
                .toList();

        return new RelatorioProjection(
                LocalDateTime.now(),
                inicio,
                fim,
                idDepartamento,
                total,
                concluidas,
                estornos,
                emAndamento,
                porDepartamento,
                estornoDemandaRepository.relatorioEstornosPorResponsavel(de, ate, idDepartamento),
                maisProdutivos
        );
    }
}
