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
class PagamentoVendaTemp {
    String metodo;
    BigDecimal valor;
    int parcelas;
    LocalDate dataVencimentoInicial;

    PagamentoVendaTemp(String metodo, BigDecimal valor, int parcelas, LocalDate dataVencimentoInicial) {
        this.metodo = metodo;
        this.valor = valor;
        this.parcelas = parcelas;
        this.dataVencimentoInicial = dataVencimentoInicial;
    }
}

// Diálogo para gerenciar múltiplos pagamentos de uma venda
public class CheckoutPagamentoDialog extends JDialog {
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
    private final Color primaryColor = new Color(34, 139, 34);
    private final Color secondaryColor = new Color(200, 255, 200);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color rowColorLightGreen = new Color(230, 255, 230);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final VendaController vendaController = new VendaController();
    private final VendaProdutoController vendaProdutoController = new VendaProdutoController();
    private final EstoqueController estoqueController = new EstoqueController();
    private final MovimentoEstoqueController movimentoEstoqueController = new MovimentoEstoqueController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController caixaMovimentoController = new CaixaMovimentoController();
    private final PagamentoVendaController pagamentoVendaController = new PagamentoVendaController();

    // Estado
    private final BigDecimal valorTotalVenda;
    private final List<VendaProduto> itensVenda;
    private final Paciente paciente;
    private final Atendimento atendimento;
    private final Orcamento orcamento;
    private final List<PagamentoVendaTemp> pagamentos = new ArrayList<>();
    private boolean vendaConcluida = false;
    private static final String[] FORMAS_PAGAMENTO = {"DINHEIRO", "PIX", "DEBITO", "CREDITO", "BOLETO"};

    public CheckoutPagamentoDialog(JFrame parentFrame, BigDecimal valorTotalVenda, List<VendaProduto> itensVenda,
                                  Paciente paciente, Atendimento atendimento, Orcamento orcamento) {
        super(parentFrame, "Checkout - Pagamentos", true);
        this.valorTotalVenda = valorTotalVenda;
        this.itensVenda = new ArrayList<>(itensVenda); // Cópia para evitar modificações externas
        this.paciente = paciente;
        this.atendimento = atendimento;
        this.orcamento = orcamento;
        setSize(600, 580);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);

