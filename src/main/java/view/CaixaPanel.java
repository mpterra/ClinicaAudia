package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.text.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.Caixa;
import model.CaixaMovimento;
import controller.CaixaController;
import controller.CaixaMovimentoController;
import util.Sessao;

// Painel para gerenciamento de caixas
public class CaixaPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfSaldoInicialDinheiro, tfSaldoInicialCartaoDebito, tfSaldoInicialCartaoCredito, tfSaldoInicialPix, tfPesquisar;
    private JFormattedTextField tfDataAbertura;
    private JTextArea taObservacoes;
    private JTable tabelaMovimentos;
    private DefaultTableModel modeloTabelaMovimentos;
    private TableRowSorter<DefaultTableModel> sorterMovimentos;
    private JButton btnAbrirCaixa, btnFecharCaixa, btnLimpar, btnAjusteEntrada, btnAjusteSaida;
    private JLabel lblSaldoFinalDinheiro, lblSaldoFinalCartaoDebito, lblSaldoFinalCartaoCredito, lblSaldoFinalPix;

    // Estilo visual
    private final Color primaryColor = new Color(30, 144, 255);
    private final Color backgroundColor = new Color(245, 245, 245);
    private final Color rowColorLightBlue = new Color(230, 240, 255);
    private final Color cardBackground = new Color(255, 255, 255);
    private final Color positiveBalanceColor = new Color(34, 139, 34); // Verde para saldos positivos
    private final Color negativeBalanceColor = new Color(220, 20, 60); // Vermelho para saldos negativos
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 17);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 13);
    private final Font tableFont = new Font("SansSerif", Font.PLAIN, 13);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DecimalFormat decimalFormat = new DecimalFormat("R$ #,##0.00");

    // Caixa atual
    private Caixa caixaAtual;

    // Construtor
    public CaixaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 10, 10));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Gerenciamento de Caixa", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaMovimentosComPesquisa();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.51);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);
        add(splitPane, BorderLayout.CENTER);

        // Ajustar proporção inicial do JSplitPane
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.51));
        revalidate();
        repaint();

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnAbrirCaixa.addActionListener(e -> {
            try {
                abrirCaixa();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnFecharCaixa.addActionListener(e -> {
            try {
                fecharCaixa();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao fechar caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnAjusteEntrada.addActionListener(e -> {
            try {
                ajusteMovimento(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao registrar entrada: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnAjusteSaida.addActionListener(e -> {
            try {
                ajusteMovimento(false);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao registrar saída: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar caixa atual
        carregarCaixaAtual();
    }

    // Filtro para formatar entrada de valores monetários
    private class CurrencyDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("\\d*")) {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
                updateField(fb, newText);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (string.matches("\\d*")) {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string + currentText.substring(offset + length);
                updateField(fb, newText);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + currentText.substring(offset + length);
            updateField(fb, newText);
        }

        private void updateField(FilterBypass fb, String text) throws BadLocationException {
            String cleanedText = text.replaceAll("[^0-9]", "");
            if (cleanedText.isEmpty()) {
                fb.replace(0, fb.getDocument().getLength(), decimalFormat.format(0), null);
                return;
            }
            try {
                long cents = Long.parseLong(cleanedText);
                BigDecimal value = new BigDecimal(cents).divide(new BigDecimal(100));
                fb.replace(0, fb.getDocument().getLength(), decimalFormat.format(value), null);
            } catch (NumberFormatException e) {
                fb.replace(0, fb.getDocument().getLength(), decimalFormat.format(0), null);
            }
        }
    }

    // Cria painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Painel de saldos finais
        JPanel panelSaldos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelSaldos.setBackground(backgroundColor);
        panelSaldos.add(criarCardSaldo("Saldo Dinheiro", lblSaldoFinalDinheiro = new JLabel(decimalFormat.format(0))));
        panelSaldos.add(criarCardSaldo("Saldo Débito", lblSaldoFinalCartaoDebito = new JLabel(decimalFormat.format(0))));
        panelSaldos.add(criarCardSaldo("Saldo Crédito", lblSaldoFinalCartaoCredito = new JLabel(decimalFormat.format(0))));
        panelSaldos.add(criarCardSaldo("Saldo PIX", lblSaldoFinalPix = new JLabel(decimalFormat.format(0))));

        // Painel de informações do caixa
        JPanel panelCaixa = new JPanel(new GridBagLayout());
        panelCaixa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Gerenciar Caixa",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panelCaixa.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Filtro para campos monetários
        CurrencyDocumentFilter currencyFilter = new CurrencyDocumentFilter();

        // Data Abertura
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblDataAbertura = new JLabel("Data Abertura:");
        lblDataAbertura.setFont(labelFont);
        panelCaixa.add(lblDataAbertura, gbc);
        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/#### ##:##");
            dataMask.setPlaceholderCharacter('_');
            tfDataAbertura = new JFormattedTextField(dataMask);
            tfDataAbertura.setPreferredSize(new Dimension(150, 25));
            tfDataAbertura.setValue(LocalDateTime.now().format(formatter));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfDataAbertura, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Saldo Inicial Dinheiro
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblSaldoInicialDinheiro = new JLabel("Saldo Inicial Dinheiro:");
        lblSaldoInicialDinheiro.setFont(labelFont);
        panelCaixa.add(lblSaldoInicialDinheiro, gbc);
        tfSaldoInicialDinheiro = new JTextField(10);
        tfSaldoInicialDinheiro.setPreferredSize(new Dimension(100, 25));
        ((AbstractDocument) tfSaldoInicialDinheiro.getDocument()).setDocumentFilter(currencyFilter);
        tfSaldoInicialDinheiro.setText(decimalFormat.format(0)); // Inicializa com R$ 0,00
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfSaldoInicialDinheiro, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Saldo Inicial Cartão Débito
        gbc.gridx = 2;
        gbc.gridy = 1;
        JLabel lblSaldoInicialCartaoDebito = new JLabel("Saldo Inicial Cartão Débito:");
        lblSaldoInicialCartaoDebito.setFont(labelFont);
        panelCaixa.add(lblSaldoInicialCartaoDebito, gbc);
        tfSaldoInicialCartaoDebito = new JTextField(10);
        tfSaldoInicialCartaoDebito.setPreferredSize(new Dimension(100, 25));
        ((AbstractDocument) tfSaldoInicialCartaoDebito.getDocument()).setDocumentFilter(currencyFilter);
        tfSaldoInicialCartaoDebito.setText(decimalFormat.format(0)); // Inicializa com R$ 0,00
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfSaldoInicialCartaoDebito, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Saldo Inicial Cartão Crédito
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblSaldoInicialCartaoCredito = new JLabel("Saldo Inicial Cartão Crédito:");
        lblSaldoInicialCartaoCredito.setFont(labelFont);
        panelCaixa.add(lblSaldoInicialCartaoCredito, gbc);
        tfSaldoInicialCartaoCredito = new JTextField(10);
        tfSaldoInicialCartaoCredito.setPreferredSize(new Dimension(100, 25));
        ((AbstractDocument) tfSaldoInicialCartaoCredito.getDocument()).setDocumentFilter(currencyFilter);
        tfSaldoInicialCartaoCredito.setText(decimalFormat.format(0)); // Inicializa com R$ 0,00
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfSaldoInicialCartaoCredito, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Saldo Inicial PIX
        gbc.gridx = 2;
        gbc.gridy = 2;
        JLabel lblSaldoInicialPix = new JLabel("Saldo Inicial PIX:");
        lblSaldoInicialPix.setFont(labelFont);
        panelCaixa.add(lblSaldoInicialPix, gbc);
        tfSaldoInicialPix = new JTextField(10);
        tfSaldoInicialPix.setPreferredSize(new Dimension(100, 25));
        ((AbstractDocument) tfSaldoInicialPix.getDocument()).setDocumentFilter(currencyFilter);
        tfSaldoInicialPix.setText(decimalFormat.format(0)); // Inicializa com R$ 0,00
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfSaldoInicialPix, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Observações
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        panelCaixa.add(lblObservacoes, gbc);
        taObservacoes = new JTextArea(3, 20);
        taObservacoes.setLineWrap(true);
        taObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(taObservacoes);
        scrollObservacoes.setPreferredSize(new Dimension(250, 60));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(scrollObservacoes, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotoes.setBackground(backgroundColor);
        btnAbrirCaixa = new JButton("Abrir Caixa");
        btnAbrirCaixa.setBackground(primaryColor);
        btnAbrirCaixa.setForeground(Color.WHITE);
        btnAbrirCaixa.setPreferredSize(new Dimension(120, 30));
        btnAbrirCaixa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFecharCaixa = new JButton("Fechar Caixa");
        btnFecharCaixa.setBackground(primaryColor);
        btnFecharCaixa.setForeground(Color.WHITE);
        btnFecharCaixa.setPreferredSize(new Dimension(120, 30));
        btnFecharCaixa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAjusteEntrada = new JButton("Ajuste Entrada");
        btnAjusteEntrada.setBackground(new Color(34, 139, 34));
        btnAjusteEntrada.setForeground(Color.WHITE);
        btnAjusteEntrada.setPreferredSize(new Dimension(120, 30));
        btnAjusteEntrada.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAjusteSaida = new JButton("Ajuste Saída");
        btnAjusteSaida.setBackground(new Color(220, 20, 60));
        btnAjusteSaida.setForeground(Color.WHITE);
        btnAjusteSaida.setPreferredSize(new Dimension(120, 30));
        btnAjusteSaida.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(80, 30));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnAbrirCaixa);
        panelBotoes.add(btnFecharCaixa);
        panelBotoes.add(btnAjusteEntrada);
        panelBotoes.add(btnAjusteSaida);
        panelBotoes.add(btnLimpar);

        panelWrapper.add(panelSaldos);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelCaixa);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelBotoes);

        return panelWrapper;
    }

    // Cria card para saldo com cores condicionais
    private JPanel criarCardSaldo(String titulo, JLabel lblSaldo) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(cardBackground);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        card.setPreferredSize(new Dimension(150, 60));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(labelFont);
        lblTitulo.setForeground(primaryColor);
        card.add(lblTitulo, BorderLayout.NORTH);

        lblSaldo.setFont(labelFont);
        lblSaldo.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblSaldo, BorderLayout.CENTER);

        // Listener para atualizar a cor com base no valor do saldo
        lblSaldo.addPropertyChangeListener("text", evt -> {
            String text = lblSaldo.getText();
            try {
                BigDecimal value = parseCurrency(text);
                if (value != null) {
                    lblSaldo.setForeground(value.compareTo(BigDecimal.ZERO) < 0 ? negativeBalanceColor : positiveBalanceColor);
                } else {
                    lblSaldo.setForeground(positiveBalanceColor); // Zero ou inválido é tratado como positivo
                }
            } catch (Exception e) {
                lblSaldo.setForeground(positiveBalanceColor);
            }
        });

        return card;
    }

    // Cria tabela de movimentos com pesquisa
    private JPanel criarTabelaMovimentosComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Movimentações do Caixa",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Movimento:");
        lblPesquisar.setFont(labelFont);
        tfPesquisar = new JTextField(15);
        tfPesquisar.setPreferredSize(new Dimension(150, 25));
        panelBusca.add(lblPesquisar);
        panelBusca.add(tfPesquisar);

        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                sorterMovimentos.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Tabela de Movimentos
        String[] colunas = {"Data/Hora", "Tipo", "Origem", "Forma Pagamento", "Valor", "Descrição"};
        modeloTabelaMovimentos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaMovimentos = new JTable(modeloTabelaMovimentos);
        tabelaMovimentos.setRowHeight(20);
        tabelaMovimentos.setShowGrid(false);
        tabelaMovimentos.setIntercellSpacing(new Dimension(0, 0));
        tabelaMovimentos.setFont(tableFont);

        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? rowColorLightBlue : Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < tabelaMovimentos.getColumnCount(); i++) {
            tabelaMovimentos.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaMovimentos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorterMovimentos = new TableRowSorter<>(modeloTabelaMovimentos);
        tabelaMovimentos.setRowSorter(sorterMovimentos);

        JScrollPane scrollTabela = new JScrollPane(tabelaMovimentos);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);

        return panel;
    }

    // Limpa campos do formulário apenas se não houver caixa aberto
    private void limparCampos() {
        if (caixaAtual != null) {
            JOptionPane.showMessageDialog(this, "Não é possível limpar campos com caixa aberto!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tfSaldoInicialDinheiro.setText(decimalFormat.format(0));
        tfSaldoInicialCartaoDebito.setText(decimalFormat.format(0));
        tfSaldoInicialCartaoCredito.setText(decimalFormat.format(0));
        tfSaldoInicialPix.setText(decimalFormat.format(0));
        tfDataAbertura.setValue(LocalDateTime.now().format(formatter));
        taObservacoes.setText("");
        modeloTabelaMovimentos.setRowCount(0);
        lblSaldoFinalDinheiro.setText(decimalFormat.format(0));
        lblSaldoFinalCartaoDebito.setText(decimalFormat.format(0));
        lblSaldoFinalCartaoCredito.setText(decimalFormat.format(0));
        lblSaldoFinalPix.setText(decimalFormat.format(0));
        btnAbrirCaixa.setEnabled(true);
        btnFecharCaixa.setEnabled(false);
        btnAjusteEntrada.setEnabled(false);
        btnAjusteSaida.setEnabled(false);
        btnLimpar.setEnabled(true);
        tfSaldoInicialDinheiro.setEditable(true);
        tfSaldoInicialCartaoDebito.setEditable(true);
        tfSaldoInicialCartaoCredito.setEditable(true);
        tfSaldoInicialPix.setEditable(true);
        tfDataAbertura.setEditable(true);
        taObservacoes.setEditable(true);
    }

    // Converte texto formatado para BigDecimal
    private BigDecimal parseCurrency(String text) {
        try {
            String cleanedText = text.replaceAll("[^0-9,-]", "").replace(",", ".");
            return new BigDecimal(cleanedText);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Abre novo caixa
    private void abrirCaixa() throws SQLException {
        CaixaController controller = new CaixaController();
        if (controller.existeCaixaAberto()) {
            JOptionPane.showMessageDialog(this, "Já existe um caixa aberto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal saldoDinheiro = parseCurrency(tfSaldoInicialDinheiro.getText());
        BigDecimal saldoCartaoDebito = parseCurrency(tfSaldoInicialCartaoDebito.getText());
        BigDecimal saldoCartaoCredito = parseCurrency(tfSaldoInicialCartaoCredito.getText());
        BigDecimal saldoPix = parseCurrency(tfSaldoInicialPix.getText());
        Object dataAberturaObj = tfDataAbertura.getValue();
        String observacoes = taObservacoes.getText().trim();

        if (saldoDinheiro == null || saldoCartaoDebito == null || saldoCartaoCredito == null || saldoPix == null || dataAberturaObj == null) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String dataAberturaStr = dataAberturaObj.toString().trim();
            LocalDateTime dataAbertura = LocalDateTime.parse(dataAberturaStr, formatter);

            Caixa caixa = new Caixa();
            caixa.setSaldoInicialDinheiro(saldoDinheiro);
            caixa.setSaldoInicialDebito(saldoCartaoDebito);
            caixa.setSaldoInicialCredito(saldoCartaoCredito);
            caixa.setSaldoInicialPix(saldoPix);
            caixa.setDataAbertura(dataAbertura);
            caixa.setObservacoes(observacoes.isEmpty() ? null : observacoes);
            caixa.setUsuario(Sessao.getUsuarioLogado().getLogin());

            if (controller.abrirCaixa(caixa)) {
                JOptionPane.showMessageDialog(this, "Caixa aberto com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                caixaAtual = controller.getCaixaAberto();
                btnAbrirCaixa.setEnabled(false);
                btnFecharCaixa.setEnabled(true);
                btnAjusteEntrada.setEnabled(true);
                btnAjusteSaida.setEnabled(true);
                btnLimpar.setEnabled(false);
                tfSaldoInicialDinheiro.setEditable(false);
                tfSaldoInicialCartaoDebito.setEditable(false);
                tfSaldoInicialCartaoCredito.setEditable(false);
                tfSaldoInicialPix.setEditable(false);
                tfDataAbertura.setEditable(false);
                taObservacoes.setEditable(false);
                carregarMovimentos();
                atualizarSaldosFinais();
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido! Use dd/MM/yyyy HH:mm.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores de saldo inválidos! Use formato numérico (ex: 100.00).", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Fecha caixa atual
    private void fecharCaixa() throws SQLException, IllegalArgumentException, IllegalStateException {
        if (caixaAtual == null) {
            JOptionPane.showMessageDialog(this, "Nenhum caixa aberto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calcula saldos finais com base nos movimentos
        CaixaMovimentoController movimentoController = new CaixaMovimentoController();
        BigDecimal[] saldos = movimentoController.calcularSaldosFinais(caixaAtual.getId());
        caixaAtual.setSaldoFinalDinheiro(saldos[0]);
        caixaAtual.setSaldoFinalDebito(saldos[1]);
        caixaAtual.setSaldoFinalCredito(saldos[2]);
        caixaAtual.setSaldoFinalPix(saldos[3]);
        caixaAtual.setDataFechamento(LocalDateTime.now());

        CaixaController controller = new CaixaController();
        if (controller.fecharCaixa(caixaAtual)) {
            JOptionPane.showMessageDialog(this, "Caixa fechado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            caixaAtual = null;
            carregarCaixaAtual();
        }
    }

    // Carrega caixa atual e movimentações
    private void carregarCaixaAtual() {
        try {
            CaixaController controller = new CaixaController();
            caixaAtual = controller.getCaixaAberto();
            if (caixaAtual != null) {
                tfSaldoInicialDinheiro.setText(decimalFormat.format(caixaAtual.getSaldoInicialDinheiro()));
                tfSaldoInicialCartaoDebito.setText(decimalFormat.format(caixaAtual.getSaldoInicialDebito()));
                tfSaldoInicialCartaoCredito.setText(decimalFormat.format(caixaAtual.getSaldoInicialCredito()));
                tfSaldoInicialPix.setText(decimalFormat.format(caixaAtual.getSaldoInicialPix()));
                tfDataAbertura.setValue(caixaAtual.getDataAbertura().format(formatter));
                taObservacoes.setText(caixaAtual.getObservacoes() != null ? caixaAtual.getObservacoes() : "");
                tfSaldoInicialDinheiro.setEditable(false);
                tfSaldoInicialCartaoDebito.setEditable(false);
                tfSaldoInicialCartaoCredito.setEditable(false);
                tfSaldoInicialPix.setEditable(false);
                tfDataAbertura.setEditable(false);
                taObservacoes.setEditable(false);
                btnAbrirCaixa.setEnabled(false);
                btnFecharCaixa.setEnabled(true);
                btnAjusteEntrada.setEnabled(true);
                btnAjusteSaida.setEnabled(true);
                btnLimpar.setEnabled(false);
                carregarMovimentos();
                atualizarSaldosFinais();
            } else {
                tfDataAbertura.setValue(LocalDateTime.now().format(formatter));
                tfSaldoInicialDinheiro.setText(decimalFormat.format(0));
                tfSaldoInicialCartaoDebito.setText(decimalFormat.format(0));
                tfSaldoInicialCartaoCredito.setText(decimalFormat.format(0));
                tfSaldoInicialPix.setText(decimalFormat.format(0));
                lblSaldoFinalDinheiro.setText(decimalFormat.format(0));
                lblSaldoFinalCartaoDebito.setText(decimalFormat.format(0));
                lblSaldoFinalCartaoCredito.setText(decimalFormat.format(0));
                lblSaldoFinalPix.setText(decimalFormat.format(0));
                btnAbrirCaixa.setEnabled(true);
                btnFecharCaixa.setEnabled(false);
                btnAjusteEntrada.setEnabled(false);
                btnAjusteSaida.setEnabled(false);
                btnLimpar.setEnabled(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega movimentações do caixa atual
    private void carregarMovimentos() {
        if (caixaAtual == null) return;
        try {
            CaixaMovimentoController controller = new CaixaMovimentoController();
            List<CaixaMovimento> movimentos = controller.listarMovimentosPorCaixa(caixaAtual.getId());
            modeloTabelaMovimentos.setRowCount(0);
            for (CaixaMovimento mov : movimentos) {
                modeloTabelaMovimentos.addRow(new Object[]{
                        mov.getDataHora().format(formatter),
                        mov.getTipo().toString(),
                        mov.getOrigem().toString(),
                        mov.getFormaPagamento().toString(),
                        String.format("R$ %.2f", mov.getValor()),
                        mov.getDescricao() != null ? mov.getDescricao() : ""
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar movimentações: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza saldos finais
    private void atualizarSaldosFinais() {
        if (caixaAtual == null) return;
        try {
            CaixaMovimentoController controller = new CaixaMovimentoController();
            BigDecimal[] saldos = controller.calcularSaldosFinais(caixaAtual.getId());
            lblSaldoFinalDinheiro.setText(decimalFormat.format(saldos[0]));
            lblSaldoFinalCartaoDebito.setText(decimalFormat.format(saldos[1]));
            lblSaldoFinalCartaoCredito.setText(decimalFormat.format(saldos[2]));
            lblSaldoFinalPix.setText(decimalFormat.format(saldos[3]));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao calcular saldos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Registra ajuste de movimento
    private void ajusteMovimento(boolean isEntrada) throws SQLException {
        if (caixaAtual == null) {
            JOptionPane.showMessageDialog(this, "Nenhum caixa aberto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] formas = {"DINHEIRO", "DEBITO", "CREDITO", "PIX"};
        JComboBox<String> cbFormaPagamento = new JComboBox<>(formas);
        JTextField tfValor = new JTextField(10);
        tfValor.setPreferredSize(new Dimension(100, 25));
        ((AbstractDocument) tfValor.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        tfValor.setText(decimalFormat.format(0)); // Inicializa com R$ 0,00
        JTextField tfDescricao = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Forma de Pagamento:"));
        panel.add(cbFormaPagamento);
        panel.add(new JLabel("Valor:"));
        panel.add(tfValor);
        panel.add(new JLabel("Descrição:"));
        panel.add(tfDescricao);

        String tipoMovimento = isEntrada ? "Entrada" : "Saída";
        int result = JOptionPane.showConfirmDialog(this, panel, "Ajuste de " + tipoMovimento, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        BigDecimal valor = parseCurrency(tfValor.getText());
        String descricao = tfDescricao.getText().trim();
        if (valor == null) {
            JOptionPane.showMessageDialog(this, "Preencha o valor!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Valor deve ser maior que zero!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CaixaMovimento movimento = new CaixaMovimento();
        movimento.setCaixa(caixaAtual);
        movimento.setTipo(isEntrada ? CaixaMovimento.TipoMovimento.ENTRADA : CaixaMovimento.TipoMovimento.SAIDA);
        movimento.setOrigem(CaixaMovimento.OrigemMovimento.AJUSTE);
        movimento.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(cbFormaPagamento.getSelectedItem().toString()));
        movimento.setValor(valor);
        movimento.setDescricao(descricao.isEmpty() ? "Ajuste de " + tipoMovimento : descricao);
        movimento.setDataHora(LocalDateTime.now());
        movimento.setUsuario(Sessao.getUsuarioLogado().getLogin());

        CaixaMovimentoController controller = new CaixaMovimentoController();
        controller.adicionarMovimento(movimento);
        JOptionPane.showMessageDialog(this, tipoMovimento + " registrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        carregarMovimentos();
        atualizarSaldosFinais();
    }
}