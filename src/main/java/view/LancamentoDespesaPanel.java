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
    private JTextField txtId;
    private JTextField txtDescricao;
    private JComboBox<Despesa.Categoria> cmbCategoria;
    private JTextField txtValor;
    private JComboBox<Despesa.FormaPagamento> cmbFormaPagamento;
    private JTextField txtDataVencimento;
    private JComboBox<String> cmbMetodoPagamento;
    private JSpinner spinnerParcelas;
    private JCheckBox chkPago;
    private JTextField txtDataPagamento;
    private JLabel lblValorTotal;
    private JTable tabelaDespesas; // Declared as class field
    private DefaultTableModel modeloTabelaDespesas;

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

    // Formato de data
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public LancamentoDespesaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa estado
        listaDespesas = new ArrayList<>();
        valorTotalDespesas = BigDecimal.ZERO;

        // Inicializa componentes de pagamento
        cmbMetodoPagamento = new JComboBox<>(new String[]{"À vista", "Parcelado"});
        cmbMetodoPagamento.setPreferredSize(new Dimension(120, 25));
        cmbMetodoPagamento.setFont(fieldFont);
        cmbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerParcelas.setPreferredSize(new Dimension(80, 25));
        spinnerParcelas.setFont(fieldFont);
        spinnerParcelas.setEnabled(false);

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
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.45));
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

        JLabel lblId = new JLabel("ID:");
        lblId.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 1;
        gbcData.gridwidth = 1;
        gbcData.weightx = 0.0;
        dataPanel.add(lblId, gbcData);

        txtId = new JTextField(5);
        txtId.setEditable(false);
        txtId.setBackground(Color.WHITE);
        txtId.setPreferredSize(new Dimension(80, 25));
        txtId.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtId, gbcData);

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

        JLabel lblCategoria = new JLabel("Categoria:");
        lblCategoria.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 3;
        gbcData.weightx = 0.0;
        dataPanel.add(lblCategoria, gbcData);

        cmbCategoria = new JComboBox<>(Despesa.Categoria.values());
        cmbCategoria.setPreferredSize(new Dimension(150, 25));
        cmbCategoria.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(cmbCategoria, gbcData);

        JLabel lblValor = new JLabel("Valor:");
        lblValor.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 4;
        gbcData.weightx = 0.0;
        dataPanel.add(lblValor, gbcData);

        txtValor = new JTextField(10);
        txtValor.setText("0,00");
        txtValor.setPreferredSize(new Dimension(100, 25));
        txtValor.setFont(fieldFont);
        ((AbstractDocument) txtValor.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtValor, gbcData);

        JLabel lblFormaPagamento = new JLabel("Forma Pagamento:");
        lblFormaPagamento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 5;
        gbcData.weightx = 0.0;
        dataPanel.add(lblFormaPagamento, gbcData);

        cmbFormaPagamento = new JComboBox<>(Despesa.FormaPagamento.values());
        cmbFormaPagamento.setPreferredSize(new Dimension(150, 25));
        cmbFormaPagamento.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(cmbFormaPagamento, gbcData);

        JLabel lblDataVencimento = new JLabel("Data Vencimento:");
        lblDataVencimento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 6;
        gbcData.weightx = 0.0;
        dataPanel.add(lblDataVencimento, gbcData);

        txtDataVencimento = new JTextField(10);
        txtDataVencimento.setPreferredSize(new Dimension(100, 25));
        txtDataVencimento.setFont(fieldFont);
        txtDataVencimento.setToolTipText("Digite a data de vencimento (dd/MM/yyyy)");
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtDataVencimento, gbcData);

        JLabel lblMetodoPagamento = new JLabel("Método Pagamento:");
        lblMetodoPagamento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 7;
        gbcData.weightx = 0.0;
        dataPanel.add(lblMetodoPagamento, gbcData);

        cmbMetodoPagamento.setPreferredSize(new Dimension(150, 25));
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(cmbMetodoPagamento, gbcData);

        JLabel lblParcelas = new JLabel("Parcelas:");
        lblParcelas.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 8;
        gbcData.weightx = 0.0;
        dataPanel.add(lblParcelas, gbcData);

        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(spinnerParcelas, gbcData);

        JLabel lblPago = new JLabel("Pago?:");
        lblPago.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 9;
        gbcData.weightx = 0.0;
        dataPanel.add(lblPago, gbcData);

        chkPago = new JCheckBox();
        chkPago.setBackground(backgroundColor);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(chkPago, gbcData);

        JLabel lblDataPagamento = new JLabel("Data Pagamento:");
        lblDataPagamento.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 10;
        gbcData.weightx = 0.0;
        dataPanel.add(lblDataPagamento, gbcData);

        txtDataPagamento = new JTextField(10);
        txtDataPagamento.setPreferredSize(new Dimension(100, 25));
        txtDataPagamento.setFont(fieldFont);
        txtDataPagamento.setEnabled(false);
        txtDataPagamento.setToolTipText("Digite a data de pagamento (dd/MM/yyyy)");
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtDataPagamento, gbcData);

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
        cmbMetodoPagamento.addActionListener(e -> atualizarParcelas());
        chkPago.addActionListener(e -> txtDataPagamento.setEnabled(chkPago.isSelected() && "À vista".equals(cmbMetodoPagamento.getSelectedItem())));

        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                        "Despesas Pendentes e Pagas (Últimas 48h)", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

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
                if (isRowSelected(row)) {
                    c.setBackground(secondaryColor);
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1,
                            column == getColumnCount() - 1 ? 1 : 0, Color.BLACK));
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }
                return c;
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

    private void atualizarParcelas() {
        String metodo = (String) cmbMetodoPagamento.getSelectedItem();
        if ("Parcelado".equals(metodo)) {
            spinnerParcelas.setModel(new SpinnerNumberModel(2, 2, 12, 1));
            spinnerParcelas.setEnabled(true);
            chkPago.setSelected(false);
            chkPago.setEnabled(false);
            txtDataPagamento.setEnabled(false);
        } else {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 1, 1));
            spinnerParcelas.setEnabled(false);
            chkPago.setEnabled(true);
            txtDataPagamento.setEnabled(chkPago.isSelected());
        }
    }

    private void limparCampos() {
        txtId.setText("");
        txtDescricao.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtValor.setText("0,00");
        cmbFormaPagamento.setSelectedIndex(0);
        txtDataVencimento.setText("");
        cmbMetodoPagamento.setSelectedIndex(0);
        spinnerParcelas.setValue(1);
        chkPago.setSelected(false);
        txtDataPagamento.setText("");
        atualizarParcelas();
    }

    private void adicionarDespesa() {
        try {
            String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
            String descricao = txtDescricao.getText().trim();
            if (descricao.isEmpty()) {
                throw new IllegalArgumentException("Descrição é obrigatória.");
            }
            BigDecimal valorTotal;
            try {
                String text = txtValor.getText().replace(".", "").replace(",", ".");
                valorTotal = new BigDecimal(text);
                if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Valor deve ser maior que zero.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor inválido.");
            }
            LocalDate dataVencimento = parseData(txtDataVencimento.getText());
            boolean isParcelado = "Parcelado".equals(cmbMetodoPagamento.getSelectedItem());
            int numParcelas = (Integer) spinnerParcelas.getValue();

            if (isParcelado) {
                lancarDespesasParceladas(valorTotal, numParcelas, dataVencimento, usuarioLogado);
            } else {
                lancarDespesaUnica(valorTotal, dataVencimento, usuarioLogado);
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

    private void lancarDespesaUnica(BigDecimal valorTotal, LocalDate dataVencimento, String usuarioLogado) throws SQLException {
        Despesa d = new Despesa();
        d.setDescricao(txtDescricao.getText());
        d.setCategoria((Despesa.Categoria) cmbCategoria.getSelectedItem());
        d.setValor(valorTotal);
        d.setFormaPagamento((Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem());
        d.setDataVencimento(dataVencimento);

        if (chkPago.isSelected()) {
            try {
				d.setDataPagamento(parseData(txtDataPagamento.getText()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            d.setStatus(Despesa.Status.PAGO);
        } else {
            d.setDataPagamento(null);
            d.setStatus(Despesa.Status.PENDENTE);
        }

        despesaController.adicionar(d, usuarioLogado);

        if (d.getStatus() == Despesa.Status.PAGO) {
            registrarMovimentoCaixa(d, usuarioLogado);
        }

        listaDespesas.add(d);
        atualizarTabelaDespesas();
    }

    private void lancarDespesasParceladas(BigDecimal valorTotal, int numParcelas, LocalDate dataVencimentoInicial, String usuarioLogado) throws SQLException {
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
            parcela.setStatus(Despesa.Status.PENDENTE); // Parcelas sempre pendentes no lançamento

            despesaController.adicionar(parcela, usuarioLogado);
            listaDespesas.add(parcela);
        }
        atualizarTabelaDespesas();
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
            case DINHEIRO: return CaixaMovimento.FormaPagamento.DINHEIRO;
            case DEBITO: return CaixaMovimento.FormaPagamento.DEBITO;
            case CREDITO: return CaixaMovimento.FormaPagamento.CREDITO;
            case PIX: return CaixaMovimento.FormaPagamento.PIX;
            case BOLETO: return CaixaMovimento.FormaPagamento.BOLETO;
            default: throw new IllegalArgumentException("Forma de pagamento não suportada: " + forma);
        }
    }

    private void carregarDespesasFiltradas() {
        try {
            List<Despesa> todas = despesaController.listarTodas();
            listaDespesas.clear();
            valorTotalDespesas = BigDecimal.ZERO;
            LocalDateTime agora = LocalDateTime.now();
            for (Despesa d : todas) {
                if (d.getStatus() == Despesa.Status.PENDENTE ||
                    (d.getStatus() == Despesa.Status.PAGO && d.getDataHora() != null &&
                     ChronoUnit.HOURS.between(d.getDataHora(), agora) <= 48)) {
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
                    String.format("R$ %.2f", d.getValor()),
                    d.getFormaPagamento(),
                    d.getDataVencimento(),
                    d.getDataPagamento(),
                    d.getStatus()
            });
        }
        lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalDespesas));
    }

    private LocalDate parseData(String dataStr) throws ParseException {
        if (dataStr.trim().isEmpty()) {
            throw new ParseException("Data não informada.", 0);
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
            return text.matches("[0-9,.]*");
        }

        private String removeNonDigits(String text) {
            return text.replaceAll("[^0-9]", "");
        }

        private String formatCurrency(String digits) {
            if (digits.isEmpty()) return "0,00";
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
            return formattedReais + "," + cents;
        }
    }
}