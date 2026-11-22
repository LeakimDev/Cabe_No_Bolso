package cabenobolso.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 2L;

    private String descricao;
    private long valorCentavos;
    private List<Participante> participantes; // sem repetição, ordenada por código

    protected Despesa(String descricao, long valorCentavos, List<Participante> participantes) {
        setDescricao(descricao);
        setValorCentavos(valorCentavos);
        setParticipantes(participantes);
    }

    public final String getDescricao() {
        return descricao;
    }

    public final void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da despesa não pode ser vazia.");
        }
        this.descricao = descricao.trim();
    }


    public final long getValorCentavos() {
        return valorCentavos;
    }

    public final void setValorCentavos(long valorCentavos) {
        if (valorCentavos <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        this.valorCentavos = valorCentavos;
    }

    public final List<Participante> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public final void setParticipantes(List<Participante> novos) {
        if (novos == null || novos.isEmpty()) {
            throw new IllegalArgumentException("A despesa precisa ter pelo menos um participante.");
        }
        List<Participante> listaUnica = new ArrayList<>();
        for (Participante p : novos) {
            if (p == null) {
                throw new IllegalArgumentException("A lista de participantes contém um item inválido.");
            }
            if (!listaUnica.contains(p)) {
                listaUnica.add(p);
            }
        }
        Collections.sort(listaUnica);
        this.participantes = listaUnica;
    }


    public abstract TipoDespesa getTipo();


    public abstract Map<Participante, Long> calcularRateio();


    public abstract long calcularCustoTotal();
}
