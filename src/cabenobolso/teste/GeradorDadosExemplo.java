package cabenobolso.teste;

import cabenobolso.controle.PlanejamentoControle;
import cabenobolso.persistencia.PersistenciaArquivo;


public class GeradorDadosExemplo {
    public static void main(String[] args) throws Exception {
        PlanejamentoControle c = TestesCasoReferencia.montarCasoReferencia();
        String destino = args.length > 0 ? args[0] : "dados/caso_referencia.dat";
        c.salvar(destino);
        System.out.println("Arquivo de exemplo gerado em: " + destino);
    }
}
