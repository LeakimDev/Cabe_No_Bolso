package cabenobolso.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Raiz do modelo: guarda participantes e despesas e faz os cálculos e validações do grupo.
 * Dinheiro em centavos.
 */
public class Planejamento implements Serializable {
    private static final long serialVersionUID = 2L;

    private final List<Participante> participantes = new ArrayList<>();
    private final List<Despesa> despesas = new ArrayList<>();

    // Participantes
    public void adicionarParticipante(Participante p) {
        if (p == null) {
            throw new IllegalArgumentException("Participante inválido.");
        }
        if (buscarParticipante(p.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe um participante com o código " + p.getCodigo() + ".");
        }
        participantes.add(p);
    }

    public Participante buscarParticipante(int codigo) {
        for (Participante p : participantes) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    public void removerParticipante(Participante p) {
        if (p == null || !participantes.contains(p)) {
            throw new IllegalArgumentException("Participante não encontrado.");
        }
        for (Despesa d : despesas) {
            if (d.getParticipantes().contains(p)) {
                throw new IllegalStateException(
                        "Não é possível remover o participante enquanto houver despesas vinculadas a ele.");
            }
        }
        participantes.remove(p);
    }

    // Despesas
    public void adicionarDespesa(Despesa d) {
        validarDespesa(d);
        despesas.add(d);
    }

    /**
     * Troca uma despesa por outra na MESMA posição. Se a nova for inválida, nada muda.
     */
    public void substituirDespesa(Despesa antiga, Despesa nova) {
        int indice = despesas.indexOf(antiga);
        if (indice < 0) {
            throw new IllegalArgumentException("Despesa não encontrada.");
        }
        validarDespesa(nova);
        despesas.set(indice, nova);
    }

    public void removerDespesa(Despesa d) {
        if (!despesas.remove(d)) {
            throw new IllegalArgumentException("Despesa não encontrada.");
        }
    }

    /**
     * Toda despesa só pode envolver participantes que estão cadastrados neste planejamento.
     */
    private void validarDespesa(Despesa d) {
        if (d == null) {
            throw new IllegalArgumentException("Despesa inválida.");
        }
        for (Participante p : d.getParticipantes()) {
            if (!participantes.contains(p)) {
                throw new IllegalArgumentException(
                        "O participante " + p.getNome() + " não está cadastrado neste planejamento.");
            }
        }
    }

    //Cálculos (polimórficos: não precisam saber o tipo da despesa, só faz oq tem q fazer)
    public long calcularCustoTotal() {
        long total = 0;
        for (Despesa d : despesas) {
            total += d.calcularCustoTotal();
        }
        return total;
    }

    /**
     * Total de cada participante (quem não participa de nada aparece com 0), ordenado por código.
     */
    public Map<Participante, Long> calcularTotaisIndividuais() {
        Map<Participante, Long> totais = new TreeMap<>();
        for (Participante p : participantes) {
            totais.put(p, 0L);
        }
        for (Despesa d : despesas) {
            for (Map.Entry<Participante, Long> e : d.calcularRateio().entrySet()) {
                totais.merge(e.getKey(), e.getValue(), Long::sum);
            }
        }
        return totais;
    }

    /**
     * Situação de cada participante frente ao orçamento, ordenada por código.
     */
    public List<ResumoParticipante> gerarResumo() {
        List<ResumoParticipante> resumo = new ArrayList<>();
        for (Map.Entry<Participante, Long> e : calcularTotaisIndividuais().entrySet()) {
            resumo.add(new ResumoParticipante(e.getKey(), e.getValue()));
        }
        return resumo;
    }

    // Visões somente leitura: quem está fora não consegue alterar as listas internas.
    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }
}
