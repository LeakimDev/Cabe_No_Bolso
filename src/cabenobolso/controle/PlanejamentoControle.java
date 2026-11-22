package cabenobolso.controle;

import cabenobolso.modelo.Despesa;
import cabenobolso.modelo.DespesaFixa;
import cabenobolso.modelo.DespesaPorPessoa;
import cabenobolso.modelo.Dinheiro;
import cabenobolso.modelo.Participante;
import cabenobolso.modelo.Planejamento;
import cabenobolso.modelo.ResumoParticipante;
import cabenobolso.modelo.TipoDespesa;
import cabenobolso.persistencia.IPersistencia;
import cabenobolso.persistencia.PersistenciaException;

import java.util.List;


public class PlanejamentoControle {

    private Planejamento planejamento;
    private final IPersistencia persistencia;


    public PlanejamentoControle(IPersistencia persistencia) {
        this.planejamento = new Planejamento();
        this.persistencia = persistencia;
    }

    // Participantes
    public void adicionarParticipante(String codigo, String nome, String orcamento) {
        int cod = parseCodigo(codigo);
        long orc = Dinheiro.parse(orcamento, "orçamento");
        planejamento.adicionarParticipante(new Participante(cod, nome, orc));
    }

    public void editarParticipante(Participante p, String novoNome, String novoOrcamento) {
        long orc = Dinheiro.parse(novoOrcamento, "orçamento");
        p.atualizar(novoNome, orc); // tudo ou nada
    }

    public void removerParticipante(Participante p) {
        planejamento.removerParticipante(p);
    }

    public List<Participante> getParticipantes() {
        return planejamento.getParticipantes();
    }

    // Despesas
    public void adicionarDespesa(TipoDespesa tipo, String descricao, String valor, List<Participante> participantes) {
        planejamento.adicionarDespesa(criarDespesa(tipo, descricao, valor, participantes));
    }


    public void editarDespesa(Despesa antiga, TipoDespesa tipo, String descricao, String valor,
                              List<Participante> participantes) {
        Despesa nova = criarDespesa(tipo, descricao, valor, participantes);
        planejamento.substituirDespesa(antiga, nova);
    }

    public void removerDespesa(Despesa d) {
        planejamento.removerDespesa(d);
    }

    public List<Despesa> getDespesas() {
        return planejamento.getDespesas();
    }

    private Despesa criarDespesa(TipoDespesa tipo, String descricao, String valor, List<Participante> participantes) {
        if (tipo == null) {
            throw new IllegalArgumentException("Selecione o tipo da despesa.");
        }
        long centavos = Dinheiro.parse(valor, "valor");
        switch (tipo) {
            case FIXA:
                return new DespesaFixa(descricao, centavos, participantes);
            case POR_PESSOA:
                return new DespesaPorPessoa(descricao, centavos, participantes);
            default:
                throw new IllegalArgumentException("Tipo de despesa desconhecido.");
        }
    }

    // Cálculos e resumo (tudo em centavos)
    public long getCustoTotal() {
        return planejamento.calcularCustoTotal();
    }

    public List<ResumoParticipante> getResumo() {
        return planejamento.gerarResumo();
    }

    //Persistência
    public void salvar(String caminhoArquivo) throws PersistenciaException {
        persistencia.salvar(planejamento, caminhoArquivo);
    }


    public void carregar(String caminhoArquivo) throws PersistenciaException {
        Planejamento carregado = persistencia.carregar(caminhoArquivo);
        this.planejamento = carregado;
    }

    public void novoPlanejamento() {
        this.planejamento = new Planejamento();
    }

    private int parseCodigo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo código não pode ficar vazio.");
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O código deve ser um número inteiro positivo.");
        }
    }
}
