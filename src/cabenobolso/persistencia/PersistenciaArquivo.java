package cabenobolso.persistencia;

import cabenobolso.modelo.Planejamento;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** Persistência por serialização de objetos em arquivo local. */
public class PersistenciaArquivo implements IPersistencia {


    private static final ObjectInputFilter FILTRO =
            ObjectInputFilter.Config.createFilter("cabenobolso.modelo.*;java.util.*;java.lang.*;!*");

    @Override
    public void salvar(Planejamento planejamento, String caminhoArquivo) throws PersistenciaException {
        if (planejamento == null) {
            throw new PersistenciaException("Não há planejamento para salvar.");
        }
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            throw new PersistenciaException("Informe o caminho do arquivo para salvar.");
        }
        Path destino = Paths.get(caminhoArquivo).toAbsolutePath();
        Path temporario = null;
        try {

            temporario = destino.resolveSibling(destino.getFileName() + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                oos.writeObject(planejamento);
            }
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            if (temporario != null) {
                try {
                    Files.deleteIfExists(temporario);
                } catch (IOException ignorado) {
                    // nada a ser fazido: já estamos tratando um erro maior
                }
            }
            throw new PersistenciaException(
                    "Não foi possível salvar o arquivo \"" + destino + "\": " + e.getMessage(), e);
        }
    }

    @Override
    public Planejamento carregar(String caminhoArquivo) throws PersistenciaException {
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            throw new PersistenciaException("Informe o caminho do arquivo para carregar.");
        }
        Path origem = Paths.get(caminhoArquivo).toAbsolutePath();
        if (!Files.isRegularFile(origem)) {
            throw new PersistenciaException("Arquivo não encontrado: " + origem);
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(origem))) {
            ois.setObjectInputFilter(FILTRO);
            Object lido = ois.readObject();
            if (!(lido instanceof Planejamento)) {
                throw new PersistenciaException("O arquivo não contém um planejamento do Cabe no Bolso.");
            }
            return (Planejamento) lido;
        } catch (StreamCorruptedException | EOFException e) {
            throw new PersistenciaException("O arquivo está corrompido ou não é um planejamento válido.", e);
        } catch (InvalidClassException e) {
            throw new PersistenciaException(
                    "O arquivo foi salvo por uma versão incompatível do programa ou contém dados não permitidos.", e);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new PersistenciaException("O arquivo não contém um planejamento válido.", e);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo \"" + origem + "\": " + e.getMessage(), e);
        }
    }
}
