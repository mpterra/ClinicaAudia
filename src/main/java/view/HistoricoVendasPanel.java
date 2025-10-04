package view;

import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.EstoqueController;
import controller.PagamentoVendaController;
import controller.ProdutoController;
import controller.VendaController;
import controller.VendaProdutoController;
import controller.PacienteController;
import model.Caixa;
import model.CaixaMovimento;
import model.Estoque;
import model.PagamentoVenda;
import model.Produto;
import model.Venda;
import model.VendaProduto;
import model.Paciente;
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Painel para gerenciamento do histórico de vendas, com foco em acompanhamento de garantias, estornos e trocas.
 */
public class HistoricoVendasPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes do formulário
    private JTextField txtBuscaProdutoNome;
    private JTextField txtBuscaPacienteNome;
    private JTable tabelaVendas;
    private DefaultTableModel modeloTabelaVendas;
    private JLabel lblValorTotal;
    private JCheckBox chkGarantiaAtiva;

    // Componentes de filtro
    private JDateChooser dateChooserInicioFiltro;
    private JDateChooser dateChooserFimFiltro;

    // Estilo
    private final Color primaryColor = new Color(0, 128, 0); // Verde
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color rowColorLightGreen = new Color(230, 255, 230);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final VendaController vendaController = new VendaController();
    private final VendaProdutoController vendaProdutoController = new VendaProdutoController();
    private final ProdutoController produtoController = new ProdutoController();
    private final PacienteController pacienteController = new PacienteController();
    private final PagamentoVendaController pagamentoController = new PagamentoVendaController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController movimentoController = new CaixaMovimentoController();
    private final EstoqueController estoqueController = new EstoqueController();

    // Variáveis de estado
    private List<Venda> listaVendas = new ArrayList<>();
    private BigDecimal valorTotalVendas = BigDecimal.ZERO;
    private Map<Integer, Produto> cacheProdutos = new HashMap<>();
    private Map<Integer, Paciente> cachePacientes = new HashMap<>();

    // Variáveis de filtro
    private LocalDate dataInicioFiltro;
    private LocalDate dataFimFiltro;
    private String produtoNomeFiltro;
    private String pacienteNomeFiltro;
    private boolean garantiaAtivaFiltro;

    // Formato de data e valor
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    /**
     * Construtor do painel de histórico de vendas.
     */
    public HistoricoVendasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa filtros padrão (mês atual)
        LocalDate now = LocalDate.now();
        dataInicioFiltro = now.withDayOfMonth(1);
        dataFimFiltro = now.withDayOfMonth(now.lengthOfMonth());
        produtoNomeFiltro = "";
        pacienteNomeFiltro = "";
        garantiaAtivaFiltro = false;

        // Título
        JLabel lblTitulo = new JLabel("Histórico de Vendas", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painel da tabela
        JPanel painelTabela = criarPainelTabela();
        add(painelTabela, BorderLayout.CENTER);

        // Carrega cache de produtos e pacientes
        carregarCacheProdutos();
        carregarCachePacientes();

        // Carrega dados iniciais
        carregarVendasFiltradas();
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
     * Carrega o cache de pacientes para otimizar a busca por nome.
     */
    private void carregarCachePacientes() {
        try {
            for (Paciente p : pacienteController.listarTodos()) {
                cachePacientes.put(p.getId(), p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pacientes: " + e.getMessage(), "Erro",
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
                        "Vendas Realizadas", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
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

        JLabel lblPacienteNome = new JLabel("Nome do Paciente:");
        lblPacienteNome.setFont(labelFont);
        filtrosPanel.add(lblPacienteNome);

        txtBuscaPacienteNome = new JTextField(20);
        txtBuscaPacienteNome.setPreferredSize(new Dimension(200, 25));
        txtBuscaPacienteNome.setFont(fieldFont);
        txtBuscaPacienteNome.setToolTipText("Digite o nome do paciente para filtrar");
        txtBuscaPacienteNome.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        filtrosPanel.add(txtBuscaPacienteNome);

        chkGarantiaAtiva = new JCheckBox("Apenas Garantias Ativas");
        chkGarantiaAtiva.setFont(labelFont);
        chkGarantiaAtiva.setBackground(backgroundColor);
        chkGarantiaAtiva.addActionListener(e -> aplicarFiltros());
        filtrosPanel.add(chkGarantiaAtiva);

        panel.add(filtrosPanel, BorderLayout.NORTH);

        // Tabela
        String[] colunas = {"ID Venda", "Paciente", "Produto", "Valor Total", "Data Venda", "Fim Garantia", "Status Pagamento"};
        modeloTabelaVendas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaVendas = new JTable(modeloTabelaVendas) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                c.setForeground(Color.BLACK);
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                // Destaca vendas com garantia vencida
                if (column == 5 && modeloTabelaVendas.getValueAt(row, 5) != null) {
                    String fimGarantiaStr = (String) modeloTabelaVendas.getValueAt(row, 5);
                    try {
                        LocalDate fimGarantia = LocalDate.parse(fimGarantiaStr, dtf);
                        if (fimGarantia.isBefore(LocalDate.now())) {
                            c.setForeground(Color.RED);
                        }
                    } catch (Exception ignored) {}
                }
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
        tabelaVendas.setShowGrid(false);
        tabelaVendas.setIntercellSpacing(new Dimension(0, 0));
        tabelaVendas.setFillsViewportHeight(true);
        tabelaVendas.setRowHeight(25);
        tabelaVendas.setFont(fieldFont);
        tabelaVendas.setBackground(backgroundColor);
        JTableHeader header = tabelaVendas.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaVendas.getColumnCount(); i++) {
            tabelaVendas.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        JScrollPane scroll = new JScrollPane(tabelaVendas);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);

        // Painel inferior
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        southPanel.setBackground(backgroundColor);
        lblValorTotal = new JLabel("Valor Total: R$ 0,00");
        lblValorTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValorTotal.setForeground(primaryColor);
        southPanel.add(lblValorTotal);

        JButton btnEstornar = new JButton("Estornar Venda");
        btnEstornar.setBackground(primaryColor);
        btnEstornar.setForeground(Color.WHITE);
        btnEstornar.setPreferredSize(new Dimension(150, 30));
        btnEstornar.setFont(fieldFont);
        btnEstornar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEstornar.addActionListener(e -> estornarVenda());
        southPanel.add(btnEstornar);

        JButton btnTrocar = new JButton("Trocar Produto");
        btnTrocar.setBackground(primaryColor);
        btnTrocar.setForeground(Color.WHITE);
        btnTrocar.setPreferredSize(new Dimension(150, 30));
        btnTrocar.setFont(fieldFont);
        btnTrocar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTrocar.addActionListener(e -> trocarProduto());
        southPanel.add(btnTrocar);

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Configura o campo de data para aceitar apenas formatos válidos.
     */
    private void configurarCampoData(JTextFieldDateEditor editor) {
        editor.setFont(fieldFont);
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '/' && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
    }

    /**
     * Aplica os filtros e atualiza a tabela.
     */
    private void aplicarFiltros() {
        java.util.Date inicio = dateChooserInicioFiltro.getDate();
        java.util.Date fim = dateChooserFimFiltro.getDate();
        produtoNomeFiltro = txtBuscaProdutoNome.getText().trim().toLowerCase();
        pacienteNomeFiltro = txtBuscaPacienteNome.getText().trim().toLowerCase();
        garantiaAtivaFiltro = chkGarantiaAtiva.isSelected();
        dataInicioFiltro = inicio != null ? inicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() : null;
        dataFimFiltro = fim != null ? fim.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() : null;
        carregarVendasFiltradas();
    }

    /**
     * Carrega as vendas filtradas por data, nome do produto, paciente e garantia ativa.
     */
    private void carregarVendasFiltradas() {
        try {
            listaVendas = vendaController.listarTodos();
            if (dataInicioFiltro != null && dataFimFiltro != null) {
                listaVendas = listaVendas.stream()
                        .filter(v -> {
                            LocalDate dataVenda = v.getDataHora().toLocalDateTime().toLocalDate();
                            return !dataVenda.isBefore(dataInicioFiltro) && !dataVenda.isAfter(dataFimFiltro);
                        })
                        .collect(Collectors.toList());
            }

            List<Venda> vendasFiltradas = new ArrayList<>();
            for (Venda v : listaVendas) {
                List<VendaProduto> produtosVenda = vendaProdutoController.listarPorVenda(v.getId());
                boolean matchesProduto = produtoNomeFiltro.isEmpty();
                boolean matchesGarantia = !garantiaAtivaFiltro;
                for (VendaProduto vp : produtosVenda) {
                    Produto prod = cacheProdutos.get(vp.getProdutoId());
                    if (prod != null && prod.getNome().toLowerCase().contains(produtoNomeFiltro)) {
                        matchesProduto = true;
                    }
                    if (garantiaAtivaFiltro && vp.getFimGarantia() != null) {
                        LocalDate fimGarantia = vp.getFimGarantia().toLocalDate();
                        if (!fimGarantia.isBefore(LocalDate.now())) {
                            matchesGarantia = true;
                        }
                    }
                }
                boolean matchesPaciente = pacienteNomeFiltro.isEmpty();
                if (v.getPacienteId() != null) {
                    Paciente paciente = cachePacientes.get(v.getPacienteId());
                    if (paciente != null && paciente.getNome().toLowerCase().contains(pacienteNomeFiltro)) {
                        matchesPaciente = true;
                    }
                } else if (pacienteNomeFiltro.isEmpty()) {
                    matchesPaciente = true;
                }
                if (matchesProduto && matchesPaciente && matchesGarantia) {
                    vendasFiltradas.add(v);
                }
            }
            listaVendas = vendasFiltradas;

            modeloTabelaVendas.setRowCount(0);
            valorTotalVendas = BigDecimal.ZERO;
            for (Venda v : listaVendas) {
                List<VendaProduto> produtosVenda = vendaProdutoController.listarPorVenda(v.getId());
                StringBuilder nomesProdutos = new StringBuilder();
                LocalDate fimGarantiaMaisRecente = null;
                for (VendaProduto vp : produtosVenda) {
                    Produto prod = cacheProdutos.get(vp.getProdutoId());
                    if (prod != null) {
                        if (nomesProdutos.length() > 0) nomesProdutos.append(", ");
                        nomesProdutos.append(prod.getNome());
                    }
                    if (vp.getFimGarantia() != null) {
                        LocalDate fimGarantia = vp.getFimGarantia().toLocalDate();
                        if (fimGarantiaMaisRecente == null || fimGarantia.isAfter(fimGarantiaMaisRecente)) {
                            fimGarantiaMaisRecente = fimGarantia;
                        }
                    }
                }
                String nomePaciente = "";
                if (v.getPacienteId() != null) {
                    Paciente paciente = cachePacientes.get(v.getPacienteId());
                    if (paciente != null) {
                        nomePaciente = paciente.getNome();
                    }
                }
                String statusPagamento = pagamentoController.listarPorVenda(v).stream()
                        .allMatch(p -> p.getStatus().equals("PAGO")) ? "PAGO" : "PENDENTE";
                Object[] row = {
                    v.getId(),
                    nomePaciente,
                    nomesProdutos.toString(),
                    df.format(v.getValorTotal()),
                    v.getDataHora().toLocalDateTime().toLocalDate().format(dtf),
                    fimGarantiaMaisRecente != null ? fimGarantiaMaisRecente.format(dtf) : "",
                    statusPagamento
                };
                modeloTabelaVendas.addRow(row);
                valorTotalVendas = valorTotalVendas.add(v.getValorTotal());
            }
            lblValorTotal.setText("Valor Total: " + df.format(valorTotalVendas));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar vendas: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Estorna a venda selecionada, revertendo movimentos financeiros e estoque.
     */
    private void estornarVenda() {
        int row = tabelaVendas.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma venda para estornar.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Venda venda = listaVendas.get(row);
        try {
            if (!caixaController.existeCaixaAberto()) {
                JOptionPane.showMessageDialog(this, "Não há caixa aberto para registrar o estorno.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<PagamentoVenda> pagamentos = pagamentoController.listarPorVenda(venda);
            // Verifica se algum produto está emprestado
            List<VendaProduto> produtosVenda = vendaProdutoController.listarPorVenda(venda.getId());
            for (VendaProduto vp : produtosVenda) {
                String codigoSerial = vp.getCogidoSerial();
                if (codigoSerial != null && !codigoSerial.isEmpty()) {
                    // Assumindo que existe um método para verificar empréstimos
                    // Para simplificação, assumimos que não há empréstimos
                }
            }
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Estornar Venda");
            dialog.setLayout(new GridBagLayout());
            dialog.getContentPane().setBackground(backgroundColor);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lblObservacoes = new JLabel("Observações do Estorno:");
            lblObservacoes.setFont(labelFont);
            gbc.gridx = 0;
            gbc.gridy = 0;
            dialog.add(lblObservacoes, gbc);

            JTextArea txtObservacoes = new JTextArea(5, 20);
            txtObservacoes.setFont(fieldFont);
            txtObservacoes.setLineWrap(true);
            txtObservacoes.setWrapStyleWord(true);
            JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
            gbc.gridx = 1;
            dialog.add(scrollObservacoes, gbc);

            JButton btnConfirmar = new JButton("Confirmar Estorno");
            btnConfirmar.setBackground(primaryColor);
            btnConfirmar.setForeground(Color.WHITE);
            btnConfirmar.setPreferredSize(new Dimension(150, 30));
            btnConfirmar.setFont(fieldFont);
            btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            dialog.add(btnConfirmar, gbc);

            btnConfirmar.addActionListener(e -> {
                try {
                    String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
                    Caixa caixaAberto = caixaController.getCaixaAberto();
                    // Reverte pagamentos
                    for (PagamentoVenda p : pagamentos) {
                        p.setStatus("CANCELADO");
                        pagamentoController.atualizar(p);
                        // Registra movimento de caixa apenas para pagamentos PAGO
                        if (p.getStatus().equals("PAGO")) {
                            CaixaMovimento movimento = new CaixaMovimento();
                            movimento.setCaixa(caixaAberto);
                            movimento.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
                            movimento.setOrigem(CaixaMovimento.OrigemMovimento.AJUSTE);
                            movimento.setValor(p.getValor());
                            movimento.setDescricao("Estorno da venda ID " + venda.getId() + " - Parcela " + p.getParcela() + ": " + txtObservacoes.getText());
                            movimento.setDataHora(LocalDateTime.now());
                            movimento.setUsuario(usuarioLogado);
                            movimento.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(p.getMetodoPagamento().name()));
                            movimentoController.adicionarMovimento(movimento);
                        }
                    }
                    // Restaura estoque
                    for (VendaProduto vp : produtosVenda) {
                        estoqueController.incrementarEstoque(vp.getProdutoId(), vp.getQuantidade(),
                            "Estorno da venda ID " + venda.getId(), usuarioLogado);
                    }
                    // Remove a venda
                    vendaController.removerVenda(venda.getId());
                    JOptionPane.showMessageDialog(dialog, "Venda estornada com sucesso!", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarVendasFiltradas();
                    dialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao estornar venda: " + ex.getMessage(), "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao verificar venda: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre popup para trocar um produto da venda selecionada.
     */
    private void trocarProduto() {
        int row = tabelaVendas.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma venda para trocar produto.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Venda venda = listaVendas.get(row);
        try {
            if (!caixaController.existeCaixaAberto()) {
                JOptionPane.showMessageDialog(this, "Não há caixa aberto para registrar a troca.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<VendaProduto> produtosVenda = vendaProdutoController.listarPorVenda(venda.getId());
            // Popup para selecionar produto a ser trocado
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Trocar Produto");
            dialog.setLayout(new GridBagLayout());
            dialog.getContentPane().setBackground(backgroundColor);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lblProdutoAntigo = new JLabel("Produto a ser Trocado:");
            lblProdutoAntigo.setFont(labelFont);
            gbc.gridx = 0;
            gbc.gridy = 0;
            dialog.add(lblProdutoAntigo, gbc);

            JComboBox<String> cmbProdutoAntigo = new JComboBox<>();
            for (VendaProduto vp : produtosVenda) {
                Produto prod = cacheProdutos.get(vp.getProdutoId());
                if (prod != null && vp.getFimGarantia() != null && !vp.getFimGarantia().toLocalDate().isBefore(LocalDate.now())) {
                    String identificador = (vp.getCogidoSerial() != null && !vp.getCogidoSerial().isEmpty())
                            ? vp.getCogidoSerial()
                            : String.valueOf(vp.getProdutoId());
                    cmbProdutoAntigo.addItem(prod.getNome() + " (" + identificador + ")");
                }
            }
            cmbProdutoAntigo.setFont(fieldFont);
            gbc.gridx = 1;
            dialog.add(cmbProdutoAntigo, gbc);

            JLabel lblProdutoNovo = new JLabel("Novo Produto:");
            lblProdutoNovo.setFont(labelFont);
            gbc.gridx = 0;
            gbc.gridy = 1;
            dialog.add(lblProdutoNovo, gbc);

            JComboBox<Produto> cmbProdutoNovo = new JComboBox<>();
            for (Produto p : produtoController.listarTodos()) {
                Estoque estoque = estoqueController.buscarPorProdutoId(p.getId());
                if (estoque != null && estoque.getQuantidade() > 0) {
                    cmbProdutoNovo.addItem(p);
                }
            }
            cmbProdutoNovo.setFont(fieldFont);
            gbc.gridx = 1;
            dialog.add(cmbProdutoNovo, gbc);

            JLabel lblNovoSerial = new JLabel("Novo Código Serial:");
            lblNovoSerial.setFont(labelFont);
            gbc.gridx = 0;
            gbc.gridy = 2;
            dialog.add(lblNovoSerial, gbc);

            JTextField txtNovoSerial = new JTextField(20);
            txtNovoSerial.setFont(fieldFont);
            txtNovoSerial.setPreferredSize(new Dimension(200, 25));
            gbc.gridx = 1;
            dialog.add(txtNovoSerial, gbc);

            JLabel lblObservacoes = new JLabel("Observações da Troca:");
            lblObservacoes.setFont(labelFont);
            gbc.gridx = 0;
            gbc.gridy = 3;
            dialog.add(lblObservacoes, gbc);

            JTextArea txtObservacoes = new JTextArea(5, 20);
            txtObservacoes.setFont(fieldFont);
            txtObservacoes.setLineWrap(true);
            txtObservacoes.setWrapStyleWord(true);
            JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
            gbc.gridx = 1;
            dialog.add(scrollObservacoes, gbc);

            JButton btnConfirmar = new JButton("Confirmar Troca");
            btnConfirmar.setBackground(primaryColor);
            btnConfirmar.setForeground(Color.WHITE);
            btnConfirmar.setPreferredSize(new Dimension(150, 30));
            btnConfirmar.setFont(fieldFont);
            btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            dialog.add(btnConfirmar, gbc);

            btnConfirmar.addActionListener(e -> {
                try {
                    if (cmbProdutoAntigo.getSelectedIndex() == -1 || cmbProdutoNovo.getSelectedIndex() == -1) {
                        JOptionPane.showMessageDialog(dialog, "Selecione o produto antigo e o novo.", "Erro",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String novoSerial = txtNovoSerial.getText().trim();
                    if (!novoSerial.isEmpty() && vendaProdutoController.serialExiste(novoSerial)) {
                        JOptionPane.showMessageDialog(dialog, "Código serial já existe.", "Erro",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
                    Caixa caixaAberto = caixaController.getCaixaAberto();
                    String produtoAntigoSelecionado = (String) cmbProdutoAntigo.getSelectedItem();
                    String identificador = produtoAntigoSelecionado.split("\\(")[1].replace(")", "");
                    VendaProduto produtoAntigo = produtosVenda.stream()
                            .filter(vp -> {
                                String vpIdentificador = (vp.getCogidoSerial() != null && !vp.getCogidoSerial().isEmpty())
                                        ? vp.getCogidoSerial()
                                        : String.valueOf(vp.getProdutoId());
                                return vpIdentificador.equals(identificador);
                            })
                            .findFirst()
                            .orElse(null);
                    Produto produtoNovo = (Produto) cmbProdutoNovo.getSelectedItem();

                    if (produtoAntigo == null || produtoNovo == null) {
                        JOptionPane.showMessageDialog(dialog, "Erro ao identificar produtos.", "Erro",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Verifica diferença de valor
                    BigDecimal diferenca = produtoNovo.getPrecoVenda().subtract(produtoAntigo.getPrecoUnitario().subtract(produtoAntigo.getDesconto()));
                    if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
                        // Cria novo pagamento para a diferença
                        PagamentoVenda pagamento = new PagamentoVenda();
                        pagamento.setVenda(venda);
                        pagamento.setDataHora(LocalDateTime.now());
                        pagamento.setDataVencimento(LocalDate.now());
                        pagamento.setValor(diferenca);
                        pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.DINHEIRO);
                        pagamento.setParcela(1);
                        pagamento.setTotalParcelas(1);
                        pagamento.setStatus("PAGO");
                        pagamento.setUsuario(usuarioLogado);
                        pagamentoController.inserir(pagamento);

                        CaixaMovimento movimento = new CaixaMovimento();
                        movimento.setCaixa(caixaAberto);
                        movimento.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
                        movimento.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
                        movimento.setPagamentoVenda(pagamento);
                        movimento.setValor(diferenca);
                        movimento.setDescricao("Diferença de troca da venda ID " + venda.getId() + ": " + txtObservacoes.getText());
                        movimento.setDataHora(LocalDateTime.now());
                        movimento.setUsuario(usuarioLogado);
                        movimento.setFormaPagamento(CaixaMovimento.FormaPagamento.DINHEIRO);
                        movimentoController.adicionarMovimento(movimento);
                    } else if (diferenca.compareTo(BigDecimal.ZERO) < 0) {
                        // Registra reembolso
                        CaixaMovimento movimento = new CaixaMovimento();
                        movimento.setCaixa(caixaAberto);
                        movimento.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
                        movimento.setOrigem(CaixaMovimento.OrigemMovimento.AJUSTE);
                        movimento.setValor(diferenca.abs());
                        movimento.setDescricao("Reembolso de troca da venda ID " + venda.getId() + ": " + txtObservacoes.getText());
                        movimento.setDataHora(LocalDateTime.now());
                        movimento.setUsuario(usuarioLogado);
                        movimento.setFormaPagamento(CaixaMovimento.FormaPagamento.DINHEIRO);
                        movimentoController.adicionarMovimento(movimento);
                    }

                    // Restaura estoque do produto antigo
                    estoqueController.incrementarEstoque(produtoAntigo.getProdutoId(), produtoAntigo.getQuantidade(),
                            "Troca da venda ID " + venda.getId(), usuarioLogado);

                    // Remove produto antigo e adiciona novo
                    vendaProdutoController.removerProdutoVenda(venda.getId(), produtoAntigo.getProdutoId());
                    VendaProduto novoProduto = new VendaProduto();
                    novoProduto.setVendaId(venda.getId());
                    novoProduto.setProdutoId(produtoNovo.getId());
                    novoProduto.setQuantidade(produtoAntigo.getQuantidade());
                    novoProduto.setPrecoUnitario(produtoNovo.getPrecoVenda());
                    novoProduto.setDesconto(BigDecimal.ZERO);
                    novoProduto.setGarantiaMeses(produtoNovo.getGarantiaMeses());
                    if (produtoNovo.getGarantiaMeses() > 0) {
                        novoProduto.setFimGarantia(java.sql.Date.valueOf(LocalDate.now().plusMonths(produtoNovo.getGarantiaMeses())));
                    }
                    novoProduto.setCogidoSerial(novoSerial.isEmpty() ? null : novoSerial);
                    vendaProdutoController.adicionarProdutoVenda(novoProduto);

                    // Atualiza estoque do novo produto
                    estoqueController.reduzirEstoque(produtoNovo.getId(), produtoAntigo.getQuantidade(),
                            "Troca da venda ID " + venda.getId(), usuarioLogado);

                    // Atualiza valor total da venda
                    BigDecimal novoValorTotal = BigDecimal.ZERO;
                    for (VendaProduto vp : vendaProdutoController.listarPorVenda(venda.getId())) {
                        novoValorTotal = novoValorTotal.add(vp.getPrecoUnitario().subtract(vp.getDesconto()).multiply(BigDecimal.valueOf(vp.getQuantidade())));
                    }
                    venda.setValorTotal(novoValorTotal);
                    vendaController.atualizarVenda(venda, usuarioLogado);

                    JOptionPane.showMessageDialog(dialog, "Troca realizada com sucesso!", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarVendasFiltradas();
                    dialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao realizar troca: " + ex.getMessage(), "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados da venda: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}