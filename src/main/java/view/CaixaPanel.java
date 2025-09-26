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
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private JTextField tfSaldoInicialDinheiro, tfSaldoInicialCartao, tfSaldoInicialPix, tfPesquisar;
    private JFormattedTextField tfDataAbertura;
    private JTextArea taObservacoes;
    private JTable tabelaMovimentos;
    private DefaultTableModel modeloTabelaMovimentos;
    private TableRowSorter<DefaultTableModel> sorterMovimentos;
    private JButton btnAbrirCaixa, btnFecharCaixa, btnLimpar;
    private JButton btnAjusteEntrada, btnAjusteSaida;
    private JLabel lblSaldoFinalDinheiro, lblSaldoFinalCartao, lblSaldoFinalPix;

    // Estilo visual
    private final Color primaryColor = new Color(30, 144, 255); // Azul
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightBlue = new Color(230, 240, 255); // Azul claro para linhas pares
    private final Color cardBackground = new Color(255, 255, 255); // Fundo dos cards
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 17);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 13);
    private final Font tableFont = new Font("SansSerif", Font.PLAIN, 13);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        splitPane.setResizeWeight(0.48);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Garantir proporção inicial do JSplitPane
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.48));
        revalidate();
        repaint();

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnAbrirCaixa.addActionListener(e -> {
            try {
                abrirCaixa();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnFecharCaixa.addActionListener(e -> {
            try {
                fecharCaixa();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao fechar caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnAjusteEntrada.addActionListener(e -> {
            try {
                ajusteMovimento(true); // Entrada
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao registrar entrada: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnAjusteSaida.addActionListener(e -> {
            try {
                ajusteMovimento(false); // Saída
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao registrar saída: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar caixa atual
        carregarCaixaAtual();
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Painel de saldos finais (cards)
        JPanel panelSaldos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelSaldos.setBackground(backgroundColor);
        panelSaldos.add(criarCardSaldo("Saldo Final Dinheiro", lblSaldoFinalDinheiro = new JLabel("R$ 0,00")));
        panelSaldos.add(criarCardSaldo("Saldo Final Cartão", lblSaldoFinalCartao = new JLabel("R$ 0,00")));
        panelSaldos.add(criarCardSaldo("Saldo Final PIX", lblSaldoFinalPix = new JLabel("R$ 0,00")));

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
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfSaldoInicialDinheiro, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Saldo Inicial Cartão
        gbc.gridx = 2;
        gbc.gridy = 1;
        JLabel lblSaldoInicialCartao = new JLabel("Saldo Inicial Cartão:");
        lblSaldoInicialCartao.setFont(labelFont);
        panelCaixa.add(lblSaldoInicialCartao, gbc);
        tfSaldoInicialCartao = new JTextField(10);
        tfSaldoInicialCartao.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCaixa.add(tfSaldoInicialCartao, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Saldo Inicial PIX
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblSaldoInicialPix = new JLabel("Saldo Inicial PIX:");
        lblSaldoInicialPix.setFont(labelFont);
        panelCaixa.add(lblSaldoInicialPix, gbc);
        tfSaldoInicialPix = new JTextField(10);
        tfSaldoInicialPix.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1;
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
        btnAjusteEntrada.setBackground(new Color(34, 139, 34)); // Verde
        btnAjusteEntrada.setForeground(Color.WHITE);
        btnAjusteEntrada.setPreferredSize(new Dimension(120, 30));
        btnAjusteEntrada.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAjusteSaida = new JButton("Ajuste Saída");
        btnAjusteSaida.setBackground(new Color(220, 20, 60)); // Vermelho
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

    // Cria um card para exibir saldo
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

        return card;
    }

    // Cria o painel da tabela de movimentos com pesquisa
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

    // Limpa os campos do formulário
    private void limparCampos() {
        tfSaldoInicialDinheiro.setText("");
        tfSaldoInicialCartao.setText("");
        tfSaldoInicialPix.setText("");
        tfDataAbertura.setText("");
        taObservacoes.setText("");
        modeloTabelaMovimentos.setRowCount(0);
        lblSaldoFinalDinheiro.setText("R$ 0,00");
        lblSaldoFinalCartao.setText("R$ 0,00");
        lblSaldoFinalPix.setText("R$ 0,00");
        btnAbrirCaixa.setEnabled(true);
        btnFecharCaixa.setEnabled(false);
        btnAjusteEntrada.setEnabled(false);
        btnAjusteSaida.setEnabled(false);
        tfSaldoInicialDinheiro.setEditable(true);
        tfSaldoInicialCartao.setEditable(true);
        tfSaldoInicialPix.setEditable(true);
        tfDataAbertura.setEditable(true);
        taObservacoes.setEditable(true);
    }

    // Abre um novo caixa
    private void abrirCaixa() throws SQLException {
        CaixaController controller = new CaixaController();
        if (controller.existeCaixaAberto()) {
            JOptionPane.showMessageDialog(this, "Já existe um caixa aberto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String saldoDinheiroStr = tfSaldoInicialDinheiro.getText().trim();
        String saldoCartaoStr = tfSaldoInicialCartao.getText().trim();
        String saldoPixStr = tfSaldoInicialPix.getText().trim();
        String dataAberturaStr = tfDataAbertura.getText().trim();
        String observacoes = taObservacoes.getText().trim();

        // Validações
        if (saldoDinheiroStr.isEmpty() || saldoCartaoStr.isEmpty() || saldoPixStr.isEmpty() || dataAberturaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal saldoDinheiro = new BigDecimal(saldoDinheiroStr.replace(",", "."));
            BigDecimal saldoCartao = new BigDecimal(saldoCartaoStr.replace(",", "."));
            BigDecimal saldoPix = new BigDecimal(saldoPixStr.replace(",", "."));
            LocalDateTime dataAbertura = LocalDateTime.parse(dataAberturaStr, formatter);

            Caixa caixa = new Caixa();
            caixa.setSaldoInicialDinheiro(saldoDinheiro);
            caixa.setSaldoInicialCartao(saldoCartao);
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
                tfSaldoInicialDinheiro.setEditable(false);
                tfSaldoInicialCartao.setEditable(false);
                tfSaldoInicialPix.setEditable(false);
                tfDataAbertura.setEditable(false);
                taObservacoes.setEditable(false);
                carregarMovimentos();
                atualizarSaldosFinais();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores de saldo inválidos! Use formato numérico (ex: 100.00).", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido! Use dd/MM/yyyy HH:mm.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Fecha o caixa atual
    private void fecharCaixa() throws SQLException {
        if (caixaAtual == null) {
            JOptionPane.showMessageDialog(this, "Nenhum caixa aberto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CaixaController controller = new CaixaController();
        caixaAtual.setDataFechamento(LocalDateTime.now());
        if (controller.fecharCaixa(caixaAtual)) {
            JOptionPane.showMessageDialog(this, "Caixa fechado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            caixaAtual = null;
            limparCampos();
        }
    }

    // Carrega o caixa atual e suas movimentações
    private void carregarCaixaAtual() {
        try {
            CaixaController controller = new CaixaController();
            caixaAtual = controller.getCaixaAberto();
            if (caixaAtual != null) {
                tfSaldoInicialDinheiro.setText(String.format("%.2f", caixaAtual.getSaldoInicialDinheiro()));
                tfSaldoInicialCartao.setText(String.format("%.2f", caixaAtual.getSaldoInicialCartao()));
                tfSaldoInicialPix.setText(String.format("%.2f", caixaAtual.getSaldoInicialPix()));
                tfDataAbertura.setText(caixaAtual.getDataAbertura().format(formatter));
                taObservacoes.setText(caixaAtual.getObservacoes() != null ? caixaAtual.getObservacoes() : "");
                tfSaldoInicialDinheiro.setEditable(false);
                tfSaldoInicialCartao.setEditable(false);
                tfSaldoInicialPix.setEditable(false);
                tfDataAbertura.setEditable(false);
                taObservacoes.setEditable(false);
                btnAbrirCaixa.setEnabled(false);
                btnFecharCaixa.setEnabled(true);
                btnAjusteEntrada.setEnabled(true);
                btnAjusteSaida.setEnabled(true);
                carregarMovimentos();
                atualizarSaldosFinais();
            } else {
                // Preenche data/hora atual e saldos do último caixa fechado
                tfDataAbertura.setText(LocalDateTime.now().format(formatter));
                carregarSaldosUltimoCaixa();
                btnAbrirCaixa.setEnabled(true);
                btnFecharCaixa.setEnabled(false);
                btnAjusteEntrada.setEnabled(false);
                btnAjusteSaida.setEnabled(false);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega saldos finais do último caixa fechado
    private void carregarSaldosUltimoCaixa() {
        try {
            CaixaController controller = new CaixaController();
            List<Caixa> caixas = controller.listarTodos();
            Caixa ultimoCaixa = caixas.stream()
                    .filter(c -> c.getDataFechamento() != null)
                    .max((c1, c2) -> c1.getDataFechamento().compareTo(c2.getDataFechamento()))
                    .orElse(null);
            if (ultimoCaixa != null) {
                CaixaMovimentoController movimentoController = new CaixaMovimentoController();
                BigDecimal[] saldos = movimentoController.calcularSaldosFinais(ultimoCaixa.getId());
                tfSaldoInicialDinheiro.setText(String.format("%.2f", saldos[0]));
                tfSaldoInicialCartao.setText(String.format("%.2f", saldos[1]));
                tfSaldoInicialPix.setText(String.format("%.2f", saldos[2]));
            } else {
                tfSaldoInicialDinheiro.setText("0.00");
                tfSaldoInicialCartao.setText("0.00");
                tfSaldoInicialPix.setText("0.00");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar saldos do último caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega as movimentações do caixa atual
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

    // Atualiza os saldos finais
    private void atualizarSaldosFinais() {
        if (caixaAtual == null) return;
        try {
            CaixaMovimentoController controller = new CaixaMovimentoController();
            BigDecimal[] saldos = controller.calcularSaldosFinais(caixaAtual.getId());
            lblSaldoFinalDinheiro.setText(String.format("R$ %.2f", saldos[0]));
            lblSaldoFinalCartao.setText(String.format("R$ %.2f", saldos[1]));
            lblSaldoFinalPix.setText(String.format("R$ %.2f", saldos[2]));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao calcular saldos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Registra ajuste de movimento (entrada ou saída)
    private void ajusteMovimento(boolean isEntrada) throws SQLException {
        if (caixaAtual == null) {
            JOptionPane.showMessageDialog(this, "Nenhum caixa aberto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Diálogo para escolher forma de pagamento e valor
        String[] formas = {"DINHEIRO", "CARTAO", "PIX"};
        JComboBox<String> cbFormaPagamento = new JComboBox<>(formas);
        JTextField tfValor = new JTextField(10);
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

        String valorStr = tfValor.getText().trim();
        String descricao = tfDescricao.getText().trim();
        if (valorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha o valor!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal valor = new BigDecimal(valorStr.replace(",", "."));
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
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido! Use formato numérico (ex: 100.00).", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}