package cabenobolso.teste;

import cabenobolso.controle.PlanejamentoControle;
import cabenobolso.persistencia.PersistenciaArquivo;
import cabenobolso.modelo.Despesa;
import cabenobolso.modelo.Dinheiro;
import cabenobolso.modelo.Participante;
import cabenobolso.modelo.ResumoParticipante;
import cabenobolso.modelo.TipoDespesa;
import cabenobolso.persistencia.PersistenciaException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Testes do caso de referência (seção 4 do enunciado) e dos 6 testes adicionais, mais alguns ai.
 * Não usa JUnit de propósito: roda só com o JDK. Para cada teste imprime ->
 * ENTRADA, ESPERADO, OBTIDO e o resultado (OK/FALHOU), que servem de evidência para o relatório.
 * ta comprovado.
 * Execução... na duvida é só ver o README.
 * O programa termina com código 1 se algum teste falhar.
 */
public class TestesCasoReferencia {

    private static int total = 0;
    private static int falhas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== TESTES - Cabe no bolso ===\n");

        casoReferencia();
        teste1CentavosNoRateio();
        teste2EntradaOuVinculoInvalido();
        teste3AlteracaoDeOrcamento();
        teste4ExclusaoDeDespesa();
        teste5SalvarEReiniciar();
        teste6ProblemaNoArquivo();

        System.out.println("\n=== TESTES EXTRAS (robustez) ===\n");
        extraPrecisaoDecimal();
        extraEdicaoAtomicaDespesa();
        extraEdicaoAtomicaParticipante();
        extraCodigoDuplicadoERemocaoBloqueada();
        extraValoresEspeciais();
        extraOrdemDosParticipantes();
        extraCampoDeEdicaoComMilhar();
        extraDoubleImpreciso();

