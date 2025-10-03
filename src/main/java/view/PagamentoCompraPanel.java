package view;

import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.CompraController;
import controller.CompraProdutoController;
import controller.EstoqueController;
import controller.MovimentoEstoqueController;
import controller.PagamentoCompraController;
import controller.ProdutoController;
import model.Caixa;
import model.CaixaMovimento;
import model.CompraProduto;
import model.Estoque;
import model.MovimentoEstoque;
import model.PagamentoCompra;
import model.Produto;
import util.Sessao;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Painel para gerenciamento de pagamentos de compras pendentes (apenas BOLETO).
 */
public class PagamentoCompraPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes do formulário
    private JTextField txtBuscaProdutoNome;
    private JTable tabelaPagamentos;
    private DefaultTableModel modeloTabelaPagamentos;
    private JLabel lblValorTotal;

    // Componentes de filtro
    private JDateChooser dateChooserInicioFiltro;
    private JDateChooser dateChooserFimFiltro;

    // Estilo
    private final Color primaryColor = new Color(154, 5, 38);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color rowColorLightGreen = new Color(230, 255, 230);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final PagamentoCompraController pagamentoController = new PagamentoCompraController();
    private final CompraController compraController = new CompraController();
    private final CompraProdutoController compraProdutoController = new CompraProdutoController();
    private final ProdutoController produtoController = new ProdutoController();
    private final EstoqueController estoqueController = new EstoqueController();
    private final MovimentoEstoqueController movimentoEstoqueController = new MovimentoEstoqueController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController movimentoController = new CaixaMovimentoController();

    // Variáveis de estado
    private List<PagamentoCompra> listaPagamentos = new ArrayList<>();
    private BigDecimal valorTotalPagamentos = BigDecimal.ZERO;
    private Map<Integer, Produto> cacheProdutos = new HashMap<>();

    // Variáveis de filtro
    private LocalDate dataInicioFiltro;
    private LocalDate dataFimFiltro;
    private String produtoNomeFiltro;

    // Formato de data
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    // Formas de pagamento permitidas para pagar boletos
    private static final PagamentoCompra.MetodoPagamento[] FORMAS_PAGAMENTO_BOLETO = {
        PagamentoCompra.MetodoPagamento.DINHEIRO,
        PagamentoCompra.MetodoPagamento.DEBITO,
        PagamentoCompra.MetodoPagamento.CREDITO
    };

    /**
     * Construtor do painel de pagamentos de compras.
     */
    public PagamentoCompraPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa filtros padrão (mês atual)
        LocalDate now = LocalDate.now();
        dataInicioFiltro = now.withDayOfMonth(1);
        dataFimFiltro = now.withDayOfMonth(now.lengthOfMonth());
        produtoNomeFiltro = "";

        // Título
        JLabel lblTitulo = new JLabel("Pagamentos de Compras (Boletos)", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painel da tabela
        JPanel painelTabela = criarPainelTabela();
        add(painelTabela, BorderLayout.CENTER);

        // Carrega cache de produtos
        carregarCacheProdutos();

        // Carrega dados iniciais
        carregarPagamentosFiltrados();
    }

    /**
     * Carrega o cache de produtos para otimizar a busca por nome.
     */
    private void carregarCacheProdutos() {
        try {
            for (Produto p : produtoController.listarTodos()) {
                cacheProdutos.put(p.getId(), p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cria o painel de tabela com filtros e botões.
     */
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                        "Pagamentos Pendentes (Boletos)", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Painel de filtros
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtrosPanel.setBackground(backgroundColor);
        filtrosPanel.setBorder(new EmptyBorder(5, 0, 10, 0));

        JLabel lblDe = new JLabel("De:");
        lblDe.setFont(labelFont);
        filtrosPanel.add(lblDe);

        dateChooserInicioFiltro = new JDateChooser();
        dateChooserInicioFiltro.setPreferredSize(new Dimension(150, 25));
        dateChooserInicioFiltro.setDateFormatString("dd/MM/yyyy");
        dateChooserInicioFiltro.setFont(fieldFont);
        dateChooserInicioFiltro.setDate(java.util.Date.from(dataInicioFiltro.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        dateChooserInicioFiltro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configurarCampoData((JTextFieldDateEditor) dateChooserInicioFiltro.getDateEditor());
        dateChooserInicioFiltro.addPropertyChangeListener("date", evt -> aplicarFiltros());
        filtrosPanel.add(dateChooserInicioFiltro);

        JLabel lblAte = new JLabel("Até:");
        lblAte.setFont(labelFont);
        filtrosPanel.add(lblAte);

        dateChooserFimFiltro = new JDateChooser();
        dateChooserFimFiltro.setPreferredSize(new Dimension(150, 25));
        dateChooserFimFiltro.setDateFormatString("dd/MM/yyyy");
        dateChooserFimFiltro.setFont(fieldFont);
        dateChooserFimFiltro.setDate(java.util.Date.from(dataFimFiltro.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        dateChooserFimFiltro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configurarCampoData((JTextFieldDateEditor) dateChooserFimFiltro.getDateEditor());
        dateChooserFimFiltro.addPropertyChangeListener("date", evt -> aplicarFiltros());
        filtrosPanel.add(dateChooserFimFiltro);

        JLabel lblProdutoNome = new JLabel("Nome do Produto:");
        lblProdutoNome.setFont(labelFont);
        filtrosPanel.add(lblProdutoNome);

        txtBuscaProdutoNome = new JTextField(20);
        txtBuscaProdutoNome.setPreferredSize(new Dimension(200, 25));
        txtBuscaProdutoNome.setFont(fieldFont);
        txtBuscaProdutoNome.setToolTipText("Digite o nome do produto para filtrar");
        txtBuscaProdutoNome.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        filtrosPanel.add(txtBuscaProdutoNome);

        panel.add(filtrosPanel, BorderLayout.NORTH);

        // Tabela
        String[] colunas = {"Produto", "Valor", "Método Pag.", "Data Venc.", "Parcela", "Status"};
        modeloTabelaPagamentos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaPagamentos = new JTable(modeloTabelaPagamentos) {
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
        tabelaPagamentos.setShowGrid(false);
        tabelaPagamentos.setIntercellSpacing(new Dimension(0, 0));
        tabelaPagamentos.setFillsViewportHeight(true);
        tabelaPagamentos.setRowHeight(25);
        tabelaPagamentos.setFont(fieldFont);
        tabelaPagamentos.setBackground(backgroundColor);

        JTableHeader header = tabelaPagamentos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaPagamentos.getColumnCount(); i++) {
            tabelaPagamentos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(tabelaPagamentos);
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

        JButton btnPagar = new JButton("Pagar Selecionado");
        btnPagar.setBackground(primaryColor);
        btnPagar.setForeground(Color.WHITE);
        btnPagar.setBorder(BorderFactory.createEmptyBorder());
        btnPagar.setPreferredSize(new Dimension(120, 30));
        btnPagar.setHorizontalAlignment(SwingConstants.CENTER);
        btnPagar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPagar.setToolTipText("Pagar boleto selecionado");
        btnPagar.addActionListener(e -> pagarPagamentoSelecionado());
        southPanel.add(btnPagar);

        JButton btnDevolverCompra = new JButton("Devolver Compra");
        btnDevolverCompra.setBackground(new Color(255, 204, 0)); // Amarelo
        btnDevolverCompra.setForeground(Color.BLACK);
        btnDevolverCompra.setBorder(BorderFactory.createEmptyBorder());
        btnDevolverCompra.setPreferredSize(new Dimension(120, 30));
        btnDevolverCompra.setHorizontalAlignment(SwingConstants.CENTER);
        btnDevolverCompra.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDevolverCompra.setToolTipText("Devolver a compra selecionada");
        btnDevolverCompra.addActionListener(e -> devolverCompra());
        southPanel.add(btnDevolverCompra);

        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Configura o campo de data com botão "Hoje".
     */
    private void configurarCampoData(JTextFieldDateEditor editor) {
        editor.setToolTipText("Digite a data no formato dd/MM/yyyy ou selecione no calendário");
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    String text = editor.getText();
                    int len = text.length();
                    if (len == 2 || len == 5) {
                        editor.setText(text + "/");
                        editor.setCaretPosition(editor.getText().length());
                    }
                });
            }
        });
        configurarBotaoHoje((JDateChooser) editor.getParent());
    }

    /**
     * Configura o botão "Hoje" no JDateChooser.
     */
    private void configurarBotaoHoje(JDateChooser dateChooser) {
        dateChooser.addPropertyChangeListener("jcalendar", evt -> {
            if (evt.getNewValue() != null) {
                JPopupMenu popup = dateChooser.getJCalendar().getComponentPopupMenu();
                if (popup != null) {
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    JButton btnHoje = new JButton("Hoje");
                    btnHoje.setFont(fieldFont);
                    btnHoje.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    btnHoje.addActionListener(e -> {
                        dateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
                        popup.setVisible(false);
                    });
                    buttonPanel.add(btnHoje);
                    popup.add(buttonPanel, BorderLayout.SOUTH);
                }
            }
        });
    }

    /**
     * Aplica os filtros para carregar os pagamentos.
     */
    private void aplicarFiltros() {
        try {
            java.util.Date inicioDate = dateChooserInicioFiltro.getDate();
            dataInicioFiltro = inicioDate != null ?
                inicioDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() :
                LocalDate.now().withDayOfMonth(1);

            java.util.Date fimDate = dateChooserFimFiltro.getDate();
            dataFimFiltro = fimDate != null ?
                fimDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() :
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            produtoNomeFiltro = txtBuscaProdutoNome.getText().trim();
            carregarPagamentosFiltrados();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao aplicar filtros: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carrega os pagamentos filtrados (somente pendentes em BOLETO).
     */
    private void carregarPagamentosFiltrados() {
        try {
            List<PagamentoCompra> todos = pagamentoController.listarPagamentosPendentesBoleto();
            listaPagamentos.clear();
            valorTotalPagamentos = BigDecimal.ZERO;

            for (PagamentoCompra p : todos) {
                // Filtra por nome do produto
                boolean matchesProduto = produtoNomeFiltro.isEmpty();
                String produtoNome = "";
                List<CompraProduto> produtosCompra = compraProdutoController.listarPorCompra(p.getCompraId());
                if (!produtosCompra.isEmpty()) {
                    Produto produto = cacheProdutos.get(produtosCompra.get(0).getProdutoId());
                    if (produto == null) {
                        produto = produtoController.buscarPorId(produtosCompra.get(0).getProdutoId());
                        cacheProdutos.put(produto.getId(), produto);
                    }
                    produtoNome = produto.getNome();
                    if (!produtoNomeFiltro.isEmpty() && produtoNome.toLowerCase().contains(produtoNomeFiltro.toLowerCase())) {
                        matchesProduto = true;
                    }
                }
                // Filtra por data de vencimento
                LocalDate dataVencimento = p.getDataVencimento().toLocalDate();
                if (matchesProduto &&
                    (dataInicioFiltro == null || !dataVencimento.isBefore(dataInicioFiltro)) &&
                    (dataFimFiltro == null || !dataVencimento.isAfter(dataFimFiltro))) {
                    listaPagamentos.add(p);
                    valorTotalPagamentos = valorTotalPagamentos.add(p.getValor());
                }
            }
            atualizarTabelaPagamentos();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pagamentos: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Atualiza a tabela de pagamentos.
     */
    private void atualizarTabelaPagamentos() {
        modeloTabelaPagamentos.setRowCount(0);
        for (PagamentoCompra p : listaPagamentos) {
            String produtoNome = "";
            try {
                List<CompraProduto> produtosCompra = compraProdutoController.listarPorCompra(p.getCompraId());
                if (!produtosCompra.isEmpty()) {
                    Produto produto = cacheProdutos.get(produtosCompra.get(0).getProdutoId());
                    if (produto == null) {
                        produto = produtoController.buscarPorId(produtosCompra.get(0).getProdutoId());
                        cacheProdutos.put(produto.getId(), produto);
                    }
                    produtoNome = produto.getNome();
                }
            } catch (SQLException ex) {
                produtoNome = "Erro ao carregar produto";
            }
            String dataVencimentoFormatada = p.getDataVencimento() != null
                    ? sdf.format(p.getDataVencimento())
                    : "";
            modeloTabelaPagamentos.addRow(new Object[]{
                    produtoNome,
                    formatValorTabela(p.getValor()),
                    p.getMetodoPagamento(),
                    dataVencimentoFormatada,
                    p.getParcela() + "/" + p.getTotalParcelas(),
                    p.getStatus()
            });
        }
        lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalPagamentos));
    }

    /**
     * Formata o valor para exibição na tabela.
     */
    private String formatValorTabela(BigDecimal valor) {
        DecimalFormat df = new DecimalFormat("R$ #,##0.00");
        return df.format(valor);
    }

    /**
     * Marca o pagamento selecionado como pago e registra o movimento no caixa.
     */
    private void pagarPagamentoSelecionado() {
        int row = tabelaPagamentos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um boleto para pagar.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PagamentoCompra pagamento = listaPagamentos.get(row);
        if (pagamento.getStatus() == PagamentoCompra.StatusPagamento.PAGO) {
            JOptionPane.showMessageDialog(this, "Boleto já pago.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Popup para registrar o pagamento
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Pagar Boleto");
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Componentes do popup
        JLabel lblMetodoOriginal = new JLabel("Método Original:");
        lblMetodoOriginal.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(lblMetodoOriginal, gbc);

        JTextField txtMetodoOriginal = new JTextField("BOLETO");
        txtMetodoOriginal.setEditable(false);
        txtMetodoOriginal.setFont(fieldFont);
        txtMetodoOriginal.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 1;
        dialog.add(txtMetodoOriginal, gbc);

        JLabel lblMetodoPagamento = new JLabel("Forma de Pagamento:");
        lblMetodoPagamento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(lblMetodoPagamento, gbc);

        JComboBox<PagamentoCompra.MetodoPagamento> cmbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO_BOLETO);
        cmbMetodoPagamento.setFont(fieldFont);
        cmbMetodoPagamento.setPreferredSize(new Dimension(120, 25));
        cmbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão
        gbc.gridx = 1;
        dialog.add(cmbMetodoPagamento, gbc);

        JLabel lblParcela = new JLabel("Parcela:");
        lblParcela.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(lblParcela, gbc);

        JTextField txtParcela = new JTextField(pagamento.getParcela() + "/" + pagamento.getTotalParcelas());
        txtParcela.setEditable(false);
        txtParcela.setFont(fieldFont);
        txtParcela.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        dialog.add(txtParcela, gbc);

        JLabel lblValor = new JLabel("Valor: R$ " + String.format("%.2f", pagamento.getValor()));
        lblValor.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(lblValor, gbc);

        JLabel lblDataPagamento = new JLabel("Data de Pagamento:");
        lblDataPagamento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        dialog.add(lblDataPagamento, gbc);

        JDateChooser dateChooserPagamento = new JDateChooser();
        dateChooserPagamento.setDateFormatString("dd/MM/yyyy");
        dateChooserPagamento.setFont(fieldFont);
        dateChooserPagamento.setDate(java.util.Date.from(LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        dateChooserPagamento.setPreferredSize(new Dimension(150, 25));
        dateChooserPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão
        configurarCampoData((JTextFieldDateEditor) dateChooserPagamento.getDateEditor());
        gbc.gridx = 1;
        dialog.add(dateChooserPagamento, gbc);

        JButton btnConfirmar = new JButton("Confirmar Pagamento");
        btnConfirmar.setBackground(primaryColor);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(150, 30));
        btnConfirmar.setFont(fieldFont);
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(btnConfirmar, gbc);

        // Ação do botão confirmar
        btnConfirmar.addActionListener(e -> {
            try {
                if (!caixaController.existeCaixaAberto()) {
                    JOptionPane.showMessageDialog(dialog, "Não há caixa aberto para registrar o pagamento.", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                java.util.Date selectedDate = dateChooserPagamento.getDate();
                if (selectedDate == null) {
                    JOptionPane.showMessageDialog(dialog, "Data de pagamento inválida.", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate dataPagamento = selectedDate.toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                PagamentoCompra.MetodoPagamento metodoPagamento = (PagamentoCompra.MetodoPagamento) cmbMetodoPagamento.getSelectedItem();
                String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
                PagamentoCompra updatedPagamento = new PagamentoCompra(
                        pagamento.getId(), pagamento.getCompraId(), Timestamp.valueOf(LocalDateTime.now()),
                        pagamento.getDataVencimento(), pagamento.getValor(),
                        pagamento.getMetodoPagamento(), pagamento.getParcela(),
                        pagamento.getTotalParcelas(), PagamentoCompra.StatusPagamento.PAGO,
                        pagamento.getObservacoes(), usuarioLogado
                );
                pagamentoController.atualizarPagamento(updatedPagamento, usuarioLogado);
                registrarMovimentoCaixa(updatedPagamento, usuarioLogado, dataPagamento, metodoPagamento);
                JOptionPane.showMessageDialog(dialog, "Pagamento realizado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarPagamentosFiltrados();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao pagar: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Registra o movimento no caixa para o pagamento.
     */
    private void registrarMovimentoCaixa(PagamentoCompra pagamento, String usuarioLogado, LocalDate dataPagamento, PagamentoCompra.MetodoPagamento metodoPagamento) throws SQLException {
        Caixa caixaAberto = caixaController.getCaixaAberto();
        CaixaMovimento movimento = new CaixaMovimento();
        movimento.setCaixa(caixaAberto);
        movimento.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
        movimento.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_COMPRA);
        movimento.setPagamentoCompra(pagamento);
        movimento.setValor(pagamento.getValor());
        movimento.setDescricao("Pagamento de boleto da compra ID " + pagamento.getCompraId() + " - Parcela " + pagamento.getParcela());
        movimento.setDataHora(dataPagamento.atStartOfDay());
        movimento.setUsuario(usuarioLogado);
        movimento.setFormaPagamento(converterMetodoPagamento(metodoPagamento));
        movimentoController.adicionarMovimento(movimento);
    }

    /**
     * Converte o método de pagamento para o formato do caixa.
     */
    private CaixaMovimento.FormaPagamento converterMetodoPagamento(PagamentoCompra.MetodoPagamento metodo) {
        switch (metodo) {
            case DINHEIRO:
                return CaixaMovimento.FormaPagamento.DINHEIRO;
            case DEBITO:
                return CaixaMovimento.FormaPagamento.DEBITO;
            case CREDITO:
                return CaixaMovimento.FormaPagamento.CREDITO;
            default:
                throw new IllegalArgumentException("Método de pagamento não suportado para boleto: " + metodo);
        }
    }

    /**
     * Devolve a compra selecionada, cancelando-a e ajustando o estoque.
     */
    private void devolverCompra() {
        int row = tabelaPagamentos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma compra para devolver.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PagamentoCompra pagamento = listaPagamentos.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja realmente devolver a compra ID " + pagamento.getCompraId() + "?",
                "Confirmar Devolução",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
                int compraId = pagamento.getCompraId();

                // Carrega os produtos da compra
                List<CompraProduto> produtosCompra = compraProdutoController.listarPorCompra(compraId);
                for (CompraProduto cp : produtosCompra) {
                    // Atualiza o estoque
                    Estoque estoque = estoqueController.buscarPorProdutoId(cp.getProdutoId());
                    if (estoque == null) {
                        estoque = new Estoque();
                        estoque.setProdutoId(cp.getProdutoId());
                        estoque.setQuantidade(0);
                        estoque.setEstoqueMinimo(0);
                    }
                    estoque.setQuantidade(estoque.getQuantidade() - cp.getQuantidade());
                    estoque.setUsuario(usuarioLogado);
                    if (!estoqueController.salvarOuAtualizarEstoque(estoque, usuarioLogado)) {
                        throw new SQLException("Falha ao atualizar estoque!");
                    }

                    // Registra movimento de saída no estoque
                    MovimentoEstoque movimentoEstoque = new MovimentoEstoque();
                    movimentoEstoque.setProdutoId(cp.getProdutoId());
                    movimentoEstoque.setQuantidade(cp.getQuantidade());
                    movimentoEstoque.setTipo(MovimentoEstoque.Tipo.SAIDA);
                    movimentoEstoque.setObservacoes("Devolução de compra ID " + compraId);
                    movimentoEstoque.setUsuario(usuarioLogado);
                    if (!movimentoEstoqueController.registrarMovimento(movimentoEstoque, usuarioLogado)) {
                        throw new SQLException("Falha ao registrar movimento de estoque!");
                    }
                }

                // Cancela a compra
                if (!compraController.cancelarCompra(compraId, usuarioLogado)) {
                    throw new SQLException("Falha ao cancelar a compra!");
                }

                // Cancela os pagamentos associados
                pagamentoController.cancelarPagamentosPorCompra(compraId, usuarioLogado);

                JOptionPane.showMessageDialog(this, "Compra devolvida com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarPagamentosFiltrados();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao devolver compra: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}