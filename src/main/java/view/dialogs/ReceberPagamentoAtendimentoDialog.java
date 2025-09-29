package view.dialogs;

import controller.AtendimentoController;
import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.PagamentoAtendimentoController;
import model.Atendimento;
import model.Caixa;
import model.CaixaMovimento;
import model.PagamentoAtendimento;
import util.Sessao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReceberPagamentoAtendimentoDialog extends JDialog {

    private final Atendimento atendimento;
    private final PagamentoAtendimentoController pagamentoController = new PagamentoAtendimentoController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController movimentoController = new CaixaMovimentoController();
    private final AtendimentoController atendimentoController = new AtendimentoController();

    private JTextField txtValorPagar;
    private JComboBox<PagamentoAtendimento.MetodoPagamento> cbMetodo;
    private JTextField txtRecebido; // Para dinheiro
    private JLabel lblTroco; // Para dinheiro
    private JTextArea txtObservacoes; // Renomeado para observações
    private BigDecimal valorRestante;

    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font boldFont = new Font("SansSerif", Font.BOLD, 16);

    public ReceberPagamentoAtendimentoDialog(EditarMarcacaoDialog editarMarcacaoDialog, Atendimento atendimento) {
        super(editarMarcacaoDialog, "Receber Pagamento", true);
        this.atendimento = atendimento;
        setSize(450, 450);
        setLocationRelativeTo(editarMarcacaoDialog);
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);

        calcularValorRestante();
        initComponents();
    }

    // Inicializa componentes da UI
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        // Painel de formulário com borda
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor), "Detalhes do Pagamento",
                TitledBorder.CENTER, TitledBorder.TOP, labelFont, primaryColor));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Valor total (maior e destacado)
        JLabel lblValorTotal = new JLabel("Valor Total: R$ " + String.format("%.2f", valorRestante).replace(".", ","));
        lblValorTotal.setFont(boldFont);
        lblValorTotal.setForeground(primaryColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblValorTotal, gbc);

        // Método de pagamento
        JLabel lblMetodo = new JLabel("Método:");
        lblMetodo.setFont(labelFont);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(lblMetodo, gbc);

        cbMetodo = new JComboBox<>(PagamentoAtendimento.MetodoPagamento.values());
        cbMetodo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão
        gbc.gridx = 1;
        formPanel.add(cbMetodo, gbc);

        // Valor a pagar
        JLabel lblValorPagar = new JLabel("Valor a Pagar:");
        lblValorPagar.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(lblValorPagar, gbc);

        txtValorPagar = createMoneyField(valorRestante);
        gbc.gridx = 1;
        formPanel.add(txtValorPagar, gbc);

        // Campos para dinheiro
        JLabel lblRecebido = new JLabel("Recebido:");
        lblRecebido.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblRecebido, gbc);

        txtRecebido = createMoneyField(BigDecimal.ZERO);
        gbc.gridx = 1;
        formPanel.add(txtRecebido, gbc);

        JLabel lblTrocoTitle = new JLabel("Troco:");
        lblTrocoTitle.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(lblTrocoTitle, gbc);

        lblTroco = new JLabel("R$ 0,00");
        gbc.gridx = 1;
        formPanel.add(lblTroco, gbc);

        // Observações
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(lblObservacoes, gbc);

        txtObservacoes = new JTextArea(3, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollObs, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Botões com espaçamento
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(backgroundColor);
        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.setBackground(primaryColor);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(100, 35));
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Cursor de mão
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnConfirmar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Listeners
        cbMetodo.addActionListener(e -> toggleCamposDinheiro());
        txtRecebido.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calcularTroco(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calcularTroco(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calcularTroco(); }
        });
        txtValorPagar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calcularTroco(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calcularTroco(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calcularTroco(); }
        });
        btnConfirmar.addActionListener(e -> confirmarPagamento());
        btnCancelar.addActionListener(e -> dispose());

        toggleCamposDinheiro(); // Inicializa visibilidade
    }

    // Cria campo de texto para entrada de moeda
    private JTextField createMoneyField(BigDecimal initialValue) {
        JTextField field = new JTextField();
        field.setText("R$ " + String.format("%.2f", initialValue).replace(".", ","));
        PlainDocument doc = (PlainDocument) field.getDocument();
        doc.setDocumentFilter(new CurrencyDocumentFilter());
        return field;
    }

    // DocumentFilter para formatar entrada de valores monetários com R$
    private static class CurrencyDocumentFilter extends DocumentFilter {
        private static final int MAX_LENGTH = 15; // Limite razoável para valores

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attrs) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, string);
            if (isValidInput(sb.toString()) && sb.length() <= MAX_LENGTH) {
                String formatted = formatCurrency(removeNonDigits(sb.toString()));
                super.replace(fb, 0, fb.getDocument().getLength(), formatted, attrs);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, string);
            if (isValidInput(sb.toString()) && sb.length() <= MAX_LENGTH) {
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
            return text.matches("[0-9,.\\sR$]*");
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

    // Calcula valor restante a pagar
    private void calcularValorRestante() {
        BigDecimal totalPago = BigDecimal.ZERO;
        try {
            List<PagamentoAtendimento> pagamentos = pagamentoController.buscarPorAtendimento(atendimento);
            for (PagamentoAtendimento p : pagamentos) {
                totalPago = totalPago.add(p.getValor());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao calcular pagamentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        valorRestante = atendimento.getValor().subtract(totalPago);
    }

    // Alterna visibilidade dos campos de dinheiro
    private void toggleCamposDinheiro() {
        boolean isDinheiro = cbMetodo.getSelectedItem() == PagamentoAtendimento.MetodoPagamento.DINHEIRO;
        txtRecebido.setVisible(isDinheiro);
        lblTroco.setVisible(isDinheiro);
        lblTroco.getParent().revalidate();
        lblTroco.getParent().repaint();
    }

    // Calcula troco
    private void calcularTroco() {
        try {
            String pagarStr = txtValorPagar.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
            String recebidoStr = txtRecebido.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
            BigDecimal pagar = new BigDecimal(pagarStr.isEmpty() ? "0" : pagarStr);
            BigDecimal recebido = new BigDecimal(recebidoStr.isEmpty() ? "0" : recebidoStr);
            BigDecimal troco = recebido.subtract(pagar);
            lblTroco.setText("R$ " + String.format("%.2f", troco.compareTo(BigDecimal.ZERO) >= 0 ? troco : BigDecimal.ZERO).replace(".", ","));
        } catch (NumberFormatException e) {
            lblTroco.setText("R$ 0,00");
        }
    }

    // Confirma e salva o pagamento
    private void confirmarPagamento() {
        try {
            // Validações
            String pagarStr = txtValorPagar.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
            BigDecimal valorPagar = new BigDecimal(pagarStr);
            if (valorPagar.compareTo(BigDecimal.ZERO) <= 0 || valorPagar.compareTo(valorRestante) > 0) {
                throw new IllegalArgumentException("Valor inválido.");
            }
            PagamentoAtendimento.MetodoPagamento metodo = (PagamentoAtendimento.MetodoPagamento) cbMetodo.getSelectedItem();
            BigDecimal valorRecebido = valorPagar;
            BigDecimal troco = BigDecimal.ZERO;
            if (metodo == PagamentoAtendimento.MetodoPagamento.DINHEIRO) {
                String recebidoStr = txtRecebido.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
                valorRecebido = new BigDecimal(recebidoStr);
                if (valorRecebido.compareTo(valorPagar) < 0) {
                    throw new IllegalArgumentException("Valor recebido insuficiente.");
                }
                troco = valorRecebido.subtract(valorPagar);
            }

            // Verifica caixa aberto
            if (!caixaController.existeCaixaAberto()) {
                throw new IllegalStateException("Nenhum caixa aberto.");
            }
            Caixa caixaAberto = caixaController.getCaixaAberto();

            // Verifica saldo para troco se necessário
            if (troco.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal saldoDinheiro = calcularSaldoDinheiro(caixaAberto);
                if (saldoDinheiro.compareTo(troco) < 0) {
                    throw new IllegalStateException("Saldo em dinheiro insuficiente para troco.");
                }
            }

            String observacoes = txtObservacoes.getText();

            // Cria PagamentoAtendimento
            PagamentoAtendimento pagamento = new PagamentoAtendimento();
            pagamento.setAtendimento(atendimento);
            pagamento.setValor(valorPagar);
            pagamento.setMetodoPagamento(metodo);
            pagamento.setObservacoes(observacoes);
            pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
            pagamentoController.salvarPagamento(pagamento);

            // Cria CaixaMovimento para entrada
            CaixaMovimento movimentoEntrada = new CaixaMovimento();
            movimentoEntrada.setCaixa(caixaAberto);
            movimentoEntrada.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
            movimentoEntrada.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_ATENDIMENTO);
            movimentoEntrada.setPagamentoAtendimento(pagamento);
            movimentoEntrada.setFormaPagamento(mapMetodoToForma(metodo));
            movimentoEntrada.setValor(valorRecebido);
            movimentoEntrada.setDescricao("Pagamento atendimento ID " + atendimento.getId() +
                    (observacoes.isEmpty() ? "" : " - " + observacoes));
            movimentoEntrada.setDataHora(LocalDateTime.now());
            movimentoEntrada.setUsuario(Sessao.getUsuarioLogado().getLogin());
            movimentoController.adicionarMovimento(movimentoEntrada);

            // Registra saída para troco se aplicável
            if (troco.compareTo(BigDecimal.ZERO) > 0) {
                CaixaMovimento movimentoTroco = new CaixaMovimento();
                movimentoTroco.setCaixa(caixaAberto);
                movimentoTroco.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
                movimentoTroco.setOrigem(CaixaMovimento.OrigemMovimento.AJUSTE);
                movimentoTroco.setFormaPagamento(CaixaMovimento.FormaPagamento.DINHEIRO);
                movimentoTroco.setValor(troco);
                movimentoTroco.setDescricao("Troco para pagamento atendimento ID " + atendimento.getId() +
                        (observacoes.isEmpty() ? "" : " - " + observacoes));
                movimentoTroco.setDataHora(LocalDateTime.now());
                movimentoTroco.setUsuario(Sessao.getUsuarioLogado().getLogin());
                movimentoController.adicionarMovimento(movimentoTroco);
            }

            // Atualiza status pagamento do atendimento
            atualizarStatusPagamento();

            JOptionPane.showMessageDialog(this, "Pagamento registrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar pagamento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Calcula saldo atual em dinheiro no caixa
    private BigDecimal calcularSaldoDinheiro(Caixa caixa) throws SQLException {
        BigDecimal saldo = caixa.getSaldoInicialDinheiro() != null ? caixa.getSaldoInicialDinheiro() : BigDecimal.ZERO;
        List<CaixaMovimento> movimentos = movimentoController.listarMovimentosPorCaixa(caixa.getId());
        for (CaixaMovimento m : movimentos) {
            if (m.getFormaPagamento() == CaixaMovimento.FormaPagamento.DINHEIRO) {
                if (m.getTipo() == CaixaMovimento.TipoMovimento.ENTRADA) {
                    saldo = saldo.add(m.getValor());
                } else if (m.getTipo() == CaixaMovimento.TipoMovimento.SAIDA) {
                    saldo = saldo.subtract(m.getValor());
                }
            }
        }
        return saldo;
    }

    // Mapeia MetodoPagamento para FormaPagamento
    private CaixaMovimento.FormaPagamento mapMetodoToForma(PagamentoAtendimento.MetodoPagamento metodo) {
        return switch (metodo) {
            case DINHEIRO -> CaixaMovimento.FormaPagamento.DINHEIRO;
            case PIX -> CaixaMovimento.FormaPagamento.PIX;
            case DEBITO -> CaixaMovimento.FormaPagamento.DEBITO;
            case CREDITO -> CaixaMovimento.FormaPagamento.CREDITO;
        };
    }

    // Atualiza status de pagamento do atendimento
    private void atualizarStatusPagamento() throws SQLException {
        BigDecimal totalPago = BigDecimal.ZERO;
        List<PagamentoAtendimento> pagamentos = pagamentoController.buscarPorAtendimento(atendimento);
        for (PagamentoAtendimento p : pagamentos) {
            totalPago = totalPago.add(p.getValor());
        }
        Atendimento.StatusPagamento novoStatus;
        if (totalPago.compareTo(atendimento.getValor()) >= 0) {
            novoStatus = Atendimento.StatusPagamento.PAGO;
        } else if (totalPago.compareTo(BigDecimal.ZERO) > 0) {
            novoStatus = Atendimento.StatusPagamento.PARCIAL;
        } else {
            novoStatus = Atendimento.StatusPagamento.PENDENTE;
        }
        atendimento.setStatusPagamento(novoStatus);
        atendimentoController.atualizarAtendimento(atendimento, Sessao.getUsuarioLogado().getLogin());
    }
}