        System.out.println("\n=== RESULTADO: " + (total - falhas) + "/" + total + " verificações OK ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }


    /**
     * Monta o caso de referência (Ana, Bruno e Carla) em um controlador vazio.
     */
    public static PlanejamentoControle montarCasoReferencia() {
        PlanejamentoControle c = new PlanejamentoControle(new PersistenciaArquivo());
        c.adicionarParticipante("1", "Ana", "70,00");
        c.adicionarParticipante("2", "Bruno", "60,00");
        c.adicionarParticipante("3", "Carla", "70,00");
        Participante ana = c.getParticipantes().get(0);
        Participante bruno = c.getParticipantes().get(1);
        Participante carla = c.getParticipantes().get(2);
        c.adicionarDespesa(TipoDespesa.FIXA, "Transporte", "90,00", List.of(ana, bruno, carla));
        c.adicionarDespesa(TipoDespesa.POR_PESSOA, "Almoço", "20,00", List.of(ana, bruno, carla));
        c.adicionarDespesa(TipoDespesa.FIXA, "Atividade recreativa", "30,00", List.of(ana, carla));
        c.adicionarDespesa(TipoDespesa.POR_PESSOA, "Sobremesa", "10,00", List.of(ana, bruno, carla));
        return c;
    }

    //  testes obrigatórios
    private static void casoReferencia() {
        System.out.println("--- CASO DE REFERÊNCIA (passeio da turma) ---");
        PlanejamentoControle c = montarCasoReferencia();
        System.out.println("Entrada: Ana R$70 / Bruno R$60 / Carla R$70; Transporte fixa 90 (A,B,C); Almoço por pessoa 20 (A,B,C);");
        System.out.println("         Atividade fixa 30 (A,C); Sobremesa por pessoa 10 (A,B,C)");
        System.out.println(descreverResumo(c));
        verificar("Custo total", "R$ 210,00", Dinheiro.formatar(c.getCustoTotal()));
        verificar("Total Ana", "R$ 75,00", Dinheiro.formatar(c.getResumo().get(0).getGasto()));
        verificar("Total Bruno", "R$ 60,00", Dinheiro.formatar(c.getResumo().get(1).getGasto()));
        verificar("Total Carla", "R$ 75,00", Dinheiro.formatar(c.getResumo().get(2).getGasto()));
        verificar("Ana excede em", "R$ 5,00", Dinheiro.formatar(c.getResumo().get(0).getExcedente()));
        verificar("Carla excede em", "R$ 5,00", Dinheiro.formatar(c.getResumo().get(2).getExcedente()));
        verificar("Bruno dentro do limite (total = orçamento)", "true", String.valueOf(c.getResumo().get(1).isDentroDoOrcamento()));
        // Rateio individual de cada despesa (tabela da seção 4)
        Despesa atividade = c.getDespesas().get(2);
        verificar("Rateio Atividade: Bruno não participa (0,00)", "[15,00, 15,00]",
                atividade.calcularRateio().values().stream().map(Dinheiro::paraCampo).toList().toString());
        System.out.println();
    }

    private static void teste1CentavosNoRateio() {
        System.out.println("--- TESTE 1: Centavos no rateio ---");
        PlanejamentoControle c = new PlanejamentoControle(new PersistenciaArquivo());
        c.adicionarParticipante("1", "P1", "0");
        c.adicionarParticipante("2", "P2", "0");
        c.adicionarParticipante("3", "P3", "0");
        c.adicionarDespesa(TipoDespesa.FIXA, "Fixa 100", "100,00", new ArrayList<>(c.getParticipantes()));
        List<ResumoParticipante> r = c.getResumo();
        System.out.println("Entrada: despesa fixa de R$ 100,00 para os códigos 1, 2 e 3");
        verificar("Código 1", "R$ 33,34", Dinheiro.formatar(r.get(0).getGasto()));
        verificar("Código 2", "R$ 33,33", Dinheiro.formatar(r.get(1).getGasto()));
        verificar("Código 3", "R$ 33,33", Dinheiro.formatar(r.get(2).getGasto()));
        verificar("Soma", "R$ 100,00", Dinheiro.formatar(r.get(0).getGasto() + r.get(1).getGasto() + r.get(2).getGasto()));
        System.out.println();
    }

    private static void teste2EntradaOuVinculoInvalido() {
        System.out.println("--- TESTE 2: Entrada ou vínculo inválido ---");
        PlanejamentoControle c = montarCasoReferencia();
        String antes = descreverResumo(c);
        List<Participante> todos = new ArrayList<>(c.getParticipantes());

        exigirRecusa(c, "Despesa sem participantes", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "X", "10,00", new ArrayList<>()));
        exigirRecusa(c, "Valor negativo", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "X", "-5,00", todos));
        exigirRecusa(c, "Valor zero", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "X", "0", todos));
        exigirRecusa(c, "Descrição vazia", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "   ", "10,00", todos));
        exigirRecusa(c, "Valor vazio", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "X", "", todos));
        exigirRecusa(c, "Valor com letras", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "X", "abc", todos));
        exigirRecusa(c, "Participante não cadastrado no planejamento", () ->
                c.adicionarDespesa(TipoDespesa.FIXA, "X", "10,00", List.of(new Participante(99, "Fantasma", 0))));
        verificar("Dados inalterados após todas as recusas", antes, descreverResumo(c));
        System.out.println();
    }

    private static void teste3AlteracaoDeOrcamento() {
        System.out.println("--- TESTE 3: Alteração de orçamento ---");
        PlanejamentoControle c = montarCasoReferencia();
        Participante ana = c.getParticipantes().get(0);
        System.out.println("Entrada: orçamento de Ana alterado de R$ 70,00 para R$ 80,00");
        c.editarParticipante(ana, "Ana", "80,00");
        ResumoParticipante r = c.getResumo().get(0);
        verificar("Custo de Ana continua", "R$ 75,00", Dinheiro.formatar(r.getGasto()));
        verificar("Ana agora dentro do limite", "true", String.valueOf(r.isDentroDoOrcamento()));
        System.out.println();
    }

    private static void teste4ExclusaoDeDespesa() {
        System.out.println("--- TESTE 4: Exclusão de despesa ---");
        PlanejamentoControle c = montarCasoReferencia();
        Despesa atividade = c.getDespesas().get(2);
        System.out.println("Entrada: excluir \"" + atividade.getDescricao() + "\" do caso principal");
        c.removerDespesa(atividade);
        verificar("Custo total", "R$ 180,00", Dinheiro.formatar(c.getCustoTotal()));
        for (ResumoParticipante r : c.getResumo()) {
            verificar("Total de " + r.getParticipante().getNome(), "R$ 60,00", Dinheiro.formatar(r.getGasto()));
        }
        System.out.println();
    }

    private static void teste5SalvarEReiniciar() throws IOException, PersistenciaException {
        System.out.println("--- TESTE 5: Salvar e reiniciar ---");
        Path dir = Files.createTempDirectory("cabenobolso-teste");
        String arquivo = dir.resolve("caso.dat").toString();
        PlanejamentoControle original = montarCasoReferencia();
        original.salvar(arquivo);

        PlanejamentoControle novo = new PlanejamentoControle(new PersistenciaArquivo()); // "reiniciar" o programa
        System.out.println("Entrada: salvar o caso principal, abrir um controlador novo (vazio) e carregar o arquivo");
        verificar("Planejamento novo começa vazio", "0 participantes, 0 despesas",
                novo.getParticipantes().size() + " participantes, " + novo.getDespesas().size() + " despesas");
        novo.carregar(arquivo);
        verificar("Participantes (sem duplicar)", "3", String.valueOf(novo.getParticipantes().size()));
        verificar("Despesas (sem duplicar)", "4", String.valueOf(novo.getDespesas().size()));
        verificar("Custo total", "R$ 210,00", Dinheiro.formatar(novo.getCustoTotal()));
        verificar("Resultados idênticos ao original", descreverResumo(original), descreverResumo(novo));
        verificar("Vínculo: Atividade recreativa -> Ana e Carla", "[Ana, Carla]",
                nomes(novo.getDespesas().get(2)));
        // carregar de novo o mesmo arquivo não deve duplicar
        novo.carregar(arquivo);
        verificar("Carregar 2x não duplica registros", "3 participantes, 4 despesas",
                novo.getParticipantes().size() + " participantes, " + novo.getDespesas().size() + " despesas");
        System.out.println();
    }

    private static void teste6ProblemaNoArquivo() throws IOException {
        System.out.println("--- TESTE 6: Problema no arquivo ---");
        PlanejamentoControle c = montarCasoReferencia();
        String antes = descreverResumo(c);

        String ausente = Files.createTempDirectory("cabenobolso-teste").resolve("nao-existe.dat").toString();
        System.out.println("Entrada: carregar arquivo inexistente");
        String msg1 = capturarMensagemCarregar(c, ausente);
        verificar("Mensagem clara para arquivo ausente", "true", String.valueOf(msg1.startsWith("Arquivo não encontrado")));
        System.out.println("   mensagem exibida: " + msg1);

        Path invalido = Files.createTempFile("cabenobolso-invalido", ".dat");
        Files.write(invalido, "isto não é um planejamento".getBytes(StandardCharsets.UTF_8));
        System.out.println("Entrada: carregar arquivo com conteúdo inválido");
        String msg2 = capturarMensagemCarregar(c, invalido.toString());
        verificar("Mensagem clara para arquivo inválido", "true", String.valueOf(msg2.contains("corrompido")));
        System.out.println("   mensagem exibida: " + msg2);

        verificar("Falha ao carregar preserva o planejamento em uso", antes, descreverResumo(c));
        c.novoPlanejamento();
        verificar("É possível iniciar um planejamento vazio", "0 participantes, 0 despesas",
                c.getParticipantes().size() + " participantes, " + c.getDespesas().size() + " despesas");
        System.out.println();
    }

    //testes extras
    private static void extraPrecisaoDecimal() {
        System.out.println("--- EXTRA: precisão decimal (0,10 + 0,20 contra orçamento 0,30) ---");
        PlanejamentoControle c = new PlanejamentoControle(new PersistenciaArquivo());
        c.adicionarParticipante("1", "Ana", "0,30");
        List<Participante> ana = new ArrayList<>(c.getParticipantes());
        c.adicionarDespesa(TipoDespesa.POR_PESSOA, "A", "0,10", ana);
        c.adicionarDespesa(TipoDespesa.POR_PESSOA, "B", "0,20", ana);
        ResumoParticipante r = c.getResumo().get(0);
        verificar("Total = orçamento está DENTRO do limite", "true", String.valueOf(r.isDentroDoOrcamento()));
        verificar("Excedente", "R$ 0,00", Dinheiro.formatar(r.getExcedente()));
        System.out.println();
    }

    private static void extraEdicaoAtomicaDespesa() {
        System.out.println("--- EXTRA: editar despesa com valor inválido não pode apagar a original ---");
        PlanejamentoControle c = montarCasoReferencia();
        Despesa transporte = c.getDespesas().get(0);
        exigirRecusa(c, "Editar Transporte para valor -1", () ->
                c.editarDespesa(transporte, TipoDespesa.FIXA, "Transporte", "-1", new ArrayList<>(c.getParticipantes())));
        verificar("Quantidade de despesas", "4", String.valueOf(c.getDespesas().size()));
        verificar("Transporte continua na posição 0 com R$ 90,00", "Transporte R$ 90,00",
                c.getDespesas().get(0).getDescricao() + " " + Dinheiro.formatar(c.getDespesas().get(0).getValorCentavos()));

        c.editarDespesa(transporte, TipoDespesa.FIXA, "Transporte", "120,00", new ArrayList<>(c.getParticipantes()));
        verificar("Edição válida mantém a posição e atualiza o total", "Transporte | R$ 240,00",
                c.getDespesas().get(0).getDescricao() + " | " + Dinheiro.formatar(c.getCustoTotal()));
        System.out.println();
    }

    private static void extraEdicaoAtomicaParticipante() {
        System.out.println("--- EXTRA: editar participante com orçamento inválido não pode mudar o nome ---");
        PlanejamentoControle c = montarCasoReferencia();
        Participante ana = c.getParticipantes().get(0);
        exigirRecusa(c, "Editar Ana para 'NOVO NOME' com orçamento -5", () ->
                c.editarParticipante(ana, "NOVO NOME", "-5"));
        verificar("Nome continua", "Ana", ana.getNome());
        verificar("Orçamento continua", "R$ 70,00", Dinheiro.formatar(ana.getOrcamento()));
        System.out.println();
    }

    private static void extraCodigoDuplicadoERemocaoBloqueada() {
        System.out.println("--- EXTRA: código duplicado e remoção de participante com despesas ---");
        PlanejamentoControle c = montarCasoReferencia();
        exigirRecusa(c, "Cadastrar outro participante com código 1", () ->
                c.adicionarParticipante("1", "Daniel", "10,00"));
        verificar("Ana não foi sobrescrita", "Ana", c.getParticipantes().get(0).getNome());
        exigirRecusa(c, "Remover Ana (tem despesas vinculadas)", () ->
                c.removerParticipante(c.getParticipantes().get(0)));
        System.out.println();
    }

    private static void extraValoresEspeciais() {
        System.out.println("--- EXTRA: textos que o Double.parseDouble aceitava e agora são recusados ---");
        PlanejamentoControle c = new PlanejamentoControle(new PersistenciaArquivo());
        for (String txt : new String[]{"NaN", "Infinity", "10d", "1e5", "10,999", "1,2,3", "--5"}) {
            exigirRecusa(c, "Orçamento \"" + txt + "\"", () -> c.adicionarParticipante("1", "X", txt));
        }
        verificar("Aceita vírgula", "R$ 70,50", Dinheiro.formatar(Dinheiro.parse("70,50", "x")));
        verificar("Aceita ponto", "R$ 70,50", Dinheiro.formatar(Dinheiro.parse("70.50", "x")));
        verificar("Aceita inteiro e 'R$'", "R$ 70,00", Dinheiro.formatar(Dinheiro.parse("R$ 70", "x")));
        System.out.println();
    }

    private static void extraOrdemDosParticipantes() {
        System.out.println("--- EXTRA: o centavo que sobra vai para o MENOR código, mesmo com a lista fora de ordem ---");
        PlanejamentoControle c = new PlanejamentoControle(new PersistenciaArquivo());
        c.adicionarParticipante("1", "P1", "0");
        c.adicionarParticipante("2", "P2", "0");
        c.adicionarParticipante("3", "P3", "0");
        List<Participante> invertida = new ArrayList<>(c.getParticipantes());
        Collections.reverse(invertida);
        c.adicionarDespesa(TipoDespesa.FIXA, "Fixa 100", "100,00", invertida);
        List<ResumoParticipante> r = c.getResumo();
        verificar("Código 1 recebe o centavo extra", "R$ 33,34", Dinheiro.formatar(r.get(0).getGasto()));
        verificar("Código 2", "R$ 33,33", Dinheiro.formatar(r.get(1).getGasto()));
        verificar("Código 3", "R$ 33,33", Dinheiro.formatar(r.get(2).getGasto()));
        System.out.println();
    }

    private static void extraCampoDeEdicaoComMilhar() {
        System.out.println("--- EXTRA: o texto que a tela põe no campo ao editar deve poder ser salvo de volta (valores >= R$ 1.000) ---");
        PlanejamentoControle c = new PlanejamentoControle(new PersistenciaArquivo());
        c.adicionarParticipante("1", "Ana", "1500,00");
        c.adicionarParticipante("2", "Bruno", "1234567,89");
        Participante ana = c.getParticipantes().get(0);
        String campo = Dinheiro.paraCampo(ana.getOrcamento());
        verificar("Texto do campo de edição (sem separador de milhar)", "1500,00", campo);
        c.editarParticipante(ana, "Ana Maria", campo);
        verificar("Edição só do nome preserva o orçamento", "Ana Maria R$ 1.500,00",
                ana.getNome() + " " + Dinheiro.formatar(ana.getOrcamento()));
        verificar("Ida e volta paraCampo -> parse (R$ 1.234.567,89)", "123456789",
                String.valueOf(Dinheiro.parse(Dinheiro.paraCampo(123456789L), "x")));
        verificar("Exibição continua com separador de milhar", "R$ 1.234.567,89", Dinheiro.formatar(123456789L));
        System.out.println();
    }

    private static void extraDoubleImpreciso() {
        System.out.println("--- EXTRA: demonstração do problema do double (motivo de usar centavos) ---");
        double somaDouble = 0.1 + 0.2;
        verificar("0,1 + 0,2 em double", "0.30000000000000004", String.valueOf(somaDouble));
        verificar("0,1 + 0,2 em double é igual a 0,3?", "false", String.valueOf(somaDouble == 0.3));
        verificar("10 + 20 centavos em long é igual a 30?", "true", String.valueOf(10L + 20L == 30L));
        System.out.println();
    }

    //utilidades
    private static void verificar(String descricao, String esperado, String obtido) {
        total++;
        boolean ok = esperado.equals(obtido);
        if (!ok) {
            falhas++;
        }
        System.out.println("  [" + (ok ? "OK    " : "FALHOU") + "] " + descricao
                + "\n           esperado: " + esperado + "\n           obtido:   " + obtido);
    }


    private static void exigirRecusa(PlanejamentoControle c, String descricao, Runnable acao) {
        total++;
        try {
            acao.run();
            falhas++;
            System.out.println("  [FALHOU] " + descricao + " -> deveria ser recusado, mas foi aceito");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  [OK    ] " + descricao + " -> recusado: " + e.getMessage());
        }
    }

    private static String capturarMensagemCarregar(PlanejamentoControle c, String caminho) {
        try {
            c.carregar(caminho);
            return "(nenhum erro)";
        } catch (PersistenciaException e) {
            return e.getMessage();
        }
    }

    private static String nomes(Despesa d) {
        List<String> n = new ArrayList<>();
        for (Participante p : d.getParticipantes()) {
            n.add(p.getNome());
        }
        return n.toString();
    }

    private static String descreverResumo(PlanejamentoControle c) {
        StringBuilder sb = new StringBuilder();
        for (ResumoParticipante r : c.getResumo()) {
            sb.append(r.getParticipante().getNome()).append("=").append(Dinheiro.formatar(r.getGasto()))
                    .append("/").append(Dinheiro.formatar(r.getOrcamento())).append(" ");
        }
        sb.append("| total=").append(Dinheiro.formatar(c.getCustoTotal()));
        return sb.toString();
    }
}
