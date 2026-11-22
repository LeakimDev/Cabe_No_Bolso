package cabenobolso.modelo;

import java.io.Serializable;
import java.util.Objects;


public class Participante implements Serializable, Comparable<Participante> {
    private static final long serialVersionUID = 2L;

    private final int codigo;
    private String nome;
    private long orcamento; // em centavos

    public Participante(int codigo, String nome, long orcamento) {
        this.codigo = validarCodigo(codigo);
        this.nome = validarNome(nome);
        this.orcamento = validarOrcamento(orcamento);
    }

    private static int validarCodigo(int codigo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O código deve ser um número positivo.");
        }
        return codigo;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode ser vazio.");
        }
        return nome.trim();
    }

    private static long validarOrcamento(long orcamento) {
        if (orcamento < 0) {
            throw new IllegalArgumentException("O orçamento não pode ser negativo.");
        }
        return orcamento;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = validarNome(nome);
    }


    public long getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(long orcamento) {
        this.orcamento = validarOrcamento(orcamento);
    }


    public void atualizar(String novoNome, long novoOrcamento) {
        String nomeValido = validarNome(novoNome);
        long orcamentoValido = validarOrcamento(novoOrcamento);
        this.nome = nomeValido;
        this.orcamento = orcamentoValido;
    }

    @Override
    public int compareTo(Participante outro) {
        return Integer.compare(this.codigo, outro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return codigo == that.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return nome + " (Cód: " + codigo + ")";
    }
}
