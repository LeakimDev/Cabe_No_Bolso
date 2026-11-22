package cabenobolso.modelo;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;


public final class Dinheiro {

    private static final Pattern FORMATO = Pattern.compile("-?\\d{1,12}([.,]\\d+)?");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Dinheiro() {
    }


    public static long parse(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo " + campo + " não pode ficar vazio.");
        }
        String s = texto.trim().replace("R$", "").replace(" ", "");
        if (!FORMATO.matcher(s).matches()) {
            throw new IllegalArgumentException(
                    "Valor inválido em " + campo + ": use apenas números, com vírgula ou ponto nos centavos (ex.: 70,50).");
        }
        BigDecimal valor = new BigDecimal(s.replace(',', '.'));
        if (valor.scale() > 2) {
            throw new IllegalArgumentException("O campo " + campo + " aceita no máximo 2 casas decimais.");
        }
        return valor.movePointRight(2).longValueExact();
    }

    /**
     * Formata centavos para exibição, com separador de milhar: "R$ 1.234,56".
     */
    public static String formatar(long centavos) {
        long abs = Math.abs(centavos);
        String reais = String.format(PT_BR, "%,d", abs / 100);
        return "R$ " + (centavos < 0 ? "-" : "") + reais + "," + String.format("%02d", abs % 100);
    }

    public static String paraCampo(long centavos) {
        long abs = Math.abs(centavos);
        return (centavos < 0 ? "-" : "") + (abs / 100) + "," + String.format("%02d", abs % 100);
    }
}
