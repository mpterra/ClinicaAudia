package view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import model.Despesa;
import controller.DespesaController;
import util.Sessao;
import view.LancamentoDespesaPanel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class EditarDespesaDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    // Componentes
    private JComboBox<Despesa.Categoria> cmbCategoria;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JCheckBox chkPago;
    private JComboBox<Despesa.FormaPagamento> cmbFormaPagamento;
    private JDateChooser dateChooserVencimento;
    private JDateChooser dateChooserPagamento;
    private JCheckBox chkRecorrente;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnCancelar;

    // Estilo
    private final Color primaryColor = new Color(154, 5, 38);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores e dados
    private final DespesaController despesaController = new DespesaController();
    private Despesa despesa;
    private final LancamentoDespesaPanel parentPanel;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public EditarDespesaDialog(LancamentoDespesaPanel parent, Despesa despesa) {
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Editar Despesa", true);
        this.parentPanel = parent;
        this.despesa = despesa;
        initComponents();
        preencherCampos();
        configurarListeners();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);
        setSize(400, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Painel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Componentes
        JLabel lblCategoria = new JLabel("Categoria:");
        lblCategoria.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(lblCategoria, gbc);

        cmbCategoria = new JComboBox<>(Despesa.Categoria.values());
        cmbCategoria.setFont(fieldFont);
        cmbCategoria.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(cmbCategoria, gbc);

        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainPanel.add(lblDescricao, gbc);

        txtDescricao = new JTextField(20);
        txtDescricao.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(txtDescricao, gbc);

        JLabel lblValor = new JLabel("Valor:");
        lblValor.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainPanel.add(lblValor, gbc);

        txtValor = new JTextField(10);
        txtValor.setFont(fieldFont);
        ((AbstractDocument) txtValor.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(txtValor, gbc);

        JLabel lblDataVencimento = new JLabel("Data Vencimento:");
        lblDataVencimento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        mainPanel.add(lblDataVencimento, gbc);

        dateChooserVencimento = new JDateChooser();
        dateChooserVencimento.setDateFormatString("dd/MM/yyyy");
        dateChooserVencimento.setFont(fieldFont);
        dateChooserVencimento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configurarCampoData((JTextFieldDateEditor) dateChooserVencimento.getDateEditor());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(dateChooserVencimento, gbc);

        JLabel lblPago = new JLabel("Pago?:");
        lblPago.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        mainPanel.add(lblPago, gbc);

        chkPago = new JCheckBox();
        chkPago.setBackground(backgroundColor);
        chkPago.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(chkPago, gbc);

        JLabel lblFormaPagamento = new JLabel("Forma Pagamento:");
        lblFormaPagamento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        mainPanel.add(lblFormaPagamento, gbc);

        cmbFormaPagamento = new JComboBox<>(Despesa.FormaPagamento.values());
        cmbFormaPagamento.setFont(fieldFont);
        cmbFormaPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(cmbFormaPagamento, gbc);

        JLabel lblDataPagamento = new JLabel("Data Pagamento:");
        lblDataPagamento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        mainPanel.add(lblDataPagamento, gbc);

        dateChooserPagamento = new JDateChooser();
        dateChooserPagamento.setDateFormatString("dd/MM/yyyy");
        dateChooserPagamento.setFont(fieldFont);
        dateChooserPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configurarCampoData((JTextFieldDateEditor) dateChooserPagamento.getDateEditor());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(dateChooserPagamento, gbc);

        JLabel lblRecorrente = new JLabel("Recorrente?:");
        lblRecorrente.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.0;
        mainPanel.add(lblRecorrente, gbc);

        chkRecorrente = new JCheckBox();
        chkRecorrente.setBackground(backgroundColor);
        chkRecorrente.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(chkRecorrente, gbc);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 30));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(btnSalvar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.LIGHT_GRAY);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setPreferredSize(new Dimension(100, 30));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(btnCancelar);

        btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(Color.RED);
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setPreferredSize(new Dimension(100, 30));
        btnExcluir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(btnExcluir);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void preencherCampos() {
        cmbCategoria.setSelectedItem(despesa.getCategoria());
        txtDescricao.setText(despesa.getDescricao());
        txtValor.setText(formatValorTabela(despesa.getValor()));
        dateChooserVencimento.setDate(Date.from(despesa.getDataVencimento().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        chkPago.setSelected(despesa.getStatus() == Despesa.Status.PAGO);
        cmbFormaPagamento.setSelectedItem(despesa.getFormaPagamento());
        dateChooserPagamento.setDate(despesa.getDataPagamento() != null ?
            Date.from(despesa.getDataPagamento().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null);
        chkRecorrente.setSelected(despesa.isRecorrente());
        atualizarCamposPagamento();
    }

    private void configurarListeners() {
        chkPago.addActionListener(e -> atualizarCamposPagamento());
        btnSalvar.addActionListener(e -> salvarAlteracoes());
        btnExcluir.addActionListener(e -> excluirDespesa());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void atualizarCamposPagamento() {
        boolean pago = chkPago.isSelected();
        cmbFormaPagamento.setEnabled(pago);
        dateChooserPagamento.setEnabled(pago);
        JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooserPagamento.getDateEditor();
        editor.setEnabled(pago);
        editor.setForeground(pago ? Color.BLACK : Color.GRAY);
        editor.setBackground(pago ? Color.WHITE : backgroundColor);
    }

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
    }

    private void salvarAlteracoes() {
        try {
            // Validações
            String descricao = txtDescricao.getText().trim();
            if (descricao.isEmpty()) throw new IllegalArgumentException("Descrição é obrigatória.");
            BigDecimal valor = parseValor(txtValor.getText());
            if (valor.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Valor deve ser maior que zero.");
            Date vencDate = dateChooserVencimento.getDate();
            if (vencDate == null) throw new IllegalArgumentException("Data de vencimento é obrigatória.");
            LocalDate dataVencimento = vencDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            boolean isPago = chkPago.isSelected();
            LocalDate dataPagamento = null;
            if (isPago) {
                Date pagDate = dateChooserPagamento.getDate();
                if (pagDate == null) throw new IllegalArgumentException("Data de pagamento é obrigatória.");
                dataPagamento = pagDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }
            // Atualizar despesa
            despesa.setDescricao(descricao);
            despesa.setCategoria((Despesa.Categoria) cmbCategoria.getSelectedItem());
            despesa.setValor(valor);
            despesa.setFormaPagamento((Despesa.FormaPagamento) cmbFormaPagamento.getSelectedItem());
            despesa.setDataVencimento(dataVencimento);
            despesa.setDataPagamento(dataPagamento);
            despesa.setStatus(isPago ? Despesa.Status.PAGO : Despesa.Status.PENDENTE);
            despesa.setRecorrente(chkRecorrente.isSelected());
            String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
            if (despesa.isRecorrente() && despesa.getStatus() != Despesa.Status.PAGO) {
                int opcao = JOptionPane.showOptionDialog(this,
                    "Editar apenas esta despesa ou também as futuras não pagas?",
                    "Despesa Recorrente",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Apenas esta", "Esta e futuras", "Cancelar"},
                    "Apenas esta");
                if (opcao == 2) return; // Cancelar
                if (opcao == 1) {
                    // Editar esta e futuras não pagas
                    despesaController.atualizar(despesa, usuarioLogado);
                    despesaController.atualizarRecorrentes(despesa, usuarioLogado, dataVencimento);
                } else {
                    // Editar apenas esta
                    despesaController.atualizar(despesa, usuarioLogado);
                }
            } else {
                despesaController.atualizar(despesa, usuarioLogado);
            }
            if (isPago && despesa.getDataPagamento() != null) {
                parentPanel.registrarMovimentoCaixa(despesa, usuarioLogado);
            }
            JOptionPane.showMessageDialog(this, "Despesa atualizada com sucesso!", "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            parentPanel.carregarDespesasFiltradas();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar despesa: " + ex.getMessage(), "Erro",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirDespesa() {
        try {
            if (JOptionPane.showConfirmDialog(this, "Confirmar exclusão da despesa ID " + despesa.getId() + "?", "Confirmação",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (despesa.getStatus() == Despesa.Status.PAGO) {
                    JOptionPane.showMessageDialog(this, "Despesa já paga não pode ser excluída.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (despesa.isRecorrente()) {
                    int opcao = JOptionPane.showOptionDialog(this,
                        "Excluir apenas esta despesa ou também as futuras não pagas?",
                        "Despesa Recorrente",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Apenas esta", "Esta e futuras", "Cancelar"},
                        "Apenas esta");
                    if (opcao == 2) return; // Cancelar
                    if (opcao == 1) {
                        // Excluir esta e futuras não pagas
                        despesaController.remover(despesa.getId());
                        despesaController.removerRecorrentes(despesa, despesa.getDataVencimento());
                    } else {
                        // Excluir apenas esta
                        despesaController.remover(despesa.getId());
                    }
                } else {
                    // Excluir apenas esta
                    despesaController.remover(despesa.getId());
                }
                JOptionPane.showMessageDialog(this, "Despesa(s) excluída(s) com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                parentPanel.carregarDespesasFiltradas();
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir despesa: " + ex.getMessage(), "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal parseValor(String text) {
        String cleaned = text.replace("R$ ", "").replace(".", "").replace(",", ".");
        return new BigDecimal(cleaned);
    }

    private String formatValorTabela(BigDecimal valor) {
        DecimalFormat df = new DecimalFormat("R$ #,##0.00");
        return df.format(valor);
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