package cabenobolso.modelo;


public enum TipoDespesa {
    FIXA("Fixa"),
    POR_PESSOA("Por Pessoa");

    private final String rotulo;

    TipoDespesa(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
