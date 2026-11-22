package cabenobolso.persistencia;

import cabenobolso.modelo.Planejamento;


public interface IPersistencia {
    void salvar(Planejamento planejamento, String caminhoArquivo) throws PersistenciaException;

    Planejamento carregar(String caminhoArquivo) throws PersistenciaException;
}
