package cabenobolso.persistencia;

/**
 * Erro ao salvar ou carregar o planejamento. A mensagem já é pensada para ser mostrada ao usuário.
 */
public class PersistenciaException extends Exception {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