        inicializarComponentes();
    }

    // Inicializa os componentes do diálogo
    private void inicializarComponentes() {
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(backgroundColor);

        // Painel de entrada de pagamento
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(backgroundColor);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                "Adicionar Pagamento",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                labelFont,
                primaryColor));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Método de pagamento
        JLabel lblMetodo = new JLabel("Forma pgto:");
        lblMetodo.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        inputPanel.add(lblMetodo, gbc);
        cbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO);
        cbMetodoPagamento.setPreferredSize(new Dimension(106, 25));
        cbMetodoPagamento.setFont(fieldFont);
        cbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(cbMetodoPagamento, gbc);

        // Valor do pagamento
        JLabel lblValor = new JLabel("Valor:");
        lblValor.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        inputPanel.add(lblValor, gbc);
        txtValorPagamento = new JTextField("0,00");
        txtValorPagamento.setPreferredSize(new Dimension(80, 25));
        txtValorPagamento.setFont(fieldFont);
        ((AbstractDocument) txtValorPagamento.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(txtValorPagamento, gbc);

        // Parcelas
        JLabel lblParcelas = new JLabel("Parcelas:");
        lblParcelas.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        inputPanel.add(lblParcelas, gbc);
        spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerParcelas.setPreferredSize(new Dimension(70, 25));
        spinnerParcelas.setFont(fieldFont);
        spinnerParcelas.setEnabled(false);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(spinnerParcelas, gbc);

        // Data de vencimento
        JLabel lblDataVencimento = new JLabel("1º Vencimento:");
        lblDataVencimento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        inputPanel.add(lblDataVencimento, gbc);
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            dateVencimentoInicial = new JFormattedTextField(dateMask);
            dateVencimentoInicial.setPreferredSize(new Dimension(120, 25));
            dateVencimentoInicial.setFont(fieldFont);
            dateVencimentoInicial.setEnabled(false);
            dateVencimentoInicial.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao configurar formato de data: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(dateVencimentoInicial, gbc);

        // Botões de ação
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
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
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        inputPanel.add(botoesPanel, gbc);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Tabela de pagamentos
        JPanel tabelaPanel = new JPanel(new BorderLayout());
        tabelaPanel.setBackground(backgroundColor);
        tabelaPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                "Pagamentos Adicionados",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                labelFont,
                primaryColor));
        String[] colunas = {"Método", "Valor", "Parcelas", "1º Vencimento"};
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
                if (isRowSelected(row)) {
                    c.setBackground(secondaryColor);
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1, column == getColumnCount() - 1 ? 1 : 0, Color.BLACK));
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }
                return c;
            }
        };
        tabelaPagamentos.setShowGrid(false);
        tabelaPagamentos.setIntercellSpacing(new Dimension(0, 0));
        tabelaPagamentos.setFillsViewportHeight(true);
        tabelaPagamentos.setRowHeight(25);
        tabelaPagamentos.setFont(fieldFont);
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
        tabelaPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(tabelaPanel, BorderLayout.CENTER);

        // Painel inferior
        JPanel southPanel = new JPanel(new GridBagLayout());
        southPanel.setBackground(backgroundColor);
        GridBagConstraints gbcSouth = new GridBagConstraints();
        gbcSouth.insets = new Insets(5, 5, 5, 5);
        gbcSouth.fill = GridBagConstraints.HORIZONTAL;

        // Valor restante
        lblValorRestante = new JLabel("Valor Restante: R$ " + String.format("%.2f", valorTotalVenda));
        lblValorRestante.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValorRestante.setForeground(primaryColor);
        gbcSouth.gridx = 0;
        gbcSouth.gridy = 0;
        gbcSouth.weightx = 1.0;
        southPanel.add(lblValorRestante, gbcSouth);

        // Botões Confirmar/Cancelar
        JPanel botoesConfirmarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoesConfirmarPanel.setBackground(backgroundColor);
        btnConfirmar = new JButton("Efetuar");
        btnConfirmar.setBackground(primaryColor);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(140, 30));
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> realizarVenda());
        botoesConfirmarPanel.add(btnConfirmar);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 30));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());
        botoesConfirmarPanel.add(btnCancelar);
        gbcSouth.gridx = 0;
        gbcSouth.gridy = 1;
        southPanel.add(botoesConfirmarPanel, gbcSouth);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // Configura calendário
        calendarPopup = new JPopupMenu();
        calendar = new JCalendar();
        calendar.setDecorationBackgroundColor(backgroundColor);
        calendar.setTodayButtonVisible(true);
        calendarPopup.add(calendar);
        calendarPopup.setPreferredSize(new Dimension(400, 300));
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
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Dimension popupSize = calendarPopup.getPreferredSize();
                    int x = (screenSize.width - popupSize.width) / 2;
                    int y = (screenSize.height - popupSize.height) / 2;
                    calendarPopup.show(dateVencimentoInicial, x - dateVencimentoInicial.getLocationOnScreen().x, y - dateVencimentoInicial.getLocationOnScreen().y);
                }
            }
        });

        // Listener para método de pagamento
        cbMetodoPagamento.addActionListener(e -> atualizarCamposPagamento());

        add(mainPanel, BorderLayout.CENTER);
        atualizarCamposPagamento();
        atualizarTabelaPagamentos();
    }

    // Atualiza os campos de parcelas e data de vencimento com base no método
    private void atualizarCamposPagamento() {
        String metodo = (String) cbMetodoPagamento.getSelectedItem();
        if ("BOLETO".equals(metodo)) {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 12, 1));
            spinnerParcelas.setEnabled(true);
            dateVencimentoInicial.setEnabled(true);
        } else if ("CREDITO".equals(metodo)) {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 12, 1));
            spinnerParcelas.setEnabled(true);
            dateVencimentoInicial.setEnabled(false);
        } else {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 1, 1));
            spinnerParcelas.setEnabled(false);
            dateVencimentoInicial.setEnabled(false);
        }
    }

    // Adiciona um pagamento à lista temporária
    private void adicionarPagamento() {
        try {
            String metodo = (String) cbMetodoPagamento.getSelectedItem();
            BigDecimal valor;
            try {
                String text = txtValorPagamento.getText().replace(".", "").replace(",", ".");
                valor = new BigDecimal(text);
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero!");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor do pagamento inválido!");
            }
            int parcelas = (Integer) spinnerParcelas.getValue();
            LocalDate dataVencimento = LocalDate.now();
            if ("BOLETO".equals(metodo)) {
                String dataText = dateVencimentoInicial.getText();
                try {
                    dataVencimento = LocalDate.parse(dataText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (dataVencimento.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("A data de vencimento inicial não pode ser anterior à data atual!");
                    }
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Data de vencimento inválida!");
                }
            }
            BigDecimal somaPagamentos = pagamentos.stream()
                    .map(p -> p.valor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (somaPagamentos.add(valor).compareTo(valorTotalVenda) > 0) {
                throw new IllegalArgumentException("O valor total dos pagamentos excede o valor da venda!");
            }
            pagamentos.add(new PagamentoVendaTemp(metodo, valor, parcelas, "BOLETO".equals(metodo) ? dataVencimento : null));
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
        for (PagamentoVendaTemp p : pagamentos) {
            String dataVencimento = p.dataVencimentoInicial != null
                    ? p.dataVencimentoInicial.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "-";
            modeloTabelaPagamentos.addRow(new Object[]{
                    p.metodo,
                    String.format("R$ %.2f", p.valor),
                    p.parcelas,
                    dataVencimento
            });
            somaPagamentos = somaPagamentos.add(p.valor);
        }
        BigDecimal restante = valorTotalVenda.subtract(somaPagamentos);
        lblValorRestante.setText(String.format("Valor Restante: R$ %.2f", restante));
        btnConfirmar.setEnabled(restante.compareTo(BigDecimal.ZERO) == 0);
    }

    // Realiza a venda e registra os pagamentos
    private void realizarVenda() {
        try {
            BigDecimal somaPagamentos = pagamentos.stream()
                    .map(p -> p.valor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (somaPagamentos.compareTo(valorTotalVenda) != 0) {
                throw new IllegalArgumentException("A soma dos pagamentos deve ser igual ao valor total da venda!");
            }
            // Verificar caixa aberto para métodos à vista (exceto BOLETO)
            Caixa caixa = null;
            boolean precisaCaixa = pagamentos.stream().anyMatch(p -> !p.metodo.equals("BOLETO"));
            if (precisaCaixa) {
                caixa = caixaController.getCaixaAberto();
                if (caixa == null) {
                    throw new IllegalStateException("Nenhum caixa aberto encontrado para pagamentos à vista!");
                }
            }
            // Registrar venda
            Venda venda = new Venda();
            venda.setPacienteId(paciente != null ? paciente.getId() : null);
            venda.setAtendimentoId(atendimento != null ? atendimento.getId() : null);
            venda.setOrcamentoId(orcamento != null ? orcamento.getId() : null);
            venda.setValorTotal(valorTotalVenda);
            venda.setUsuario(Sessao.getUsuarioLogado().getLogin());
            venda.setDataHora(Timestamp.valueOf(LocalDateTime.now()));
            if (!vendaController.registrarVenda(venda, Sessao.getUsuarioLogado().getLogin())) {
                throw new SQLException("Falha ao registrar venda!");
            }
            int vendaId = venda.getId();
            // Registrar produtos da venda
            for (VendaProduto vp : itensVenda) {
                vp.setVendaId(vendaId);
                if (!vendaProdutoController.adicionarProdutoVenda(vp)) {
                    throw new SQLException("Falha ao registrar produto da venda!");
                }
                Estoque estoque = estoqueController.buscarPorProdutoId(vp.getProdutoId());
                estoque.setQuantidade(estoque.getQuantidade() - vp.getQuantidade());
                estoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!estoqueController.salvarOuAtualizarEstoque(estoque, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao atualizar estoque!");
                }
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
            // Registrar pagamentos
            for (PagamentoVendaTemp p : pagamentos) {
                if (p.metodo.equals("BOLETO")) {
                    // Para BOLETO, registrar parcelas com datas de vencimento mensais
                    BigDecimal valorParcela = p.valor.divide(BigDecimal.valueOf(p.parcelas), 2, BigDecimal.ROUND_HALF_UP);
                    for (int i = 1; i <= p.parcelas; i++) {
                        PagamentoVenda pagamento = new PagamentoVenda();
                        pagamento.setVenda(venda);
                        pagamento.setValor(valorParcela);
                        pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(p.metodo));
                        pagamento.setParcela(i);
                        pagamento.setTotalParcelas(p.parcelas);
                        pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                        pagamento.setDataHora(LocalDateTime.now());
                        pagamento.setDataVencimento(p.dataVencimentoInicial.plusDays((i - 1) * 30));
                        pagamento.setStatus("PENDENTE"); // Pagamentos de boleto são pendentes
                        pagamento.setObservacoes("Parcela " + i + " de " + p.parcelas + " do boleto");
                        pagamentoVendaController.inserir(pagamento);
                    }
                } else {
                    // Para outros métodos (DINHEIRO, PIX, DEBITO, CREDITO), registrar um único pagamento
                    PagamentoVenda pagamento = new PagamentoVenda();
                    pagamento.setVenda(venda);
                    pagamento.setValor(p.valor);
                    pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(p.metodo));
                    pagamento.setParcela(1);
                    pagamento.setTotalParcelas(1); // Sempre 1 para métodos à vista e CREDITO
                    pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    pagamento.setDataHora(LocalDateTime.now());
                    pagamento.setDataVencimento(LocalDate.now());
                    pagamento.setStatus("PAGO"); // Pagamentos à vista são considerados pagos
                    pagamento.setObservacoes("Pagamento via " + p.metodo);
                    pagamentoVendaController.inserir(pagamento);
                    // Registrar movimento no caixa
                    CaixaMovimento movimentoCaixa = new CaixaMovimento();
                    movimentoCaixa.setCaixa(caixa);
                    movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
                    movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
                    movimentoCaixa.setPagamentoVenda(pagamento);
                    movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(p.metodo));
                    movimentoCaixa.setValor(p.valor);
                    movimentoCaixa.setDescricao("Pagamento à vista de venda ID " + vendaId);
                    movimentoCaixa.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    movimentoCaixa.setDataHora(LocalDateTime.now());
                    caixaMovimentoController.adicionarMovimento(movimentoCaixa);
                }
            }
            vendaConcluida = true;
            JOptionPane.showMessageDialog(this, "Venda realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao realizar venda: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Retorna se a venda foi concluída
    public boolean isVendaConcluida() {
        return vendaConcluida;
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