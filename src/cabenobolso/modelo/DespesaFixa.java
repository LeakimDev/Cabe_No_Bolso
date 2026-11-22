package cabenobolso.modelo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 2L;

    public DespesaFixa(String descricao, long valorCentavos, List<Participante> participantes) {
        super(descricao, valorCentavos, participantes);
    }

    @Override
    public TipoDespesa getTipo() {
        return TipoDespesa.FIXA;
    }

    @Override
    public Map<Participante, Long> calcularRateio() {
        Map<Participante, Long> rateio = new TreeMap<>();
        List<Participante> lista = getParticipantes(); // já vem ordenada por código
        int qtd = lista.size();

        long base = getValorCentavos() / qtd;
        long resto = getValorCentavos() % qtd;

        for (int i = 0; i < qtd; i++) {
            rateio.put(lista.get(i), base + (i < resto ? 1 : 0));
        }
        return rateio;
    }

    @Override
    public long calcularCustoTotal() {
        return getValorCentavos();
    }
}
