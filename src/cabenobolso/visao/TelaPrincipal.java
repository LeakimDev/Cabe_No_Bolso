package cabenobolso.visao;

import cabenobolso.controle.PlanejamentoControle;
import cabenobolso.modelo.Despesa;
import cabenobolso.modelo.Dinheiro;
import cabenobolso.modelo.Participante;
import cabenobolso.modelo.ResumoParticipante;
import cabenobolso.modelo.TipoDespesa;
import cabenobolso.persistencia.PersistenciaArquivo;
import cabenobolso.persistencia.PersistenciaException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class TelaPrincipal extends JFrame {

    private final PlanejamentoControle controle;

    // Layout com CardLayout para alternar a área central
    private final CardLayout cardLayout;
    private final JPanel panelConteudoCentral;

    // Botões da Sidebar
    private JButton btnNavParticipantes, btnNavDespesas, btnNavResumo;

    // Componentes - Participantes
    private JTextField txtCodigo, txtNome, txtOrcamento;
    private Participante participanteEmEdicao = null;
    private JTable tabelaParticipantes;
    private DefaultTableModel modelParticipantes;

    // Componentes - Despesas
    private JTextField txtDescricaoDespesa, txtValorDespesa;
    private JComboBox<TipoDespesa> cbTipoDespesa;
    private JList<Participante> listParticipantesDespesa;
    private DefaultListModel<Participante> listModelParticipantes;
    private JTable tabelaDespesas;
    private DefaultTableModel modelDespesas;
    private Despesa despesaEmEdicao = null;

    // Componentes - Resumo & Métricas
    private JTable tabelaResumo;
    private DefaultTableModel modelResumo;
    private JTable tabelaResumoDespesas;
    private DefaultTableModel modelResumoDespesas;
    private JLabel lblValorCustoTotal, lblValorQtdParticipantes, lblValorQtdDespesas;

    // Cores (tema escuro, com verde como cor de destaque)
    private final Color BG_APP = new Color(30, 32, 36);
    private final Color BG_SIDEBAR = new Color(38, 40, 45);
    private final Color BG_CARD = new Color(45, 48, 54);
    private final Color BG_INPUT = new Color(36, 38, 43);

    private final Color COLOR_ACCENT = new Color(67, 160, 71);     // verde
    private final Color COLOR_PERIGO = new Color(165, 65, 60);     // botões de excluir
    private final Color COLOR_BORDA = new Color(70, 74, 82);
    private final Color COLOR_TEXT_WHITE = new Color(225, 228, 232);
    private final Color COLOR_TEXT_MUTED = new Color(150, 155, 165);

    // Fontes lógicas do Java: existem em qualquer sistema (Windows, Linux, Mac)
    private final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    private final Font FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private final Font FONT_BASE = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    public TelaPrincipal() {
        controle = new PlanejamentoControle(new PersistenciaArquivo());

        setTitle("Cabe no bolso - Planejador de Despesas Compartilhadas");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Painel Principal
        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.setBackground(BG_APP);

        cardLayout = new CardLayout();
        panelConteudoCentral = new JPanel(cardLayout);
        panelConteudoCentral.setBackground(BG_APP);

        panelConteudoCentral.add(criarPainelParticipantes(), "PARTICIPANTES");
        panelConteudoCentral.add(criarPainelDespesas(), "DESPESAS");
        panelConteudoCentral.add(criarPainelResumo(), "RESUMO");

        panelMain.add(criarSidebar(), BorderLayout.WEST);
        panelMain.add(panelConteudoCentral, BorderLayout.CENTER);

        add(panelMain);
    }

    //MENU LATERAL
    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JLabel lblLogo = new JLabel("Cabe no bolso");
        lblLogo.setFont(FONT_TITLE);
        lblLogo.setForeground(COLOR_TEXT_WHITE);
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(lblLogo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        // Navegação entre as telas
        btnNavParticipantes = criarBotaoSidebar("Participantes", true);
        btnNavDespesas = criarBotaoSidebar("Despesas", false);
        btnNavResumo = criarBotaoSidebar("Resumo", false);

        btnNavParticipantes.addActionListener(e -> navegarPara("PARTICIPANTES", btnNavParticipantes));
        btnNavDespesas.addActionListener(e -> navegarPara("DESPESAS", btnNavDespesas));
        btnNavResumo.addActionListener(e -> navegarPara("RESUMO", btnNavResumo));

        sidebar.add(btnNavParticipantes);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavDespesas);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavResumo);

        sidebar.add(Box.createVerticalGlue());

        // Botões de arquivo
        JLabel lblArquivo = new JLabel("Arquivo");
        lblArquivo.setFont(FONT_BASE);
        lblArquivo.setForeground(COLOR_TEXT_MUTED);
        lblArquivo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnNovo = criarBotaoSidebar("Novo planejamento", false);
        btnNovo.addActionListener(e -> {
            if (!controle.getParticipantes().isEmpty() || !controle.getDespesas().isEmpty()) {
                int r = JOptionPane.showConfirmDialog(this,
                        "Os dados atuais serão descartados. Deseja iniciar um novo planejamento?",
                        "Novo planejamento", JOptionPane.YES_NO_OPTION);
                if (r != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            controle.novoPlanejamento();
            limparFormularioParticipante();
            limparFormularioDespesa();
            atualizarTodasTelas();
            JOptionPane.showMessageDialog(this, "Novo planejamento iniciado!");
        });

        JButton btnSalvar = criarBotaoSidebar("Salvar planejamento", false);
        btnSalvar.addActionListener(e -> salvarPlanejamento());

        JButton btnCarregar = criarBotaoSidebar("Carregar planejamento", false);
        btnCarregar.addActionListener(e -> carregarPlanejamento());

        sidebar.add(lblArquivo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNovo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnSalvar);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnCarregar);

        return sidebar;
    }

    private void navegarPara(String cardName, JButton btnAtivo) {
        cardLayout.show(panelConteudoCentral, cardName);
        resetarBotoesSidebar();
        btnAtivo.setBackground(COLOR_ACCENT);
        btnAtivo.setForeground(Color.WHITE);
    }

    private void resetarBotoesSidebar() {
        for (JButton btn : new JButton[]{btnNavParticipantes, btnNavDespesas, btnNavResumo}) {
            btn.setBackground(BG_SIDEBAR);
            btn.setForeground(COLOR_TEXT_MUTED);
        }
    }

    // Botão "chato" de propósito: retângulo liso, sem cantos arredondados
    private JButton criarBotaoSidebar(String texto, boolean ativo) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_BASE);
        btn.setForeground(ativo ? Color.WHITE : COLOR_TEXT_MUTED);
        btn.setBackground(ativo ? COLOR_ACCENT : BG_SIDEBAR);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true); // opaque DEPOIS: assim o Swing pinta o fundo com a cor definida
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(190, 34));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        return btn;
    }

    //PAINEL DE PARTICIPANTES
    private JPanel criarPainelParticipantes() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_APP);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pForm = criarCardPanel("Cadastrar / Editar Participante");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        pForm.add(criarLabel("Código:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtCodigo = criarTextField(8);
        pForm.add(txtCodigo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        pForm.add(criarLabel("Nome:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtNome = criarTextField(25);
        pForm.add(txtNome, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        pForm.add(criarLabel("Orçamento (R$):"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtOrcamento = criarTextField(12);
        pForm.add(txtOrcamento, gbc);

        JPanel pBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pBotoes.setOpaque(false);
        JButton btnSalvarParticipante = criarBotaoAcao("Salvar Participante", COLOR_ACCENT);
        btnSalvarParticipante.addActionListener(e -> salvarParticipante());

        JButton btnLimpar = criarBotaoAcao("Limpar Campos", BG_CARD);
        btnLimpar.addActionListener(e -> limparFormularioParticipante());

        pBotoes.add(btnSalvarParticipante);
        pBotoes.add(btnLimpar);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        pForm.add(pBotoes, gbc);

        modelParticipantes = new DefaultTableModel(new String[]{"Código", "Nome", "Orçamento (R$)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaParticipantes = new JTable(modelParticipantes);
        estilizarTabelaDark(tabelaParticipantes);

        tabelaParticipantes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabelaParticipantes.getSelectedRow();
                if (row >= 0 && row < controle.getParticipantes().size()) {
                    Participante p = controle.getParticipantes().get(row);
                    participanteEmEdicao = p; // a partir daqui, "Salvar" EDITA este participante
                    txtCodigo.setText(String.valueOf(p.getCodigo()));
                    txtCodigo.setEditable(false);
                    txtNome.setText(p.getNome());
                    txtOrcamento.setText(Dinheiro.paraCampo(p.getOrcamento()));
                }
            }
        });

        JPanel pCentro = new JPanel(new BorderLayout(10, 10));
        pCentro.setOpaque(false);
        pCentro.add(criarScrollPaneDark(tabelaParticipantes), BorderLayout.CENTER);

        JButton btnRemoverParticipante = criarBotaoAcao("Remover Participante Selecionado", COLOR_PERIGO);
        btnRemoverParticipante.addActionListener(e -> removerParticipante());
        pCentro.add(btnRemoverParticipante, BorderLayout.SOUTH);

        panel.add(pForm, BorderLayout.NORTH);
        panel.add(pCentro, BorderLayout.CENTER);

        return panel;
    }

    //PAINEL DE DESPESAS
    private JPanel criarPainelDespesas() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_APP);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pForm = criarCardPanel("Cadastrar / Editar Despesa");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        pForm.add(criarLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtDescricaoDespesa = criarTextField(25);
        pForm.add(txtDescricaoDespesa, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        pForm.add(criarLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtValorDespesa = criarTextField(12);
        pForm.add(txtValorDespesa, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        pForm.add(criarLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        cbTipoDespesa = new JComboBox<>(TipoDespesa.values());
        cbTipoDespesa.setFont(FONT_BASE);
        cbTipoDespesa.setBackground(BG_INPUT);
        cbTipoDespesa.setForeground(COLOR_TEXT_WHITE);
        pForm.add(cbTipoDespesa, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        pForm.add(criarLabel("Participantes Vinculados:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        listModelParticipantes = new DefaultListModel<>();
        listParticipantesDespesa = new JList<>(listModelParticipantes);
        listParticipantesDespesa.setFont(FONT_BASE);
        listParticipantesDespesa.setBackground(BG_INPUT);
        listParticipantesDespesa.setForeground(COLOR_TEXT_WHITE);
        listParticipantesDespesa.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollList = criarScrollPaneDark(listParticipantesDespesa);
        scrollList.setPreferredSize(new Dimension(220, 80));

        JLabel lblDica = new JLabel("Segure Ctrl para selecionar mais de um participante.", SwingConstants.LEFT);
        lblDica.setFont(FONT_BASE);
        lblDica.setForeground(COLOR_TEXT_MUTED);

        JPanel pListaComDica = new JPanel(new BorderLayout(2, 4));
        pListaComDica.setOpaque(false);
        pListaComDica.add(scrollList, BorderLayout.CENTER);
        pListaComDica.add(lblDica, BorderLayout.SOUTH);

        pForm.add(pListaComDica, gbc);

        JPanel pBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pBotoes.setOpaque(false);
        JButton btnSalvarDespesa = criarBotaoAcao("Salvar Despesa", COLOR_ACCENT);
        btnSalvarDespesa.addActionListener(e -> salvarDespesa());

        JButton btnLimparD = criarBotaoAcao("Limpar Campos", BG_CARD);
        btnLimparD.addActionListener(e -> limparFormularioDespesa());

        pBotoes.add(btnSalvarDespesa);
        pBotoes.add(btnLimparD);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        pForm.add(pBotoes, gbc);

        modelDespesas = new DefaultTableModel(new String[]{"Descrição", "Tipo", "Valor (R$)", "Participantes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaDespesas = new JTable(modelDespesas);
        estilizarTabelaDark(tabelaDespesas);

        tabelaDespesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabelaDespesas.getSelectedRow();
                if (row >= 0 && row < controle.getDespesas().size()) {
                    despesaEmEdicao = controle.getDespesas().get(row);
                    txtDescricaoDespesa.setText(despesaEmEdicao.getDescricao());
                    txtValorDespesa.setText(Dinheiro.paraCampo(despesaEmEdicao.getValorCentavos()));
                    cbTipoDespesa.setSelectedItem(despesaEmEdicao.getTipo());

                    listParticipantesDespesa.clearSelection();
                    List<Integer> indices = new ArrayList<>();
                    for (int i = 0; i < listModelParticipantes.size(); i++) {
                        Participante p = listModelParticipantes.get(i);
                        if (despesaEmEdicao.getParticipantes().contains(p)) {
                            indices.add(i);
                        }
                    }
                    int[] indArray = indices.stream().mapToInt(i -> i).toArray();
                    listParticipantesDespesa.setSelectedIndices(indArray);
                }
            }
        });

        JPanel pCentro = new JPanel(new BorderLayout(10, 10));
        pCentro.setOpaque(false);
        pCentro.add(criarScrollPaneDark(tabelaDespesas), BorderLayout.CENTER);

        JButton btnRemoverDespesa = criarBotaoAcao("Excluir Despesa Selecionada", COLOR_PERIGO);
        btnRemoverDespesa.addActionListener(e -> removerDespesa());
        pCentro.add(btnRemoverDespesa, BorderLayout.SOUTH);

        panel.add(pForm, BorderLayout.NORTH);
        panel.add(pCentro, BorderLayout.CENTER);

        return panel;
    }

    //PAINEL DE RESUMO
    private JPanel criarPainelResumo() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_APP);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        lblValorCustoTotal = new JLabel("R$ 0,00");
        lblValorQtdParticipantes = new JLabel("0");
        lblValorQtdDespesas = new JLabel("0");

        JPanel pCards = criarCardPanel("Totais do planejamento");
        JPanel linhaTotais = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        linhaTotais.setOpaque(false);
        linhaTotais.add(criarLinhaTotal("Custo total:", lblValorCustoTotal));
        linhaTotais.add(criarLinhaTotal("Participantes:", lblValorQtdParticipantes));
        linhaTotais.add(criarLinhaTotal("Despesas:", lblValorQtdDespesas));
        GridBagConstraints gbcTot = new GridBagConstraints();
        gbcTot.anchor = GridBagConstraints.WEST;
        gbcTot.weightx = 1.0;
        pCards.add(linhaTotais, gbcTot);

        modelResumo = new DefaultTableModel(new String[]{"Código", "Participante", "Gasto Total (R$)", "Orçamento (R$)", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaResumo = new JTable(modelResumo);
        estilizarTabelaDark(tabelaResumo);

        tabelaResumo.getColumnModel().getColumn(4).setPreferredWidth(190); // cabe "Excedido em R$ 1.234,56"
        tabelaResumo.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String text = (value != null) ? value.toString() : "";

                if (!isSelected) {
                    c.setBackground(BG_CARD);
                    if (text.startsWith("Excedido")) {
                        c.setForeground(new Color(230, 120, 110));
                        setFont(FONT_BOLD);
                    } else {
                        c.setForeground(new Color(125, 200, 135));
                        setFont(FONT_BASE);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        modelResumoDespesas = new DefaultTableModel(new String[]{"Despesa", "Tipo", "Valor informado", "Participantes", "Custo total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaResumoDespesas = new JTable(modelResumoDespesas);
        estilizarTabelaDark(tabelaResumoDespesas);

        JPanel pTabelas = new JPanel(new GridLayout(2, 1, 0, 15));
        pTabelas.setOpaque(false);
        pTabelas.add(criarSecao("Situação de cada participante", criarScrollPaneDark(tabelaResumo)));
        pTabelas.add(criarSecao("Despesas do planejamento", criarScrollPaneDark(tabelaResumoDespesas)));

        panel.add(pCards, BorderLayout.NORTH);
        panel.add(pTabelas, BorderLayout.CENTER);

        return panel;
    }

    private JPanel criarSecao(String titulo, JComponent conteudo) {
        JPanel secao = new JPanel(new BorderLayout(0, 6));
        secao.setOpaque(false);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COLOR_TEXT_MUTED);
        secao.add(lbl, BorderLayout.NORTH);
        secao.add(conteudo, BorderLayout.CENTER);
        return secao;
    }


    private JPanel criarLinhaTotal(String titulo, JLabel lblValor) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        linha.setOpaque(false);
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(FONT_BASE);
        lblTit.setForeground(COLOR_TEXT_MUTED);
        lblValor.setFont(FONT_BOLD);
        lblValor.setForeground(COLOR_TEXT_WHITE);
        linha.add(lblTit);
        linha.add(lblValor);
        return linha;
    }

    //MÉTODOS AUXILIARES DE ESTILIZAÇÃO
    private JPanel criarCardPanel(String titulo) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDA), titulo,
                        javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                        FONT_BOLD, COLOR_TEXT_WHITE),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        return p;
    }

    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONT_BASE);
        lbl.setForeground(COLOR_TEXT_WHITE);
        return lbl;
    }

    private JTextField criarTextField(int columns) {
        JTextField txt = new JTextField(columns);
        txt.setFont(FONT_BASE);
        txt.setBackground(BG_INPUT);
        txt.setForeground(COLOR_TEXT_WHITE);
        txt.setCaretColor(COLOR_TEXT_WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDA, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return txt;
    }

    // Botão retangular simples: a cor vem do parâmetro "bg"
    private JButton criarBotaoAcao(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_BASE);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDA, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        return btn;
    }

    private void estilizarTabelaDark(JTable tabela) {
        tabela.setRowHeight(26);
        tabela.setFont(FONT_BASE);
        tabela.setBackground(BG_CARD);
        tabela.setForeground(COLOR_TEXT_WHITE);
        tabela.setSelectionBackground(new Color(60, 90, 65));
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setGridColor(COLOR_BORDA);

        tabela.getTableHeader().setFont(FONT_BOLD);
        tabela.getTableHeader().setBackground(BG_SIDEBAR);
        tabela.getTableHeader().setForeground(COLOR_TEXT_WHITE);
        tabela.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tabela.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    }

    private JScrollPane criarScrollPaneDark(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDA, 1));
        return scroll;
    }

    //AÇÕES DE PERSISTÊNCIA E DADOS
    private void salvarPlanejamento() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Planejamento Cabe no Bolso (*.dat)", "dat"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fc.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".dat")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".dat");
            }
            if (arquivo.exists()) {
                int r = JOptionPane.showConfirmDialog(this,
                        "O arquivo \"" + arquivo.getName() + "\" já existe. Deseja substituí-lo?",
                        "Confirmar", JOptionPane.YES_NO_OPTION);
                if (r != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try {
                controle.salvar(arquivo.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Planejamento salvo com sucesso!");
            } catch (PersistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro ao salvar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarPlanejamento() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Planejamento Cabe no Bolso (*.dat)", "dat"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Se falhar, o controlador mantém o planejamento que já estava em uso.
                controle.carregar(fc.getSelectedFile().getAbsolutePath());
                limparFormularioParticipante();
                limparFormularioDespesa();
                atualizarTodasTelas();
                JOptionPane.showMessageDialog(this, "Planejamento carregado com sucesso!");
            } catch (PersistenciaException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage() + "\n\nO planejamento atual foi mantido.",
                        "Erro ao carregar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarErro(Exception ex) {
        String msg = (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) ? ex.getMessage() : ex.toString();
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void salvarParticipante() {
        try {
            String codigo = txtCodigo.getText();
            String nome = txtNome.getText();
            String orcamento = txtOrcamento.getText();

            if (participanteEmEdicao != null) {

                controle.editarParticipante(participanteEmEdicao, nome, orcamento);
                JOptionPane.showMessageDialog(this, "Participante atualizado com sucesso!");
            } else {

                controle.adicionarParticipante(codigo, nome, orcamento);
                JOptionPane.showMessageDialog(this, "Participante adicionado com sucesso!");
            }

            limparFormularioParticipante();
            atualizarTodasTelas();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarErro(ex);
        }
    }

    private void removerParticipante() {
        int row = tabelaParticipantes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um participante na tabela para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Participante p = controle.getParticipantes().get(row);
            controle.removerParticipante(p);
            limparFormularioParticipante();
            atualizarTodasTelas();
            JOptionPane.showMessageDialog(this, "Participante removido com sucesso!");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarErro(ex);
        }
    }

    private void salvarDespesa() {
        try {
            TipoDespesa tipo = (TipoDespesa) cbTipoDespesa.getSelectedItem();
            List<Participante> selecionados = listParticipantesDespesa.getSelectedValuesList();

            if (despesaEmEdicao != null) {

                controle.editarDespesa(despesaEmEdicao, tipo, txtDescricaoDespesa.getText(),
                        txtValorDespesa.getText(), selecionados);
            } else {
                controle.adicionarDespesa(tipo, txtDescricaoDespesa.getText(),
                        txtValorDespesa.getText(), selecionados);
            }

            limparFormularioDespesa();
            atualizarTodasTelas();
            JOptionPane.showMessageDialog(this, "Despesa salva com sucesso!");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarErro(ex);
        }
    }

    private void removerDespesa() {
        int row = tabelaDespesas.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Despesa d = controle.getDespesas().get(row);
            controle.removerDespesa(d);
            limparFormularioDespesa();
            atualizarTodasTelas();
            JOptionPane.showMessageDialog(this, "Despesa excluída com sucesso!");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarErro(ex);
        }
    }

    private void limparFormularioParticipante() {
        participanteEmEdicao = null;
        txtCodigo.setText("");
        txtCodigo.setEditable(true);
        txtNome.setText("");
        txtOrcamento.setText("");
        tabelaParticipantes.clearSelection();
    }

    private void limparFormularioDespesa() {
        txtDescricaoDespesa.setText("");
        txtValorDespesa.setText("");
        cbTipoDespesa.setSelectedIndex(0);
        listParticipantesDespesa.clearSelection();
        tabelaDespesas.clearSelection();
        despesaEmEdicao = null;
    }


    private void atualizarTodasTelas() {

        List<Participante> selecaoAnterior = listParticipantesDespesa.getSelectedValuesList();

        modelParticipantes.setRowCount(0);
        listModelParticipantes.clear();
        for (Participante p : controle.getParticipantes()) {
            modelParticipantes.addRow(new Object[]{p.getCodigo(), p.getNome(), Dinheiro.formatar(p.getOrcamento())});
            listModelParticipantes.addElement(p);
        }
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < listModelParticipantes.size(); i++) {
            if (selecaoAnterior.contains(listModelParticipantes.get(i))) {
                indices.add(i);
            }
        }
        listParticipantesDespesa.setSelectedIndices(indices.stream().mapToInt(i -> i).toArray());

        modelDespesas.setRowCount(0);
        modelResumoDespesas.setRowCount(0);
        for (Despesa d : controle.getDespesas()) {
            modelDespesas.addRow(new Object[]{d.getDescricao(), d.getTipo(), Dinheiro.formatar(d.getValorCentavos()),
                    d.getParticipantes().size() + " participante(s)"});

            StringBuilder nomes = new StringBuilder();
            for (Participante p : d.getParticipantes()) {
                if (nomes.length() > 0) {
                    nomes.append(", ");
                }
                nomes.append(p.getNome());
            }
            modelResumoDespesas.addRow(new Object[]{d.getDescricao(), d.getTipo(),
                    Dinheiro.formatar(d.getValorCentavos()), nomes.toString(),
                    Dinheiro.formatar(d.calcularCustoTotal())});
        }

        modelResumo.setRowCount(0);
        for (ResumoParticipante r : controle.getResumo()) {
            Participante p = r.getParticipante();
            String situacao = r.isDentroDoOrcamento()
                    ? "Dentro do limite"
                    : "Excedido em " + Dinheiro.formatar(r.getExcedente());
            modelResumo.addRow(new Object[]{p.getCodigo(), p.getNome(), Dinheiro.formatar(r.getGasto()),
                    Dinheiro.formatar(r.getOrcamento()), situacao});
        }

        lblValorCustoTotal.setText(Dinheiro.formatar(controle.getCustoTotal()));
        lblValorQtdParticipantes.setText(String.valueOf(controle.getParticipantes().size()));
        lblValorQtdDespesas.setText(String.valueOf(controle.getDespesas().size()));
    }
}
