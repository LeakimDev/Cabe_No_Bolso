package cabenobolso.modelo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 2L;

    public DespesaPorPessoa(String descricao, long valorUnitarioCentavos, List<Participante> participantes) {
        super(descricao, valorUnitarioCentavos, participantes);
    }

    @Override
    public TipoDespesa getTipo() {
        return TipoDespesa.POR_PESSOA;
    }

    @Override
    public Map<Participante, Long> calcularRateio() {
        Map<Participante, Long> rateio = new TreeMap<>();
        for (Participante p : getParticipantes()) {
            rateio.put(p, getValorCentavos());
        }
        return rateio;
    }

    @Override
    public long calcularCustoTotal() {
        return Math.multiplyExact(getValorCentavos(), (long) getParticipantes().size());
    }
}
