package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.EstoqueController;
import controller.MovimentoEstoqueController;
import controller.PacienteController;
import controller.PagamentoVendaController;
import controller.ProdutoController;
import controller.VendaController;
import controller.VendaProdutoController;
import model.Caixa;
import model.CaixaMovimento;
import model.Estoque;
import model.MovimentoEstoque;
import model.Paciente;
import model.PagamentoVenda;
import model.Produto;
import model.Venda;
import model.VendaProduto;
import util.Sessao;

// Painel para registro de vendas de produtos com suporte a múltiplos itens
public class VendaProdutoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes do formulário
    private JTextField txtBuscaPaciente;
    private JTextField txtBuscaProduto;
    private JLabel lblNomePaciente;
    private JLabel lblTelefone;
    private JLabel lblIdade;
    private JLabel lblEmail;
    private JLabel lblNomeProduto;
    private JLabel lblEstoque;
    private JSpinner spinnerQuantidade;
    private JTextField txtPrecoUnitario;
    private JComboBox<String> cbMetodoPagamento;
    private JSpinner spinnerParcelas;
    private JTextArea txtObservacoes;
    // Componentes da tabela de itens da venda atual
    private JTable tabelaItensVenda;
    private DefaultTableModel modeloTabelaItens;
    // Componentes da tabela de histórico (removida para outra tela futura)
    private JTable tabelaVendas;
    private DefaultTableModel modeloTabelaVendas;

    // Estilo
    private final Color primaryColor = new Color(34, 139, 34); // Verde
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    // Controladores
    private final PacienteController pacienteController = new PacienteController();
    private final ProdutoController produtoController = new ProdutoController();
    private final VendaController vendaController = new VendaController();
    private final VendaProdutoController vendaProdutoController = new VendaProdutoController();
    private final EstoqueController estoqueController = new EstoqueController();
    private final MovimentoEstoqueController movimentoEstoqueController = new MovimentoEstoqueController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController caixaMovimentoController = new CaixaMovimentoController();
    private final PagamentoVendaController pagamentoVendaController = new PagamentoVendaController();

    // Variáveis de estado
    private Paciente pacienteSelecionado;
    private Produto produtoSelecionado;
    private List<VendaProduto> itensVendaAtual; // Lista de itens da venda atual
    private BigDecimal valorTotalVenda; // Valor total da venda atual

    // Formas de pagamento disponíveis
    private static final String[] FORMAS_PAGAMENTO = {"DINHEIRO", "PIX", "DEBITO", "CREDITO", "BOLETO"};

    public VendaProdutoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        // Inicializa lista de itens da venda atual
        itensVendaAtual = new ArrayList<>();
        valorTotalVenda = BigDecimal.ZERO;

        // Título
        JLabel lblTitulo = new JLabel("Venda de Produtos", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de formulário e tabela
        JPanel painelFormulario = criarPainelFormulario();
        JPanel painelTabela = criarPainelTabela();

        // Configura o JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelFormulario, painelTabela);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.4));
        add(splitPane, BorderLayout.CENTER);
    }

    // Cria o painel de formulário para registrar vendas
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Registrar Venda", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Row 1: Busca Paciente
        JLabel lblBuscaPaciente = new JLabel("Buscar Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        txtBuscaPaciente = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        mainGrid.add(lblBuscaPaciente, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainGrid.add(txtBuscaPaciente, gbc);

        // Row 2: Busca Produto
        JLabel lblBuscaProduto = new JLabel("Buscar Produto:");
        lblBuscaProduto.setFont(labelFont);
        txtBuscaProduto = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        mainGrid.add(lblBuscaProduto, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainGrid.add(txtBuscaProduto, gbc);

        // Row 3: Dados Paciente e Produto
        JPanel dataSection = new JPanel(new GridBagLayout());
        dataSection.setBackground(backgroundColor);
        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.insets = new Insets(10, 10, 10, 10);
        gbcS.fill = GridBagConstraints.BOTH;
        gbcS.weightx = 1.0;
        gbcS.weighty = 1.0;

        // Seção Paciente
        JPanel pacientePanel = new JPanel(new GridBagLayout());
        pacientePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                "Dados do Paciente", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor));
        pacientePanel.setBackground(backgroundColor);
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 5, 5, 5);
        gbcP.anchor = GridBagConstraints.WEST;
        gbcP.fill = GridBagConstraints.HORIZONTAL;

        lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNomePaciente.setPreferredSize(new Dimension(300, 20));
        lblNomePaciente.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridx = 0;
        gbcP.gridy = 0;
        pacientePanel.add(lblNomePaciente, gbcP);

        lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        lblTelefone.setPreferredSize(new Dimension(300, 20));
        lblTelefone.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 1;
        pacientePanel.add(lblTelefone, gbcP);

        lblIdade = new JLabel("Idade:");
        lblIdade.setFont(labelFont);
        lblIdade.setPreferredSize(new Dimension(300, 20));
        lblIdade.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 2;
        pacientePanel.add(lblIdade, gbcP);

        lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        lblEmail.setPreferredSize(new Dimension(300, 20));
        lblEmail.setHorizontalAlignment(SwingConstants.LEFT);
        gbcP.gridy = 3;
        pacientePanel.add(lblEmail, gbcP);

        gbcS.gridx = 0;
        gbcS.gridy = 0;
        dataSection.add(pacientePanel, gbcS);

        // Seção Produto
        JPanel produtoPanel = new JPanel(new GridBagLayout());
        produtoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                "Dados do Produto", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor));
        produtoPanel.setBackground(backgroundColor);
        GridBagConstraints gbcProd = new GridBagConstraints();
        gbcProd.insets = new Insets(5, 5, 5, 5);
        gbcProd.anchor = GridBagConstraints.WEST;
        gbcProd.fill = GridBagConstraints.HORIZONTAL;

        lblNomeProduto = new JLabel("Produto:");
        lblNomeProduto.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNomeProduto.setPreferredSize(new Dimension(300, 20));
        lblNomeProduto.setHorizontalAlignment(SwingConstants.LEFT);
        gbcProd.gridx = 0;
        gbcProd.gridy = 0;
        produtoPanel.add(lblNomeProduto, gbcProd);

        lblEstoque = new JLabel("Estoque Disponível:");
        lblEstoque.setFont(labelFont);
        lblEstoque.setPreferredSize(new Dimension(300, 20));
        lblEstoque.setHorizontalAlignment(SwingConstants.LEFT);
        gbcProd.gridy = 1;
        produtoPanel.add(lblEstoque, gbcProd);

        JLabel lblQuantidade = new JLabel("Quantidade:");
        lblQuantidade.setFont(labelFont);
        gbcProd.gridy = 2;
        produtoPanel.add(lblQuantidade, gbcProd);
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinnerQuantidade.setPreferredSize(new Dimension(100, 30));
        gbcProd.gridx = 1;
        produtoPanel.add(spinnerQuantidade, gbcProd);

        JLabel lblPreco = new JLabel("Preço Unitário (R$):");
        lblPreco.setFont(labelFont);
        gbcProd.gridx = 0;
        gbcProd.gridy = 3;
        produtoPanel.add(lblPreco, gbcProd);
        txtPrecoUnitario = new JTextField(20);
        txtPrecoUnitario.setText("0,00");
        gbcProd.gridx = 1;
        produtoPanel.add(txtPrecoUnitario, gbcProd);

        // Botão para adicionar item à venda atual
        JButton btnAdicionarItem = new JButton("Adicionar Item");
        btnAdicionarItem.setBackground(primaryColor);
        btnAdicionarItem.setForeground(Color.WHITE);
        btnAdicionarItem.setPreferredSize(new Dimension(120, 30));
        btnAdicionarItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcProd.gridx = 0;
        gbcProd.gridy = 4;
        gbcProd.gridwidth = 2;
        gbcProd.anchor = GridBagConstraints.CENTER;
        produtoPanel.add(btnAdicionarItem, gbcProd);

        // Adiciona filtro para formatação automática de valores
        ((AbstractDocument) txtPrecoUnitario.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());

        gbcS.gridx = 0;
        gbcS.gridy = 1;
        dataSection.add(produtoPanel, gbcS);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weighty = 0.3;
        mainGrid.add(dataSection, gbc);

        // Row 4: Pagamento
        JPanel pagamentoPanel = new JPanel(new GridBagLayout());
        pagamentoPanel.setBackground(backgroundColor);
        GridBagConstraints gbcPag = new GridBagConstraints();
        gbcPag.insets = new Insets(5, 5, 5, 5);
        gbcPag.fill = GridBagConstraints.HORIZONTAL;
        gbcPag.weightx = 1.0;

        JLabel lblMetodo = new JLabel("Método Pagamento:");
        lblMetodo.setFont(labelFont);
        gbcPag.gridx = 0;
        gbcPag.gridy = 0;
        gbcPag.weightx = 0.0;
        pagamentoPanel.add(lblMetodo, gbcPag);
        cbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO);
        cbMetodoPagamento.setPreferredSize(new Dimension(150, 30));
        cbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcPag.gridx = 1;
        gbcPag.weightx = 1.0;
        pagamentoPanel.add(cbMetodoPagamento, gbcPag);

        JLabel lblParcelas = new JLabel("Parcelas:");
        lblParcelas.setFont(labelFont);
        gbcPag.gridx = 2;
        gbcPag.weightx = 0.0;
        pagamentoPanel.add(lblParcelas, gbcPag);
        spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerParcelas.setPreferredSize(new Dimension(100, 30));
        spinnerParcelas.setEnabled(false);
        gbcPag.gridx = 3;
        gbcPag.weightx = 1.0;
        pagamentoPanel.add(spinnerParcelas, gbcPag);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        mainGrid.add(pagamentoPanel, gbc);

        // Row 5: Observações
        JPanel observacaoPanel = new JPanel(new BorderLayout());
        observacaoPanel.setBackground(backgroundColor);
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        lblObservacoes.setBorder(new EmptyBorder(0, 0, 5, 0));
        observacaoPanel.add(lblObservacoes, BorderLayout.NORTH);
        txtObservacoes = new JTextArea(4, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        observacaoPanel.add(scrollObservacoes, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weighty = 0.2;
        mainGrid.add(observacaoPanel, gbc);

        // Row 6: Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBackground(backgroundColor);
        JButton btnRealizarVenda = new JButton("Realizar Venda");
        btnRealizarVenda.setBackground(primaryColor);
        btnRealizarVenda.setForeground(Color.WHITE);
        btnRealizarVenda.setPreferredSize(new Dimension(120, 35));
        btnRealizarVenda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnRealizarVenda);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        mainGrid.add(panelBotoes, gbc);

        // Listeners
        btnAdicionarItem.addActionListener(e -> adicionarItemVenda());
        btnRealizarVenda.addActionListener(e -> realizarVenda());
        btnLimpar.addActionListener(e -> limparCampos());
        txtBuscaPaciente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarPaciente(); }
            public void removeUpdate(DocumentEvent e) { atualizarPaciente(); }
            public void changedUpdate(DocumentEvent e) { atualizarPaciente(); }
        });
        txtBuscaProduto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarProduto(); }
            public void removeUpdate(DocumentEvent e) { atualizarProduto(); }
            public void changedUpdate(DocumentEvent e) { atualizarProduto(); }
        });
        cbMetodoPagamento.addActionListener(e -> atualizarParcelas());

        panel.add(mainGrid, BorderLayout.CENTER);
        return panel;
    }

    // Cria o painel da tabela de itens da venda atual
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Itens da Venda Atual", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        // Configuração da tabela de itens
        String[] colunas = {"Produto", "Quantidade", "Preço Unitário", "Subtotal", "Garantia (meses)"};
        modeloTabelaItens = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaItensVenda = new JTable(modeloTabelaItens) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? backgroundColor : new Color(230, 230, 230));
                c.setForeground(Color.BLACK);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(144, 238, 144));
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1, column == getColumnCount() - 1 ? 1 : 0, Color.BLACK));
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }
                return c;
            }
        };

        tabelaItensVenda.setShowGrid(false);
        tabelaItensVenda.setIntercellSpacing(new Dimension(0, 0));
        tabelaItensVenda.setFillsViewportHeight(true);
        tabelaItensVenda.setRowHeight(25);
        tabelaItensVenda.setFont(labelFont);
        tabelaItensVenda.setBackground(backgroundColor);

        JTableHeader header = tabelaItensVenda.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaItensVenda.getColumnCount(); i++) {
            tabelaItensVenda.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(tabelaItensVenda);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);

        // Label para valor total
        JLabel lblValorTotal = new JLabel("Valor Total: R$ 0,00");
        lblValorTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValorTotal.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(lblValorTotal, BorderLayout.SOUTH);

        return panel;
    }

    // Atualiza os dados do paciente com base na busca
    private void atualizarPaciente() {
        String busca = txtBuscaPaciente.getText().toLowerCase();
        pacienteSelecionado = null;

        try {
            for (Paciente p : pacienteController.listarTodos()) {
                if (p.getNome().toLowerCase().contains(busca)) {
                    if (pacienteSelecionado == null || p.getId() > pacienteSelecionado.getId()) {
                        pacienteSelecionado = p;
                    }
                }
            }

            if (pacienteSelecionado != null) {
                lblNomePaciente.setText("Nome: " + pacienteSelecionado.getNome());
                lblTelefone.setText("Telefone: " + (pacienteSelecionado.getTelefone() != null ? pacienteSelecionado.getTelefone() : "N/A"));
                long idade = pacienteSelecionado.getDataNascimento() != null
                        ? java.time.temporal.ChronoUnit.YEARS.between(pacienteSelecionado.getDataNascimento(), LocalDate.now())
                        : 0;
                lblIdade.setText("Idade: " + idade);
                lblEmail.setText("Email: " + (pacienteSelecionado.getEmail() != null ? pacienteSelecionado.getEmail() : "N/A"));
            } else {
                lblNomePaciente.setText("Nome:");
                lblTelefone.setText("Telefone:");
                lblIdade.setText("Idade:");
                lblEmail.setText("Email:");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar paciente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza os dados do produto com base na busca
    private void atualizarProduto() {
        String busca = txtBuscaProduto.getText().toLowerCase();
        produtoSelecionado = null;

        try {
            for (Produto p : produtoController.listarTodos()) {
                if (p.getNome().toLowerCase().contains(busca)) {
                    if (produtoSelecionado == null || p.getId() > produtoSelecionado.getId()) {
                        produtoSelecionado = p;
                    }
                }
            }

            if (produtoSelecionado != null) {
                lblNomeProduto.setText("Produto: " + produtoSelecionado.getNome());
                Estoque estoque = estoqueController.buscarPorProdutoId(produtoSelecionado.getId());
                lblEstoque.setText("Estoque Disponível: " + (estoque != null ? estoque.getQuantidade() : 0));
            } else {
                lblNomeProduto.setText("Produto:");
                lblEstoque.setText("Estoque Disponível:");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza opções de parcelas com base no método de pagamento
    private void atualizarParcelas() {
        String metodo = (String) cbMetodoPagamento.getSelectedItem();
        if ("CREDITO".equals(metodo) || "BOLETO".equals(metodo)) {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 12, 1));
            spinnerParcelas.setEnabled(true);
        } else {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 1, 1));
            spinnerParcelas.setEnabled(false);
        }
    }

    // Adiciona um item à venda atual
    private void adicionarItemVenda() {
        try {
            // Validações
            if (produtoSelecionado == null) {
                throw new IllegalArgumentException("Selecione um produto!");
            }
            int quantidade = (Integer) spinnerQuantidade.getValue();
            BigDecimal precoUnitario;
            try {
                String text = txtPrecoUnitario.getText().replace(".", "").replace(",", ".");
                precoUnitario = new BigDecimal(text);
                if (precoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Preço unitário deve ser maior que zero!");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Preço unitário inválido!");
            }

            // Verifica estoque
            Estoque estoque = estoqueController.buscarPorProdutoId(produtoSelecionado.getId());
            if (estoque == null || estoque.getQuantidade() < quantidade) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto!");
            }

            // Cria o item da venda
            VendaProduto vendaProduto = new VendaProduto();
            vendaProduto.setProdutoId(produtoSelecionado.getId());
            vendaProduto.setQuantidade(quantidade);
            vendaProduto.setPrecoUnitario(precoUnitario);
            vendaProduto.setGarantiaMeses(produtoSelecionado.getGarantiaMeses());
            LocalDate dataVenda = LocalDate.now();
            vendaProduto.setDataVenda(Timestamp.valueOf(dataVenda.atStartOfDay()));
            vendaProduto.setFimGarantia(Date.valueOf(dataVenda.plusMonths(produtoSelecionado.getGarantiaMeses())));

            // Adiciona à lista de itens da venda atual
            itensVendaAtual.add(vendaProduto);

            // Atualiza tabela de itens
            atualizarTabelaItens();

            // Limpa campos do produto
            txtBuscaProduto.setText("");
            spinnerQuantidade.setValue(1);
            txtPrecoUnitario.setText("0,00");
            produtoSelecionado = null;
            atualizarProduto();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar item: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza a tabela de itens da venda atual
    private void atualizarTabelaItens() {
        modeloTabelaItens.setRowCount(0);
        valorTotalVenda = BigDecimal.ZERO;

        for (VendaProduto vp : itensVendaAtual) {
            try {
                Produto p = produtoController.buscarPorId(vp.getProdutoId());
                BigDecimal subtotal = vp.getPrecoUnitario().multiply(BigDecimal.valueOf(vp.getQuantidade()));
                valorTotalVenda = valorTotalVenda.add(subtotal);
                modeloTabelaItens.addRow(new Object[]{
                        p.getNome(),
                        vp.getQuantidade(),
                        String.format("R$ %.2f", vp.getPrecoUnitario()),
                        String.format("R$ %.2f", subtotal),
                        vp.getGarantiaMeses()
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Atualiza label do valor total
        JLabel lblValorTotal = (JLabel) ((JPanel) tabelaItensVenda.getParent().getParent()).getComponent(1);
        lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalVenda));
    }

    // Realiza a venda, atualizando estoque, movimentos e caixa
    private void realizarVenda() {
        try {
            // Validações
            if (pacienteSelecionado == null) {
                throw new IllegalArgumentException("Selecione um paciente!");
            }
            if (itensVendaAtual.isEmpty()) {
                throw new IllegalArgumentException("Adicione pelo menos um produto à venda!");
            }
            int parcelas = (Integer) spinnerParcelas.getValue();
            String metodo = (String) cbMetodoPagamento.getSelectedItem();

            // Verifica caixa aberto
            Caixa caixa = caixaController.buscarCaixaAberto();
            if (caixa == null) {
                throw new IllegalStateException("Nenhum caixa aberto encontrado!");
            }

            // Cria a venda
            Venda venda = new Venda();
            venda.setValorTotal(valorTotalVenda);
            venda.setUsuario(Sessao.getUsuarioLogado().getLogin());
            venda.setDataHora(Timestamp.valueOf(LocalDateTime.now()));

            // Registra a venda
            if (!vendaController.registrarVenda(venda, Sessao.getUsuarioLogado().getLogin())) {
                throw new SQLException("Falha ao registrar venda!");
            }

            // Obtém o ID da venda
            int vendaId = venda.getId();

            // Registra os produtos da venda e atualiza estoque
            for (VendaProduto vp : itensVendaAtual) {
                vp.setVendaId(vendaId);
                if (!vendaProdutoController.adicionarProdutoVenda(vp)) {
                    throw new SQLException("Falha ao registrar produto da venda!");
                }

                // Atualiza estoque
                Estoque estoque = estoqueController.buscarPorProdutoId(vp.getProdutoId());
                estoque.setQuantidade(estoque.getQuantidade() - vp.getQuantidade());
                estoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!estoqueController.salvarOuAtualizarEstoque(estoque, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao atualizar estoque!");
                }

                // Registra movimento de estoque
                MovimentoEstoque movimentoEstoque = new MovimentoEstoque();
                movimentoEstoque.setProdutoId(vp.getProdutoId());
                movimentoEstoque.setQuantidade(vp.getQuantidade());
                movimentoEstoque.setTipo(MovimentoEstoque.Tipo.SAIDA);
                movimentoEstoque.setObservacoes("Saída por venda ID " + vendaId);
                movimentoEstoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!movimentoEstoqueController.registrarMovimento(movimentoEstoque, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao registrar movimento de estoque!");
                }
            }

            // Registra pagamento(s)
            BigDecimal valorParcela = valorTotalVenda.divide(BigDecimal.valueOf(parcelas), 2, BigDecimal.ROUND_HALF_UP);
            for (int i = 1; i <= parcelas; i++) {
                if (i == 1 && ("DINHEIRO".equals(metodo) || "PIX".equals(metodo) || "DEBITO".equals(metodo) || parcelas == 1)) {
                    PagamentoVenda pagamento = new PagamentoVenda();
                    pagamento.setVenda(venda);
                    pagamento.setValor(valorTotalVenda);
                    pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(metodo));
                    pagamento.setParcela(i);
                    pagamento.setTotalParcelas(parcelas);
                    pagamento.setObservacoes(txtObservacoes.getText());
                    pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    pagamento.setDataHora(LocalDateTime.now());
                    pagamentoVendaController.inserir(pagamento);

                    // Registra movimento no caixa
                    CaixaMovimento movimentoCaixa = new CaixaMovimento();
                    movimentoCaixa.setCaixa(caixa);
                    movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
                    movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
                    movimentoCaixa.setPagamentoVenda(pagamento);
                    movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(metodo));
                    movimentoCaixa.setValor(valorTotalVenda);
                    movimentoCaixa.setDescricao("Pagamento " + (parcelas == 1 ? "à vista" : "parcela " + i + "/" + parcelas) + " de venda ID " + vendaId);
                    movimentoCaixa.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    movimentoCaixa.setDataHora(LocalDateTime.now());
                    caixaMovimentoController.adicionarMovimento(movimentoCaixa);
                } else if (i == 1) {
                    PagamentoVenda pagamento = new PagamentoVenda();
                    pagamento.setVenda(venda);
                    pagamento.setValor(valorParcela);
                    pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(metodo));
                    pagamento.setParcela(i);
                    pagamento.setTotalParcelas(parcelas);
                    pagamento.setObservacoes(txtObservacoes.getText());
                    pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    pagamento.setDataHora(LocalDateTime.now());
                    pagamentoVendaController.inserir(pagamento);

                    // Registra movimento no caixa
                    CaixaMovimento movimentoCaixa = new CaixaMovimento();
                    movimentoCaixa.setCaixa(caixa);
                    movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
                    movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
                    movimentoCaixa.setPagamentoVenda(pagamento);
                    movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(metodo));
                    movimentoCaixa.setValor(valorParcela);
                    movimentoCaixa.setDescricao("Pagamento parcela " + i + "/" + parcelas + " de venda ID " + vendaId);
                    movimentoCaixa.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    movimentoCaixa.setDataHora(LocalDateTime.now());
                    caixaMovimentoController.adicionarMovimento(movimentoCaixa);
                } else {
                    PagamentoVenda pagamento = new PagamentoVenda();
                    pagamento.setVenda(venda);
                    pagamento.setValor(valorParcela);
                    pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(metodo));
                    pagamento.setParcela(i);
                    pagamento.setTotalParcelas(parcelas);
                    pagamento.setObservacoes(txtObservacoes.getText());
                    pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    pagamento.setDataHora(LocalDateTime.now());
                    pagamentoVendaController.inserir(pagamento);
                }
            }

            JOptionPane.showMessageDialog(this, "Venda realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao realizar venda: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Limpa os campos do formulário e a lista de itens
    private void limparCampos() {
        txtBuscaPaciente.setText("");
        txtBuscaProduto.setText("");
        spinnerQuantidade.setValue(1);
        txtPrecoUnitario.setText("0,00");
        cbMetodoPagamento.setSelectedIndex(0);
        spinnerParcelas.setValue(1);
        spinnerParcelas.setEnabled(false);
        txtObservacoes.setText("");
        pacienteSelecionado = null;
        produtoSelecionado = null;
        itensVendaAtual.clear();
        atualizarPaciente();
        atualizarProduto();
        atualizarTabelaItens();
    }

    // Filtro para formatar entrada de valores monetários
    private class CurrencyDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, string);
            if (isValidInput(sb.toString())) {
                String formatted = formatCurrency(removeNonDigits(sb.toString()));
                super.replace(fb, 0, fb.getDocument().getLength(), formatted, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
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