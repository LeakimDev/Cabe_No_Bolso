package cabenobolso.modelo;


public class ResumoParticipante {
    private final Participante participante;
    private final long gasto;
    private final long orcamento;

    public ResumoParticipante(Participante participante, long gasto) {
        this.participante = participante;
        this.gasto = gasto;
        this.orcamento = participante.getOrcamento();
    }

    public Participante getParticipante() {
        return participante;
    }

    public long getGasto() {
        return gasto;
    }

    public long getOrcamento() {
        return orcamento;
    }

    public boolean isDentroDoOrcamento() {
        return gasto <= orcamento;
    }


    public long getExcedente() {
        return Math.max(0, gasto - orcamento);
    }
}
