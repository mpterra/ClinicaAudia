package view.dialogs;

import com.toedter.calendar.JCalendar;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import controller.*;
import model.*;
import util.Sessao;

// Classe temporária para armazenar dados de pagamento na interface
class PagamentoCompraTemp {
    String metodo;
    BigDecimal valor;
    int parcelas;
    LocalDate dataVencimentoInicial;

    PagamentoCompraTemp(String metodo, BigDecimal valor, int parcelas, LocalDate dataVencimentoInicial) {
        this.metodo = metodo;
        this.valor = valor;
        this.parcelas = parcelas;
        this.dataVencimentoInicial = dataVencimentoInicial;
    }
}

// Diálogo para gerenciar múltiplos pagamentos de uma compra
public class CheckoutCompraDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    // Componentes
    private JComboBox<String> cbMetodoPagamento;
    private JTextField txtValorPagamento;
    private JSpinner spinnerParcelas;
    private JFormattedTextField dateVencimentoInicial;
    private JPopupMenu calendarPopup;
    private JCalendar calendar;
    private JTable tabelaPagamentos;
    private DefaultTableModel modeloTabelaPagamentos;
    private JLabel lblValorRestante;
    private JButton btnConfirmar;

    // Estilo
    private final Color primaryColor = new Color(154, 5, 38);
    private final Color secondaryColor = new Color(255, 204, 0);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color rowColorLightRed = new Color(255, 230, 230);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final CompraController compraController = new CompraController();
    private final CompraProdutoController compraProdutoController = new CompraProdutoController();
    private final EstoqueController estoqueController = new EstoqueController();
    private final MovimentoEstoqueController movimentoEstoqueController = new MovimentoEstoqueController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController caixaMovimentoController = new CaixaMovimentoController();
    private final PagamentoCompraController pagamentoCompraController = new PagamentoCompraController();

    // Estado
    private final BigDecimal valorTotalCompra;
    private final List<CompraProduto> itensCompra;
    private final List<PagamentoCompraTemp> pagamentos = new ArrayList<>();
    private boolean compraConcluida = false;
    private static final String[] FORMAS_PAGAMENTO = {"DINHEIRO", "PIX", "DEBITO", "CREDITO", "BOLETO"};

    public CheckoutCompraDialog(JFrame parentFrame, BigDecimal valorTotalCompra, List<CompraProduto> itensCompra) {
        super(parentFrame, "Checkout - Pagamentos de Compra", true);
        this.valorTotalCompra = valorTotalCompra;
        this.itensCompra = new ArrayList<>(itensCompra);
        setSize(600, 580);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);

        inicializarComponentes();
    }

    // Inicializa os componentes do diálogo
    private void inicializarComponentes() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(backgroundColor);

        // Painel de entrada de pagamento
        JPanel inputPanel = criarPainelEntrada();
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Tabela de pagamentos
        JPanel tabelaPanel = criarPainelTabela();
        mainPanel.add(tabelaPanel, BorderLayout.CENTER);

        // Painel inferior
        JPanel southPanel = criarPainelInferior();
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // Configura calendário
        configurarCalendario();

        // Listener para método de pagamento
        cbMetodoPagamento.addActionListener(e -> atualizarCamposPagamento());

        add(mainPanel, BorderLayout.CENTER);
        atualizarCamposPagamento();
        atualizarTabelaPagamentos();
    }

    // Cria painel de entrada de dados
    private JPanel criarPainelEntrada() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(backgroundColor);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1), "Adicionar Pagamento",
                TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Método de pagamento
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblMetodo = new JLabel("Forma pgto:");
        lblMetodo.setFont(labelFont);
        inputPanel.add(lblMetodo, gbc);
        cbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO);
        cbMetodoPagamento.setPreferredSize(new Dimension(120, 25));
        cbMetodoPagamento.setFont(fieldFont);
        cbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1; gbc.weightx = 1.0;
        inputPanel.add(cbMetodoPagamento, gbc);

        // Valor do pagamento
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblValor = new JLabel("Valor:");
        lblValor.setFont(labelFont);
        inputPanel.add(lblValor, gbc);
        txtValorPagamento = new JTextField("0,00", 10);
        txtValorPagamento.setFont(fieldFont);
        ((AbstractDocument) txtValorPagamento.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        gbc.gridx = 1; gbc.weightx = 1.0;
        inputPanel.add(txtValorPagamento, gbc);

        // Parcelas
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblParcelas = new JLabel("Parcelas:");
        lblParcelas.setFont(labelFont);
        inputPanel.add(lblParcelas, gbc);
        spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerParcelas.setPreferredSize(new Dimension(70, 25));
        spinnerParcelas.setFont(fieldFont);
        spinnerParcelas.setEnabled(false);
        spinnerParcelas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1; gbc.weightx = 1.0;
        inputPanel.add(spinnerParcelas, gbc);

        // Data de vencimento
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel lblDataVencimento = new JLabel("1º Vencimento:");
        lblDataVencimento.setFont(labelFont);
        inputPanel.add(lblDataVencimento, gbc);
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            dateVencimentoInicial = new JFormattedTextField(dateMask);
            dateVencimentoInicial.setPreferredSize(new Dimension(120, 25));
            dateVencimentoInicial.setFont(fieldFont);
            dateVencimentoInicial.setEnabled(false);
            dateVencimentoInicial.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateVencimentoInicial.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao configurar formato de data.", "Erro", JOptionPane.ERROR_MESSAGE);
            dateVencimentoInicial = new JFormattedTextField();
            dateVencimentoInicial.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        gbc.gridx = 1; gbc.weightx = 1.0;
        inputPanel.add(dateVencimentoInicial, gbc);

        // Botões de ação
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        botoesPanel.setBackground(backgroundColor);
        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.setBackground(primaryColor);
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.setPreferredSize(new Dimension(100, 30));
        btnAdicionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdicionar.addActionListener(e -> adicionarPagamento());
        botoesPanel.add(btnAdicionar);
        JButton btnRemover = new JButton("Remover");
        btnRemover.setBackground(Color.RED);
        btnRemover.setForeground(Color.WHITE);
        btnRemover.setPreferredSize(new Dimension(100, 30));
        btnRemover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemover.addActionListener(e -> removerPagamento());
        botoesPanel.add(btnRemover);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 0.0;
        inputPanel.add(botoesPanel, gbc);

        return inputPanel;
    }

    // Cria painel da tabela de pagamentos
    private JPanel criarPainelTabela() {
        JPanel tabelaPanel = new JPanel(new BorderLayout());
        tabelaPanel.setBackground(backgroundColor);
        tabelaPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1), "Pagamentos Adicionados",
                TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor));

        String[] colunas = {"Método", "Valor", "Parcelas", "1º Vencimento"};
        modeloTabelaPagamentos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaPagamentos = new JTable(modeloTabelaPagamentos) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? rowColorLightRed : Color.WHITE);
                if (isRowSelected(row)) {
                    c.setBackground(secondaryColor);
                }
                return c;
            }
        };
        tabelaPagamentos.setRowHeight(25);
        tabelaPagamentos.setFont(fieldFont);
        JTableHeader header = tabelaPagamentos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tabelaPagamentos.setDefaultRenderer(Object.class, centerRenderer);
        tabelaPagamentos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JScrollPane scroll = new JScrollPane(tabelaPagamentos);
        tabelaPanel.add(scroll, BorderLayout.CENTER);
        return tabelaPanel;
    }

    // Cria painel inferior com valor restante e botões
    private JPanel criarPainelInferior() {
        JPanel southPanel = new JPanel(new GridBagLayout());
        southPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblValorRestante = new JLabel("Valor Restante: R$ " + String.format("%.2f", valorTotalCompra));
        lblValorRestante.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValorRestante.setForeground(primaryColor);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        southPanel.add(lblValorRestante, gbc);

        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoesPanel.setBackground(backgroundColor);
        btnConfirmar = new JButton("Efetuar");
        btnConfirmar.setBackground(primaryColor);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(100, 30));
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> realizarCompra());
        botoesPanel.add(btnConfirmar);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 30));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());
        botoesPanel.add(btnCancelar);
        gbc.gridx = 0; gbc.gridy = 1;
        southPanel.add(botoesPanel, gbc);

        return southPanel;
    }

    // Configura o calendário
    private void configurarCalendario() {
        calendarPopup = new JPopupMenu();
        calendar = new JCalendar();
        calendar.setDecorationBackgroundColor(backgroundColor);
        calendar.setTodayButtonVisible(true);
        calendar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarPopup.add(calendar);
        calendarPopup.setPreferredSize(new Dimension(300, 200));
        calendar.addPropertyChangeListener("calendar", evt -> {
            java.util.Calendar selectedDate = calendar.getCalendar();
            if (selectedDate != null) {
                LocalDate date = selectedDate.getTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                dateVencimentoInicial.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                calendarPopup.setVisible(false);
            }
        });
        dateVencimentoInicial.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (dateVencimentoInicial.isEnabled()) {
                    calendarPopup.show(dateVencimentoInicial, 0, dateVencimentoInicial.getHeight());
                }
            }
        });
    }

    // Atualiza os campos de parcelas e data de vencimento
    private void atualizarCamposPagamento() {
        String metodo = (String) cbMetodoPagamento.getSelectedItem();
        spinnerParcelas.setEnabled("BOLETO".equals(metodo) || "CREDITO".equals(metodo));
        dateVencimentoInicial.setEnabled("BOLETO".equals(metodo));
        spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, "BOLETO".equals(metodo) || "CREDITO".equals(metodo) ? 12 : 1, 1));
    }

    // Adiciona um pagamento à lista temporária
    private void adicionarPagamento() {
        try {
            String metodo = (String) cbMetodoPagamento.getSelectedItem();
            BigDecimal valor = new BigDecimal(txtValorPagamento.getText().replace(".", "").replace(",", "."));
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero!");
            }
            int parcelas = (Integer) spinnerParcelas.getValue();
            LocalDate dataVencimento = null;
            if ("BOLETO".equals(metodo)) {
                try {
                    dataVencimento = LocalDate.parse(dateVencimentoInicial.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (dataVencimento.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Data de vencimento não pode ser anterior à data atual!");
                    }
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Data de vencimento inválida!");
                }
            }
            BigDecimal somaPagamentos = pagamentos.stream().map(p -> p.valor).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (somaPagamentos.add(valor).compareTo(valorTotalCompra) > 0) {
                throw new IllegalArgumentException("O valor total dos pagamentos excede o valor da compra!");
            }
            pagamentos.add(new PagamentoCompraTemp(metodo, valor, parcelas, dataVencimento));
            atualizarTabelaPagamentos();
            txtValorPagamento.setText("0,00");
            cbMetodoPagamento.setSelectedIndex(0);
            spinnerParcelas.setValue(1);
            dateVencimentoInicial.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            atualizarCamposPagamento();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar pagamento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Remove o pagamento selecionado
    private void removerPagamento() {
        int selectedRow = tabelaPagamentos.getSelectedRow();
        if (selectedRow >= 0) {
            pagamentos.remove(selectedRow);
            atualizarTabelaPagamentos();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um pagamento para remover!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza a tabela de pagamentos
    private void atualizarTabelaPagamentos() {
        modeloTabelaPagamentos.setRowCount(0);
        BigDecimal somaPagamentos = BigDecimal.ZERO;
        for (PagamentoCompraTemp p : pagamentos) {
            modeloTabelaPagamentos.addRow(new Object[]{
                    p.metodo,
                    String.format("R$ %.2f", p.valor),
                    p.parcelas,
                    p.dataVencimentoInicial != null ? p.dataVencimentoInicial.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"
            });
            somaPagamentos = somaPagamentos.add(p.valor);
        }
        BigDecimal restante = valorTotalCompra.subtract(somaPagamentos);
        lblValorRestante.setText(String.format("Valor Restante: R$ %.2f", restante));
        btnConfirmar.setEnabled(restante.compareTo(BigDecimal.ZERO) == 0);
    }

    // Realiza a compra e registra os pagamentos
    private void realizarCompra() {
        try {
            BigDecimal somaPagamentos = pagamentos.stream().map(p -> p.valor).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (somaPagamentos.compareTo(valorTotalCompra) != 0) {
                throw new IllegalArgumentException("A soma dos pagamentos deve ser igual ao valor total da compra!");
            }
            Caixa caixa = null;
            boolean precisaCaixa = pagamentos.stream().anyMatch(p -> !p.metodo.equals("BOLETO"));
            if (precisaCaixa) {
                caixa = caixaController.getCaixaAberto();
                if (caixa == null) {
                    throw new IllegalStateException("Nenhum caixa aberto encontrado!");
                }
            }
            Compra compra = new Compra();
            compra.setUsuario(Sessao.getUsuarioLogado().getLogin());
            compra.setDataCompra(Timestamp.valueOf(LocalDateTime.now()));
            if (!compraController.criarCompra(compra, Sessao.getUsuarioLogado().getLogin())) {
                throw new SQLException("Falha ao registrar compra!");
            }
            int compraId = compra.getId();
            for (CompraProduto cp : itensCompra) {
                cp.setCompraId(compraId);
                if (!compraProdutoController.adicionarProdutoCompra(cp)) {
                    throw new SQLException("Falha ao registrar produto da compra!");
                }
                Estoque estoque = estoqueController.buscarPorProdutoId(cp.getProdutoId());
                if (estoque == null) {
                    estoque = new Estoque();
                    estoque.setProdutoId(cp.getProdutoId());
                    estoque.setQuantidade(0);
                    estoque.setEstoqueMinimo(0);
                }
                estoque.setQuantidade(estoque.getQuantidade() + cp.getQuantidade());
                estoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!estoqueController.salvarOuAtualizarEstoque(estoque, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao atualizar estoque!");
                }
                MovimentoEstoque movimento = new MovimentoEstoque();
                movimento.setProdutoId(cp.getProdutoId());
                movimento.setQuantidade(cp.getQuantidade());
                movimento.setTipo(MovimentoEstoque.Tipo.ENTRADA);
                movimento.setObservacoes("Entrada por compra ID " + compraId);
                movimento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!movimentoEstoqueController.registrarMovimento(movimento, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao registrar movimento de estoque!");
                }
            }
            String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
            for (PagamentoCompraTemp p : pagamentos) {
                if (p.metodo.equals("BOLETO")) {
                    BigDecimal valorParcela = p.valor.divide(BigDecimal.valueOf(p.parcelas), 2, BigDecimal.ROUND_HALF_UP);
                    for (int i = 1; i <= p.parcelas; i++) {
                        PagamentoCompra pagamento = new PagamentoCompra();
                        pagamento.setCompraId(compraId);
                        pagamento.setValor(valorParcela);
                        pagamento.setMetodoPagamento(PagamentoCompra.MetodoPagamento.valueOf(p.metodo));
                        pagamento.setParcela(i);
                        pagamento.setTotalParcelas(p.parcelas);
                        pagamento.setDataHora(Timestamp.valueOf(LocalDateTime.now()));
                        pagamento.setDataVencimento(java.sql.Date.valueOf(p.dataVencimentoInicial.plusMonths(i - 1)));
                        pagamento.setStatus(PagamentoCompra.StatusPagamento.PENDENTE);
                        pagamento.setObservacoes("Parcela " + i + " de " + p.parcelas + " do boleto");
                        pagamentoCompraController.registrarPagamento(pagamento, usuarioLogado);
                    }
                } else {
                    PagamentoCompra pagamento = new PagamentoCompra();
                    pagamento.setCompraId(compraId);
                    pagamento.setValor(p.valor);
                    pagamento.setMetodoPagamento(PagamentoCompra.MetodoPagamento.valueOf(p.metodo));
                    pagamento.setParcela(1);
                    pagamento.setTotalParcelas(1);
                    pagamento.setDataHora(Timestamp.valueOf(LocalDateTime.now()));
                    pagamento.setDataVencimento(java.sql.Date.valueOf(LocalDate.now()));
                    pagamento.setStatus(PagamentoCompra.StatusPagamento.PAGO);
                    pagamento.setObservacoes("Pagamento via " + p.metodo);
                    pagamentoCompraController.registrarPagamento(pagamento, usuarioLogado);
                    CaixaMovimento movimentoCaixa = new CaixaMovimento();
                    movimentoCaixa.setCaixa(caixa);
                    movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
                    movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_COMPRA);
                    movimentoCaixa.setPagamentoCompra(pagamento);
                    movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(p.metodo));
                    movimentoCaixa.setValor(p.valor);
                    movimentoCaixa.setDescricao("Pagamento à vista de compra ID " + compraId);
                    movimentoCaixa.setUsuario(usuarioLogado);
                    movimentoCaixa.setDataHora(LocalDateTime.now());
                    caixaMovimentoController.adicionarMovimento(movimentoCaixa);
                }
            }
            compraConcluida = true;
            JOptionPane.showMessageDialog(this, "Compra realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao realizar compra: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Retorna se a compra foi concluída
    public boolean isCompraConcluida() {
        return compraConcluida;
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
            while (digits.length() < 3) digits = "0" + digits;
            String cents = digits.substring(digits.length() - 2);
            String reais = digits.substring(0, digits.length() - 2);
            reais = reais.replaceFirst("^0+(?!$)", "");
            if (reais.isEmpty()) reais = "0";
            StringBuilder formattedReais = new StringBuilder();
            int count = 0;
            for (int i = reais.length() - 1; i >= 0; i--) {
                formattedReais.insert(0, reais.charAt(i));
                if (++count % 3 == 0 && i > 0) formattedReais.insert(0, ".");
            }
            return formattedReais + "," + cents;
        }
    }
}