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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import controller.ProfissionalController;
import controller.ValorAtendimentoController;
import model.Profissional;
import model.ValorAtendimento;
import util.Sessao;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// Painel para cadastro e listagem de valores de atendimento
public class CadastroValorAtendimentoPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CadastroValorAtendimentoPanel.class.getName());
    // Componentes de entrada
    private JTextField tfPesquisar, tfValor;
    private JComboBox<Profissional> cbProfissional;
    private JComboBox<ValorAtendimento.Tipo> cbTipoAtendimento;
    private JButton btnSalvar, btnLimpar;
    // Componentes da tabela
    private JTable tabelaValores;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    // Construtor
    public CadastroValorAtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);
        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Valor de Atendimento", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);
        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarPainelTabela();
        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.4); // 40% para cadastro, 60% para tabela
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.4)); // Ajusta divisor para 40-60
        add(splitPane, BorderLayout.CENTER);
        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> salvarValorAtendimento());
        // Carregar dados iniciais com tratamento de erro
        try {
            carregarProfissionais();
            carregarValoresAtendimento();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar dados: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Não foi possível carregar os dados iniciais. Verifique a conexão com o banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Novo Valor de Atendimento",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        // Profissional
        JLabel lblProfissional = new JLabel("Profissional:");
        lblProfissional.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainGrid.add(lblProfissional, gbc);
        cbProfissional = new JComboBox<>();
        cbProfissional.setPreferredSize(new Dimension(200, 30));
        cbProfissional.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbProfissional.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof Profissional ? ((Profissional) value).getNome() : "");
                return this;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(cbProfissional, gbc);
        // Tipo de Atendimento
        JLabel lblTipoAtendimento = new JLabel("Tipo de Atendimento:");
        lblTipoAtendimento.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainGrid.add(lblTipoAtendimento, gbc);
        cbTipoAtendimento = new JComboBox<>(ValorAtendimento.Tipo.values());
        cbTipoAtendimento.setPreferredSize(new Dimension(200, 30));
        cbTipoAtendimento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(cbTipoAtendimento, gbc);
        // Valor
        JLabel lblValor = new JLabel("Valor (R$):");
        lblValor.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainGrid.add(lblValor, gbc);
        tfValor = new JTextField(20);
        tfValor.setText("R$ 0,00");
        tfValor.setPreferredSize(new Dimension(200, 30));
        ((AbstractDocument) tfValor.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(tfValor, gbc);
        // Botões (Limpar à esquerda, Salvar à direita)
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBackground(backgroundColor);
        btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(100, 35));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainGrid.add(panelBotoes, gbc);
        panel.add(mainGrid, BorderLayout.NORTH);
        return panel;
    }
    // Cria o painel da tabela com pesquisa
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Valores de Atendimento Cadastrados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);
        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Profissional:");
        lblPesquisar.setFont(labelFont);
        tfPesquisar = new JTextField(15);
        tfPesquisar.setPreferredSize(new Dimension(200, 30));
        panelBusca.add(lblPesquisar);
        panelBusca.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarTabela(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrarTabela(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrarTabela(); }
            private void filtrarTabela() {
                String texto = tfPesquisar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 0));
            }
        });
        // Tabela
        String[] colunas = {"Profissional", "Tipo", "Valor (R$)"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaValores = new JTable(modeloTabela);
        tabelaValores.setRowHeight(25);
        tabelaValores.setShowGrid(false);
        tabelaValores.setIntercellSpacing(new Dimension(0, 0));
        tabelaValores.setFont(labelFont);
        // Renderizador para alternar cores das linhas
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? rowColorLightLilac : Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < tabelaValores.getColumnCount(); i++) {
            tabelaValores.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }
        JTableHeader header = tabelaValores.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        sorter = new TableRowSorter<>(modeloTabela);
        tabelaValores.setRowSorter(sorter);
        JScrollPane scrollTabela = new JScrollPane(tabelaValores);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);
        return panel;
    }
    // Limpa os campos do formulário
    private void limparCampos() {
        tfValor.setText("R$ 0,00");
        cbProfissional.setSelectedIndex(-1);
        cbTipoAtendimento.setSelectedIndex(-1);
    }
    // Salva o valor de atendimento no banco
    private void salvarValorAtendimento() {
        try {
            Profissional profissionalSelecionado = (Profissional) cbProfissional.getSelectedItem();
            if (profissionalSelecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione um profissional.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ValorAtendimento.Tipo tipoSelecionado = (ValorAtendimento.Tipo) cbTipoAtendimento.getSelectedItem();
            if (tipoSelecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione um tipo de atendimento.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Valida valor
            String valorStr = tfValor.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
            BigDecimal valor;
            try {
                valor = new BigDecimal(valorStr);
                if (valor.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "Valor não pode ser negativo.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Arredonda para 2 casas decimais para compatibilidade com DECIMAL(10,2)
                valor = valor.setScale(2, BigDecimal.ROUND_HALF_UP);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Cria o valor de atendimento
            ValorAtendimento valorAtendimento = new ValorAtendimento();
            valorAtendimento.setProfissionalId(profissionalSelecionado.getId());
            valorAtendimento.setTipo(tipoSelecionado);
            valorAtendimento.setValor(valor);
            valorAtendimento.setUsuario(Sessao.getUsuarioLogado().getLogin());
            ValorAtendimentoController controller = new ValorAtendimentoController();
            boolean sucesso = controller.salvar(valorAtendimento, Sessao.getUsuarioLogado().getLogin());
            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Valor de atendimento salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                tfValor.setText("R$ 0,00"); // Reseta apenas o campo de valor
                carregarValoresAtendimento();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao salvar o valor de atendimento.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar valor de atendimento: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Já existe um valor de atendimento cadastrado para este profissional e tipo de atendimento.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar valor de atendimento: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao salvar valor de atendimento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Carrega os valores de atendimento na tabela
    private void carregarValoresAtendimento() {
        try {
            ValorAtendimentoController controller = new ValorAtendimentoController();
            List<ValorAtendimento> valores = controller.listarTodos();
            modeloTabela.setRowCount(0);
            ProfissionalController profissionalController = new ProfissionalController();
            for (ValorAtendimento v : valores) {
                String profissionalNome = "Desconhecido";
                try {
                    Profissional profissional = profissionalController.buscarPorId(v.getProfissionalId());
                    if (profissional != null) profissionalNome = profissional.getNome();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erro ao buscar nome do profissional ID " + v.getProfissionalId() + ": " + e.getMessage(), e);
                }
                modeloTabela.addRow(new Object[]{
                        profissionalNome,
                        v.getTipo(),
                        String.format("R$ %.2f", v.getValor())
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar valores de atendimento: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao carregar valores de atendimento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Carrega os profissionais no JComboBox, filtrando por FONOAUDIOLOGA e ativos
    private void carregarProfissionais() {
        try {
            ProfissionalController controller = new ProfissionalController();
            List<Profissional> profissionais = controller.buscarPorTipo("FONOAUDIOLOGA").stream()
                    .filter(Profissional::isAtivo)
                    .sorted((p1, p2) -> Integer.compare(p2.getId(), p1.getId())) // Mantém ordenação por ID decrescente
                    .collect(Collectors.toList());
            cbProfissional.removeAllItems();
            for (Profissional profissional : profissionais) {
                cbProfissional.addItem(profissional);
            }
            // Seleciona o primeiro profissional, se disponível
            if (cbProfissional.getItemCount() > 0) {
                cbProfissional.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar profissionais: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Filtro para formatar entrada de valores monetários com R$
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
}