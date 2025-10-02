package view;

import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.DespesaController;
import model.Caixa;
import model.CaixaMovimento;
import model.Despesa;
import util.Sessao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LancamentoDespesaPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes do formulário
    private JComboBox<Despesa.Categoria> cmbCategoria;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JCheckBox chkPago;
    private JComboBox<Despesa.FormaPagamento> cmbFormaPagamento;
    private JFormattedTextField txtDataVencimento;
    private JFormattedTextField txtDataPagamento;
    private JSpinner spinnerParcelas;
    private JCheckBox chkRecorrente;
    private JSpinner spinnerMesesRecorrentes;
    private JLabel lblValorTotal;
    private JTable tabelaDespesas;
    private DefaultTableModel modeloTabelaDespesas;

    // Componentes de filtro
    private JFormattedTextField txtDataInicioFiltro;
    private JFormattedTextField txtDataFimFiltro;
    private JComboBox<Object> cmbCategoriaFiltro;
    private JTextField txtDescricaoFiltro;

    // Estilo
    private final Color primaryColor = new Color(154, 5, 38); // Vermelho escuro
    private final Color secondaryColor = new Color(94, 5, 38); // Vermelho claro
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightGreen = new Color(230, 255, 230); // Verde muito claro
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final DespesaController despesaController = new DespesaController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController movimentoController = new CaixaMovimentoController();

    // Variáveis de estado
    private List<Despesa> listaDespesas;
    private BigDecimal valorTotalDespesas;

    // Variáveis de filtro
    private LocalDate dataInicioFiltro;
    private LocalDate dataFimFiltro;
    private Despesa.Categoria categoriaFiltro;
    private String descricaoFiltro;

    // Formato de data
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public LancamentoDespesaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa estado
        listaDespesas = new ArrayList<>();
        valorTotalDespesas = BigDecimal.ZERO;

        // Inicializa filtros padrão (mês atual)
        LocalDate now = LocalDate.now();
        dataInicioFiltro = now.withDayOfMonth(1);
        dataFimFiltro = now.withDayOfMonth(now.lengthOfMonth());
        categoriaFiltro = null; // Todas
        descricaoFiltro = "";

        // Inicializa componentes de pagamento
        spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerParcelas.setPreferredSize(new Dimension(80, 25));
        spinnerParcelas.setFont(fieldFont);
        spinnerParcelas.setEnabled(false);
        chkRecorrente = new JCheckBox();
        chkRecorrente.setBackground(backgroundColor);
        spinnerMesesRecorrentes = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerMesesRecorrentes.setPreferredSize(new Dimension(80, 25));
        spinnerMesesRecorrentes.setFont(fieldFont);
        spinnerMesesRecorrentes.setEnabled(false);

        // Título
        JLabel lblTitulo = new JLabel("Lançamento de Despesas", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de formulário e tabela
        JPanel painelFormulario = criarPainelFormulario();
        JPanel painelTabela = criarPainelTabela();

        // Configura o JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelFormulario, painelTabela);
        splitPane.setResizeWeight(0.45);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(backgroundColor);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.31));
        add(splitPane, BorderLayout.CENTER);

        // Carrega dados iniciais
        carregarDespesasFiltradas();
    }

    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                        "Registrar Despesa", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Seção de Dados
        JPanel dataPanel = new JPanel(new GridBagLayout());
        dataPanel.setBackground(backgroundColor);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        GridBagConstraints gbcData = new GridBagConstraints();
        gbcData.insets = new Insets(2, 2, 2, 2);
        gbcData.fill = GridBagConstraints.HORIZONTAL;
        gbcData.anchor = GridBagConstraints.WEST;

        // Dados da Despesa
        JLabel lblDespesaTitle = new JLabel("Dados da Despesa");
        lblDespesaTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblDespesaTitle.setForeground(primaryColor);
        gbcData.gridx = 0;
        gbcData.gridy = 0;
        gbcData.gridwidth = 2;
        dataPanel.add(lblDespesaTitle, gbcData);

        // Categoria antes de Descrição
        JLabel lblCategoria = new JLabel("Categoria:");
        lblCategoria.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 1;
        gbcData.gridwidth = 1;
        gbcData.weightx = 0.0;
        dataPanel.add(lblCategoria, gbcData);
        cmbCategoria = new JComboBox<>(Despesa.Categoria.values());
        cmbCategoria.setPreferredSize(new Dimension(150, 25));
        cmbCategoria.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(cmbCategoria, gbcData);

        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 2;
        gbcData.weightx = 0.0;
        dataPanel.add(lblDescricao, gbcData);
        txtDescricao = new JTextField(20);
        txtDescricao.setPreferredSize(new Dimension(200, 25));
        txtDescricao.setFont(fieldFont);
        txtDescricao.setToolTipText("Digite a descrição da despesa");
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtDescricao, gbcData);

        JLabel lblValor = new JLabel("Valor:");
        lblValor.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 3;
        gbcData.weightx = 0.0;
        dataPanel.add(lblValor, gbcData);
        txtValor = new JTextField(10);
        txtValor.setText("R$ 0,00");
        txtValor.setPreferredSize(new Dimension(100, 25));
        txtValor.setFont(fieldFont);
        ((AbstractDocument) txtValor.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtValor, gbcData);

        JLabel lblDataVencimento = new JLabel("Data Vencimento:");
        lblDataVencimento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 4;
        gbcData.weightx = 0.0;
        dataPanel.add(lblDataVencimento, gbcData);
        try {
            MaskFormatter maskData = new MaskFormatter("##/##/####");
            txtDataVencimento = new JFormattedTextField(maskData);
            txtDataVencimento.setPreferredSize(new Dimension(100, 25));
            txtDataVencimento.setFont(fieldFont);
            txtDataVencimento.setToolTipText("Digite a data de vencimento (dd/MM/yyyy)");
            gbcData.gridx = 1;
            gbcData.weightx = 1.0;
            dataPanel.add(txtDataVencimento, gbcData);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Checkbox Pago antes dos campos de pagamento
        JLabel lblPago = new JLabel("Pago?:");
        lblPago.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 5;
        gbcData.weightx = 0.0;
        dataPanel.add(lblPago, gbcData);
        chkPago = new JCheckBox();
        chkPago.setBackground(backgroundColor);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(chkPago, gbcData);

        JLabel lblFormaPagamento = new JLabel("Forma Pagamento:");
        lblFormaPagamento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 6;
        gbcData.weightx = 0.0;
        dataPanel.add(lblFormaPagamento, gbcData);
        cmbFormaPagamento = new JComboBox<>(Despesa.FormaPagamento.values());
        cmbFormaPagamento.setPreferredSize(new Dimension(150, 25));
        cmbFormaPagamento.setFont(fieldFont);
        cmbFormaPagamento.setEnabled(false);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(cmbFormaPagamento, gbcData);

        JLabel lblParcelas = new JLabel("Parcelas:");
        lblParcelas.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 7;
        gbcData.weightx = 0.0;
        dataPanel.add(lblParcelas, gbcData);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(spinnerParcelas, gbcData);

        JLabel lblDataPagamento = new JLabel("Data Pagamento:");
        lblDataPagamento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 8;
        gbcData.weightx = 0.0;
        dataPanel.add(lblDataPagamento, gbcData);
        try {
            MaskFormatter maskDataPag = new MaskFormatter("##/##/####");
            txtDataPagamento = new JFormattedTextField(maskDataPag);
            txtDataPagamento.setPreferredSize(new Dimension(100, 25));
            txtDataPagamento.setFont(fieldFont);
            txtDataPagamento.setEnabled(false);
            txtDataPagamento.setToolTipText("Digite a data de pagamento (dd/MM/yyyy)");
            gbcData.gridx = 1;
            gbcData.weightx = 1.0;
            dataPanel.add(txtDataPagamento, gbcData);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Recorrente
        JLabel lblRecorrente = new JLabel("Recorrente Mensal?:");
        lblRecorrente.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 9;
        gbcData.weightx = 0.0;
        dataPanel.add(lblRecorrente, gbcData);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(chkRecorrente, gbcData);

        JLabel lblMesesRecorrentes = new JLabel("Nº Meses:");
        lblMesesRecorrentes.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 10;
        gbcData.weightx = 0.0;
        dataPanel.add(lblMesesRecorrentes, gbcData);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(spinnerMesesRecorrentes, gbcData);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.4;
        mainPanel.add(dataPanel, gbc);

        // Seção de Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        botoesPanel.setBackground(backgroundColor);
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setBorder(BorderFactory.createEmptyBorder());
        btnLimpar.setPreferredSize(new Dimension(80, 30));
        btnLimpar.setHorizontalAlignment(SwingConstants.CENTER);
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar.setToolTipText("Limpar todos os campos");
        botoesPanel.add(btnLimpar);

        JButton btnAdicionar = new JButton("Adicionar Despesa");
        btnAdicionar.setBackground(primaryColor);
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.setBorder(BorderFactory.createEmptyBorder());
        btnAdicionar.setPreferredSize(new Dimension(120, 30));
        btnAdicionar.setHorizontalAlignment(SwingConstants.CENTER);
        btnAdicionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdicionar.setToolTipText("Adicionar despesa à lista");
        botoesPanel.add(btnAdicionar);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(botoesPanel, gbc);

        // Listeners
        btnAdicionar.addActionListener(e -> adicionarDespesa());
        btnLimpar.addActionListener(e -> limparCampos());
        chkPago.addActionListener(e -> atualizarCamposPagamento());
        cmbFormaPagamento.addActionListener(e -> atualizarParcelas());
        chkRecorrente.addActionListener(e -> spinnerMesesRecorrentes.setEnabled(chkRecorrente.isSelected()));

        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                        "Despesas Pendentes e Pagas", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Painel de filtros acima da tabela
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtrosPanel.setBackground(backgroundColor);
        filtrosPanel.setBorder(new EmptyBorder(5, 0, 10, 0)); // Espaço abaixo para a tabela

        JLabel lblDe = new JLabel("De:");
        lblDe.setFont(labelFont);
        filtrosPanel.add(lblDe);
        try {
            MaskFormatter maskData = new MaskFormatter("##/##/####");
            txtDataInicioFiltro = new JFormattedTextField(maskData);
            txtDataInicioFiltro.setPreferredSize(new Dimension(100, 25));
            txtDataInicioFiltro.setFont(fieldFont);
            txtDataInicioFiltro.setText(sdf.format(java.sql.Date.valueOf(dataInicioFiltro)));
            filtrosPanel.add(txtDataInicioFiltro);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JLabel lblAte = new JLabel("Até:");
        lblAte.setFont(labelFont);
        filtrosPanel.add(lblAte);
        try {
            MaskFormatter maskData = new MaskFormatter("##/##/####");
            txtDataFimFiltro = new JFormattedTextField(maskData);
            txtDataFimFiltro.setPreferredSize(new Dimension(100, 25));
            txtDataFimFiltro.setFont(fieldFont);
            txtDataFimFiltro.setText(sdf.format(java.sql.Date.valueOf(dataFimFiltro)));
            filtrosPanel.add(txtDataFimFiltro);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JLabel lblCategoriaFiltro = new JLabel("Categoria:");
        lblCategoriaFiltro.setFont(labelFont);
        filtrosPanel.add(lblCategoriaFiltro);
        cmbCategoriaFiltro = new JComboBox<>();
        cmbCategoriaFiltro.addItem("Todas");
        for (Despesa.Categoria cat : Despesa.Categoria.values()) {
            cmbCategoriaFiltro.addItem(cat);
        }
        cmbCategoriaFiltro.setPreferredSize(new Dimension(150, 25));
        cmbCategoriaFiltro.setFont(fieldFont);
        filtrosPanel.add(cmbCategoriaFiltro);

        JLabel lblDescricaoFiltro = new JLabel("Descrição:");
        lblDescricaoFiltro.setFont(labelFont);
        filtrosPanel.add(lblDescricaoFiltro);
        txtDescricaoFiltro = new JTextField(15);
        txtDescricaoFiltro.setPreferredSize(new Dimension(150, 25));
        txtDescricaoFiltro.setFont(fieldFont);
        filtrosPanel.add(txtDescricaoFiltro);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBackground(primaryColor);
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setPreferredSize(new Dimension(80, 25));
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        filtrosPanel.add(btnFiltrar);

        panel.add(filtrosPanel, BorderLayout.NORTH);

        // Tabela
        String[] colunas = {"ID", "Descrição", "Categoria", "Valor", "Forma Pag.", "Data Venc.", "Data Pag.", "Status"};
        modeloTabelaDespesas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaDespesas = new JTable(modeloTabelaDespesas) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                c.setForeground(Color.BLACK);
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                return c;
            }
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getSelectedRow() >= 0) {
                    Rectangle rect = getCellRect(getSelectedRow(), 0, true);
                    rect.width = getWidth();
                    g.setColor(Color.BLACK);
                    g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
                }
            }
        };
        tabelaDespesas.setShowGrid(false);
        tabelaDespesas.setIntercellSpacing(new Dimension(0, 0));
        tabelaDespesas.setFillsViewportHeight(true);
        tabelaDespesas.setRowHeight(25);
        tabelaDespesas.setFont(fieldFont);
        tabelaDespesas.setBackground(backgroundColor);
        JTableHeader header = tabelaDespesas.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaDespesas.getColumnCount(); i++) {
            tabelaDespesas.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        JScrollPane scroll = new JScrollPane(tabelaDespesas);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);

        // Painel inferior
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        southPanel.setBackground(backgroundColor);
        lblValorTotal = new JLabel("Valor Total: R$ 0,00");
        lblValorTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValorTotal.setForeground(primaryColor);
        lblValorTotal.setBorder(new EmptyBorder(5, 5, 5, 5));
        southPanel.add(lblValorTotal);

        JButton btnPagar = new JButton("Pagar Selecionada");
        btnPagar.setBackground(primaryColor);
        btnPagar.setForeground(Color.WHITE);
        btnPagar.setBorder(BorderFactory.createEmptyBorder());
        btnPagar.setPreferredSize(new Dimension(120, 30));
        btnPagar.setHorizontalAlignment(SwingConstants.CENTER);
        btnPagar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPagar.setToolTipText("Pagar despesa selecionada");
        btnPagar.addActionListener(e -> pagarDespesaSelecionada());
        southPanel.add(btnPagar);

        JButton btnDeletar = new JButton("Deletar Selecionada");
        btnDeletar.setBackground(Color.LIGHT_GRAY);
        btnDeletar.setForeground(Color.BLACK);
        btnDeletar.setBorder(BorderFactory.createEmptyBorder());
        btnDeletar.setPreferredSize(new Dimension(120, 30));
        btnDeletar.setHorizontalAlignment(SwingConstants.CENTER);
        btnDeletar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeletar.setToolTipText("Deletar despesa selecionada");
        btnDeletar.addActionListener(e -> deletar());
        southPanel.add(btnDeletar);

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    // Método para aplicar os filtros informados
    private void aplicarFiltros() {
        try {
            dataInicioFiltro = parseData(txtDataInicioFiltro.getText());
            dataFimFiltro = parseData(txtDataFimFiltro.getText());
            Object selectedCat = cmbCategoriaFiltro.getSelectedItem();
            categoriaFiltro = (selectedCat instanceof Despesa.Categoria) ? (Despesa.Categoria) selectedCat : null;
            descricaoFiltro = txtDescricaoFiltro.getText().trim().toLowerCase();
            carregarDespesasFiltradas();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro nos filtros: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarCamposPagamento() {
        boolean pago = chkPago.isSelected();
        cmbFormaPagamento.setEnabled(pago);
        txtDataPagamento.setEnabled(pago);
        atualizarParcelas();
    }

    private void atualizarParcelas() {
        if (!chkPago.isSelected()) {
            spinnerParcelas.setEnabled(false);
            spinnerParcelas.setValue(1);
            return;
        }
        Despesa.FormaPagamento forma = (Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem();
        if (forma == Despesa.FormaPagamento.CREDITO || forma == Despesa.FormaPagamento.BOLETO) {
            spinnerParcelas.setModel(new SpinnerNumberModel(2, 2, 12, 1));
            spinnerParcelas.setEnabled(true);
        } else {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 1, 1));
            spinnerParcelas.setEnabled(false);
        }
    }

    private void limparCampos() {
        cmbCategoria.setSelectedIndex(0);
        txtDescricao.setText("");
        txtValor.setText("R$ 0,00");
        txtDataVencimento.setText("");
        chkPago.setSelected(false);
        cmbFormaPagamento.setSelectedIndex(0);
        spinnerParcelas.setValue(1);
        txtDataPagamento.setText("");
        chkRecorrente.setSelected(false);
        spinnerMesesRecorrentes.setValue(1);
        atualizarCamposPagamento();
    }

    private void adicionarDespesa() {
        try {
            String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
            String descricao = txtDescricao.getText().trim();
            if (descricao.isEmpty()) {
                throw new IllegalArgumentException("Descrição é obrigatória.");
            }
            BigDecimal valorTotal = parseValor(txtValor.getText());
            if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor deve ser maior que zero.");
            }
            LocalDate dataVencimento = parseData(txtDataVencimento.getText());
            boolean isPago = chkPago.isSelected();
            Despesa.FormaPagamento formaPagamento = (Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem();
            int numParcelas = (Integer) spinnerParcelas.getValue();
            LocalDate dataPagamento = isPago ? parseData(txtDataPagamento.getText()) : null;
            boolean isRecorrente = chkRecorrente.isSelected();
            int numMeses = (Integer) spinnerMesesRecorrentes.getValue();
            if (isRecorrente) {
                lancarDespesasRecorrentes(valorTotal, numParcelas, dataVencimento, dataPagamento, usuarioLogado, numMeses);
            } else if (numParcelas > 1) {
                lancarDespesasParceladas(valorTotal, numParcelas, dataVencimento, dataPagamento, usuarioLogado);
            } else {
                lancarDespesaUnica(valorTotal, dataVencimento, dataPagamento, usuarioLogado);
            }
            JOptionPane.showMessageDialog(this, "Despesa(s) adicionada(s) com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarDespesasFiltradas();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar despesa: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal parseValor(String text) {
        String cleaned = text.replace("R$ ", "").replace(".", "").replace(",", ".");
        return new BigDecimal(cleaned);
    }

    private void lancarDespesaUnica(BigDecimal valorTotal, LocalDate dataVencimento, LocalDate dataPagamento, String usuarioLogado) throws SQLException {
        Despesa d = new Despesa();
        d.setDescricao(txtDescricao.getText());
        d.setCategoria((Despesa.Categoria) cmbCategoria.getSelectedItem());
        d.setValor(valorTotal);
        d.setFormaPagamento((Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem());
        d.setDataVencimento(dataVencimento);
        d.setDataPagamento(dataPagamento);
        d.setStatus(dataPagamento != null ? Despesa.Status.PAGO : Despesa.Status.PENDENTE);
        despesaController.adicionar(d, usuarioLogado);
        if (d.getStatus() == Despesa.Status.PAGO) {
            registrarMovimentoCaixa(d, usuarioLogado);
        }
        listaDespesas.add(d);
        atualizarTabelaDespesas();
    }

    private void lancarDespesasParceladas(BigDecimal valorTotal, int numParcelas, LocalDate dataVencimentoInicial, LocalDate dataPagamento, String usuarioLogado) throws SQLException {
        BigDecimal valorParcela = valorTotal.divide(BigDecimal.valueOf(numParcelas), 2, RoundingMode.DOWN);
        BigDecimal somaParcelas = valorParcela.multiply(BigDecimal.valueOf(numParcelas));
        BigDecimal ajusteUltima = valorTotal.subtract(somaParcelas);
        String descricaoBase = txtDescricao.getText();
        Despesa.Categoria categoria = (Despesa.Categoria) cmbCategoria.getSelectedItem();
        Despesa.FormaPagamento forma = (Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem();
        for (int i = 1; i <= numParcelas; i++) {
            Despesa parcela = new Despesa();
            parcela.setDescricao("Parcela " + i + "/" + numParcelas + " - " + descricaoBase);
            parcela.setCategoria(categoria);
            BigDecimal valor = (i == numParcelas) ? valorParcela.add(ajusteUltima) : valorParcela;
            parcela.setValor(valor);
            parcela.setFormaPagamento(forma);
            parcela.setDataVencimento(dataVencimentoInicial.plusMonths(i - 1));
            parcela.setDataPagamento(dataPagamento); // Se pago, aplica a mesma data
            parcela.setStatus(dataPagamento != null ? Despesa.Status.PAGO : Despesa.Status.PENDENTE);
            despesaController.adicionar(parcela, usuarioLogado);
            if (parcela.getStatus() == Despesa.Status.PAGO) {
                registrarMovimentoCaixa(parcela, usuarioLogado);
            }
            listaDespesas.add(parcela);
        }
        atualizarTabelaDespesas();
    }

    private void lancarDespesasRecorrentes(BigDecimal valorTotal, int numParcelas, LocalDate dataVencimentoInicial, LocalDate dataPagamento, String usuarioLogado, int numMeses) throws SQLException {
        // Lança despesas mensais recorrentes. Para término ou alteração, usuário deleta/edita manualmente via painel.
        for (int mes = 0; mes < numMeses; mes++) {
            LocalDate dataVencimento = dataVencimentoInicial.plusMonths(mes);
            if (numParcelas > 1) {
                lancarDespesasParceladas(valorTotal, numParcelas, dataVencimento, dataPagamento, usuarioLogado);
            } else {
                lancarDespesaUnica(valorTotal, dataVencimento, dataPagamento, usuarioLogado);
            }
        }
    }

    private void deletar() {
        int row = tabelaDespesas.getSelectedRow();
        if (row >= 0) {
            Despesa d = listaDespesas.get(row);
            if (JOptionPane.showConfirmDialog(this, "Confirmar exclusão da despesa ID " + d.getId() + "?", "Confirmação",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    despesaController.remover(d.getId());
                    carregarDespesasFiltradas();
                    limparCampos();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage(), "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa para deletar.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pagarDespesaSelecionada() {
        int row = tabelaDespesas.getSelectedRow();
        if (row >= 0) {
            Despesa d = listaDespesas.get(row);
            if (d.getStatus() == Despesa.Status.PAGO) {
                JOptionPane.showMessageDialog(this, "Despesa já paga.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String dataStr = JOptionPane.showInputDialog(this, "Informe a data de pagamento (dd/MM/yyyy):");
            if (dataStr != null && !dataStr.isEmpty()) {
                try {
                    LocalDate dataPagamento = parseData(dataStr);
                    String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
                    despesaController.marcarComoPago(d.getId(), dataPagamento, usuarioLogado);
                    registrarMovimentoCaixa(d, usuarioLogado);
                    JOptionPane.showMessageDialog(this, "Despesa paga com sucesso!", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarDespesasFiltradas();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao pagar: " + ex.getMessage(), "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa para pagar.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarMovimentoCaixa(Despesa d, String usuarioLogado) throws SQLException {
        if (!caixaController.existeCaixaAberto()) {
            throw new SQLException("Não há caixa aberto para registrar o movimento.");
        }
        Caixa caixaAberto = caixaController.getCaixaAberto();
        CaixaMovimento movimento = new CaixaMovimento();
        movimento.setCaixa(caixaAberto);
        movimento.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
        movimento.setOrigem(CaixaMovimento.OrigemMovimento.DESPESA);
        movimento.setFormaPagamento(converterFormaPagamento(d.getFormaPagamento()));
        movimento.setValor(d.getValor());
        movimento.setDescricao("Pagamento de despesa: " + d.getDescricao());
        movimento.setDataHora(LocalDateTime.now());
        movimento.setUsuario(usuarioLogado);
        movimentoController.adicionarMovimento(movimento);
    }

    private CaixaMovimento.FormaPagamento converterFormaPagamento(Despesa.FormaPagamento forma) {
        switch (forma) {
            case DINHEIRO:
                return CaixaMovimento.FormaPagamento.DINHEIRO;
            case DEBITO:
                return CaixaMovimento.FormaPagamento.DEBITO;
            case CREDITO:
                return CaixaMovimento.FormaPagamento.CREDITO;
            case PIX:
                return CaixaMovimento.FormaPagamento.PIX;
            case BOLETO:
                return CaixaMovimento.FormaPagamento.BOLETO;
            default:
                throw new IllegalArgumentException("Forma de pagamento não suportada: " + forma);
        }
    }

    private void carregarDespesasFiltradas() {
        try {
            List<Despesa> todas = despesaController.listarTodas();
            listaDespesas.clear();
            valorTotalDespesas = BigDecimal.ZERO;
            for (Despesa d : todas) {
                LocalDate venc = d.getDataVencimento();
                String descLower = d.getDescricao().toLowerCase();
                if (venc != null && !venc.isBefore(dataInicioFiltro) && !venc.isAfter(dataFimFiltro) &&
                        (categoriaFiltro == null || d.getCategoria() == categoriaFiltro) &&
                        (descricaoFiltro.isEmpty() || descLower.contains(descricaoFiltro))) {
                    listaDespesas.add(d);
                    valorTotalDespesas = valorTotalDespesas.add(d.getValor());
                }
            }
            atualizarTabelaDespesas();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar despesas: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabelaDespesas() {
        modeloTabelaDespesas.setRowCount(0);
        for (Despesa d : listaDespesas) {
            modeloTabelaDespesas.addRow(new Object[]{
                    d.getId(),
                    d.getDescricao(),
                    d.getCategoria(),
                    formatValorTabela(d.getValor()),
                    d.getFormaPagamento(),
                    d.getDataVencimento(),
                    d.getDataPagamento(),
                    d.getStatus()
            });
        }
        lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalDespesas));
    }

    private String formatValorTabela(BigDecimal valor) {
        DecimalFormat df = new DecimalFormat("R$ #,##0.00");
        return df.format(valor);
    }

    private LocalDate parseData(String dataStr) throws ParseException {
        if (dataStr.trim().isEmpty() || !dataStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
            throw new ParseException("Data inválida ou não informada.", 0);
        }
        Date date = sdf.parse(dataStr);
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private class CurrencyDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, string);
            if (isValidInput(sb.toString())) {
                String formatted = formatCurrency(removeNonDigits(sb.toString()));
                super.replace(fb, 0, fb.getDocument().getLength(), formatted, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs)
                throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, string);
            if (isValidInput(sb.toString())) {
                String formatted = formatCurrency(removeNonDigits(sb.toString()));
                super.replace(fb, 0, fb.getDocument().getLength(), formatted, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.delete(offset, offset + length);
            String formatted = formatCurrency(removeNonDigits(sb.toString()));
            super.replace(fb, 0, fb.getDocument().getLength(), formatted, null);
        }

        private boolean isValidInput(String text) {
            return text.matches("[R$ 0-9,.]*");
        }

        private String removeNonDigits(String text) {
            return text.replaceAll("[^0-9]", "");
        }

        private String formatCurrency(String digits) {
            if (digits.isEmpty()) return "R$ 0,00";
            while (digits.length() < 3) {
                digits = "0" + digits;
            }
            String cents = digits.substring(digits.length() - 2);
            String reais = digits.substring(0, digits.length() - 2);
            reais = reais.replaceFirst("^0+(?!$)", "");
            if (reais.isEmpty()) reais = "0";
            StringBuilder formattedReais = new StringBuilder();
            int count = 0;
            for (int i = reais.length() - 1; i >= 0; i--) {
                formattedReais.insert(0, reais.charAt(i));
                count++;
                if (count % 3 == 0 && i > 0) {
                    formattedReais.insert(0, ".");
                }
            }
            return "R$ " + formattedReais + "," + cents;
        }
    }
}