package view;

import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.PagamentoVendaController;
import controller.ProdutoController;
import controller.VendaController;
import controller.VendaProdutoController;
import controller.PacienteController;
import model.Caixa;
import model.CaixaMovimento;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Painel para gerenciamento de pagamentos pendentes de vendas (apenas BOLETO).
 */
public class PagamentoVendaPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes do formulário
    private JTextField txtBuscaProdutoNome;
    private JTextField txtBuscaPacienteNome;
    private JTable tabelaPagamentos;
    private DefaultTableModel modeloTabelaPagamentos;
    private JLabel lblValorTotal;

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
    private final PagamentoVendaController pagamentoController = new PagamentoVendaController();
    private final VendaController vendaController = new VendaController();
    private final VendaProdutoController vendaProdutoController = new VendaProdutoController();
    private final ProdutoController produtoController = new ProdutoController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController movimentoController = new CaixaMovimentoController();
    private final PacienteController pacienteController = new PacienteController();

    // Variáveis de estado
    private List<PagamentoVenda> listaPagamentos = new ArrayList<>();
    private BigDecimal valorTotalPagamentos = BigDecimal.ZERO;
    private Map<Integer, Produto> cacheProdutos = new HashMap<>();
    private Map<Integer, Paciente> cachePacientes = new HashMap<>();

    // Variáveis de filtro
    private LocalDate dataInicioFiltro;
    private LocalDate dataFimFiltro;
    private String produtoNomeFiltro;
    private String pacienteNomeFiltro;

    // Formato de data e valor
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    // Formas de pagamento permitidas para pagar boletos
    private static final PagamentoVenda.MetodoPagamento[] FORMAS_PAGAMENTO_BOLETO = {
        PagamentoVenda.MetodoPagamento.DINHEIRO,
        PagamentoVenda.MetodoPagamento.DEBITO,
        PagamentoVenda.MetodoPagamento.CREDITO
    };

    /**
     * Construtor do painel de pagamentos de vendas.
     */
    public PagamentoVendaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa filtros padrão (mês atual)
        LocalDate now = LocalDate.now();
        dataInicioFiltro = now.withDayOfMonth(1);
        dataFimFiltro = now.withDayOfMonth(now.lengthOfMonth());
        produtoNomeFiltro = "";
        pacienteNomeFiltro = "";

        // Título
        JLabel lblTitulo = new JLabel("Pagamentos de Vendas (Boletos)", SwingConstants.CENTER);
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

        panel.add(filtrosPanel, BorderLayout.NORTH);

        // Tabela
        String[] colunas = {"Paciente", "Produto", "Valor", "Método Pag.", "Data Venc.", "Parcela", "Status"};
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
        southPanel.add(lblValorTotal);

        JButton btnReceber = new JButton("Receber Selecionado");
        btnReceber.setBackground(primaryColor);
        btnReceber.setForeground(Color.WHITE);
        btnReceber.setPreferredSize(new Dimension(150, 30));
        btnReceber.setFont(fieldFont);
        btnReceber.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReceber.addActionListener(e -> pagarBoleto());
        southPanel.add(btnReceber);

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

        dataInicioFiltro = inicio != null ? inicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() : null;
        dataFimFiltro = fim != null ? fim.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() : null;

        carregarPagamentosFiltrados();
    }

    /**
     * Carrega os pagamentos filtrados por data de vencimento, nome do produto e nome do paciente.
     */
    private void carregarPagamentosFiltrados() {
        try {
            // Carrega pagamentos pendentes de boleto
            listaPagamentos = pagamentoController.listarPorMetodo(PagamentoVenda.MetodoPagamento.BOLETO)
                    .stream()
                    .filter(p -> p.getStatus().equals("PENDENTE"))
                    .collect(Collectors.toList());

            // Aplica filtros de data e nome do produto
            if (dataInicioFiltro != null && dataFimFiltro != null) {
                listaPagamentos = pagamentoController.listarPorMetodoEDataVencimento(
                        PagamentoVenda.MetodoPagamento.BOLETO, dataInicioFiltro, dataFimFiltro);
            }

            // Filtra por nome do produto e nome do paciente
            List<PagamentoVenda> pagamentosFiltrados = new ArrayList<>();
            for (PagamentoVenda p : listaPagamentos) {
                List<VendaProduto> produtosVenda = vendaProdutoController.listarPorVenda(p.getVenda().getId());
                boolean matchesProduto = produtoNomeFiltro.isEmpty();
                for (VendaProduto vp : produtosVenda) {
                    Produto prod = cacheProdutos.get(vp.getProdutoId());
                    if (prod != null && prod.getNome().toLowerCase().contains(produtoNomeFiltro)) {
                        matchesProduto = true;
                        break;
                    }
                }

                boolean matchesPaciente = pacienteNomeFiltro.isEmpty();
                Venda venda = vendaController.buscarPorId(p.getVenda().getId());
                if (venda != null && venda.getPacienteId() != null) {
                    Paciente paciente = cachePacientes.get(venda.getPacienteId());
                    if (paciente != null && paciente.getNome().toLowerCase().contains(pacienteNomeFiltro)) {
                        matchesPaciente = true;
                    }
                } else if (pacienteNomeFiltro.isEmpty()) {
                    matchesPaciente = true; // Se não há filtro de paciente, considera válido
                }

                if (matchesProduto && matchesPaciente) {
                    pagamentosFiltrados.add(p);
                }
            }
            listaPagamentos = pagamentosFiltrados;

            // Atualiza tabela
            modeloTabelaPagamentos.setRowCount(0);
            valorTotalPagamentos = BigDecimal.ZERO;

            for (PagamentoVenda p : listaPagamentos) {
                List<VendaProduto> produtosVenda = vendaProdutoController.listarPorVenda(p.getVenda().getId());
                StringBuilder nomesProdutos = new StringBuilder();
                for (VendaProduto vp : produtosVenda) {
                    Produto prod = cacheProdutos.get(vp.getProdutoId());
                    if (prod != null) {
                        if (nomesProdutos.length() > 0) nomesProdutos.append(", ");
                        nomesProdutos.append(prod.getNome());
                    }
                }

                String nomePaciente = "";
                Venda venda = vendaController.buscarPorId(p.getVenda().getId());
                if (venda != null && venda.getPacienteId() != null) {
                    Paciente paciente = cachePacientes.get(venda.getPacienteId());
                    if (paciente != null) {
                        nomePaciente = paciente.getNome();
                    }
                }

                Object[] row = {
                    nomePaciente,
                    nomesProdutos.toString(),
                    df.format(p.getValor()),
                    p.getMetodoPagamento(),
                    p.getDataVencimento() != null ? sdf.format(java.util.Date.from(p.getDataVencimento().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())) : "",
                    p.getParcela() + "/" + p.getTotalParcelas(),
                    p.getStatus()
                };
                modeloTabelaPagamentos.addRow(row);
                valorTotalPagamentos = valorTotalPagamentos.add(p.getValor());
            }

            lblValorTotal.setText("Valor Total: " + df.format(valorTotalPagamentos));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pagamentos: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre popup para registrar o recebimento do boleto selecionado.
     */
    private void pagarBoleto() {
        int row = tabelaPagamentos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um boleto para receber.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PagamentoVenda pagamento = listaPagamentos.get(row);
        if (pagamento.getStatus().equals("PAGO")) {
            JOptionPane.showMessageDialog(this, "Boleto já pago.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Popup para registrar o recebimento
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Receber Boleto");
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

        JComboBox<PagamentoVenda.MetodoPagamento> cmbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO_BOLETO);
        cmbMetodoPagamento.setFont(fieldFont);
        cmbMetodoPagamento.setPreferredSize(new Dimension(120, 25));
        cmbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cmbMetodoPagamento.setSelectedItem(PagamentoVenda.MetodoPagamento.DEBITO); // Define DÉBITO como padrão
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

        JLabel lblValor = new JLabel("Valor: " + df.format(pagamento.getValor()));
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
        dateChooserPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configurarCampoData((JTextFieldDateEditor) dateChooserPagamento.getDateEditor());
        gbc.gridx = 1;
        dialog.add(dateChooserPagamento, gbc);

        JButton btnConfirmar = new JButton("Confirmar Recebimento");
        btnConfirmar.setBackground(primaryColor);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(150, 30));
        btnConfirmar.setFont(fieldFont);
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(btnConfirmar, gbc);

        // Ação do botão confirmar
        btnConfirmar.addActionListener(e -> {
            try {
                if (!caixaController.existeCaixaAberto()) {
                    JOptionPane.showMessageDialog(dialog, "Não há caixa aberto para registrar o recebimento.", "Erro",
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
                PagamentoVenda.MetodoPagamento metodoPagamento = (PagamentoVenda.MetodoPagamento) cmbMetodoPagamento.getSelectedItem();
                String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
                PagamentoVenda updatedPagamento = new PagamentoVenda(
                        pagamento.getId(), pagamento.getVenda(), LocalDateTime.now(),
                        pagamento.getDataVencimento(), pagamento.getValor(),
                        metodoPagamento, pagamento.getParcela(),
                        pagamento.getTotalParcelas(), "PAGO",
                        pagamento.getObservacoes(), usuarioLogado
                );
                pagamentoController.atualizar(updatedPagamento);
                registrarMovimentoCaixa(updatedPagamento, usuarioLogado, dataPagamento, metodoPagamento);
                JOptionPane.showMessageDialog(dialog, "Recebimento registrado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarPagamentosFiltrados();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao registrar recebimento: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Registra o movimento no caixa para o pagamento (entrada).
     */
    private void registrarMovimentoCaixa(PagamentoVenda pagamento, String usuarioLogado, LocalDate dataPagamento, PagamentoVenda.MetodoPagamento metodoPagamento) throws SQLException {
        Caixa caixaAberto = caixaController.getCaixaAberto();
        CaixaMovimento movimento = new CaixaMovimento();
        movimento.setCaixa(caixaAberto);
        movimento.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
        movimento.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
        movimento.setPagamentoVenda(pagamento);
        movimento.setValor(pagamento.getValor());
        movimento.setDescricao("Recebimento de boleto da venda ID " + pagamento.getVenda().getId() + " - Parcela " + pagamento.getParcela());
        movimento.setDataHora(dataPagamento.atStartOfDay());
        movimento.setUsuario(usuarioLogado);
        movimento.setFormaPagamento(converterMetodoPagamento(metodoPagamento));
        movimentoController.adicionarMovimento(movimento);
    }

    /**
     * Converte o método de pagamento para o formato do caixa.
     */
    private CaixaMovimento.FormaPagamento converterMetodoPagamento(PagamentoVenda.MetodoPagamento metodo) {
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
}