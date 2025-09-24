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
import controller.ProdutoController;
import controller.TipoProdutoController;
import model.Produto;
import model.TipoProduto;
import util.Sessao;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// Painel para cadastro e listagem de produtos
public class CadastroProdutoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfNome, tfCodigoSerial, tfPesquisar;
    private JTextArea taDescricao;
    private JComboBox<TipoProduto> cbTipoProduto;
    private JComboBox<Integer> cbGarantiaMeses;
    private JButton btnSalvar, btnLimpar;

    // Componentes da tabela
    private JTable tabelaProdutos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

    // Construtor
    public CadastroProdutoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Produto", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarPainelTabela();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.495);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> salvarProduto());

        // Carregar dados iniciais
        carregarTiposProduto();
        carregarProdutos();
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Novo Produto",
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

        // Tipo de Produto
        JLabel lblTipoProduto = new JLabel("Tipo de Produto:");
        lblTipoProduto.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        mainGrid.add(lblTipoProduto, gbc);

        cbTipoProduto = new JComboBox<>();
        cbTipoProduto.setPreferredSize(new Dimension(200, 30));
        cbTipoProduto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbTipoProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof TipoProduto ? ((TipoProduto) value).getNome() : "");
                return this;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(cbTipoProduto, gbc);

        // Nome
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainGrid.add(lblNome, gbc);

        tfNome = new JTextField(20);
        tfNome.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(tfNome, gbc);

        // Código Serial
        JLabel lblCodigoSerial = new JLabel("Código Serial:");
        lblCodigoSerial.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainGrid.add(lblCodigoSerial, gbc);

        tfCodigoSerial = new JTextField(20);
        tfCodigoSerial.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(tfCodigoSerial, gbc);

        // Garantia em meses (JComboBox editável com padrões)
        JLabel lblGarantia = new JLabel("Garantia (meses):");
        lblGarantia.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        mainGrid.add(lblGarantia, gbc);

        cbGarantiaMeses = new JComboBox<>(new Integer[]{0, 12, 24, 36});
        cbGarantiaMeses.setEditable(true);
        cbGarantiaMeses.setPreferredSize(new Dimension(200, 30));
        cbGarantiaMeses.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(cbGarantiaMeses, gbc);

        // Descrição
        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        mainGrid.add(lblDescricao, gbc);

        taDescricao = new JTextArea(4, 20);
        taDescricao.setLineWrap(true);
        taDescricao.setWrapStyleWord(true);
        JScrollPane scrollDescricao = new JScrollPane(taDescricao);
        scrollDescricao.setPreferredSize(new Dimension(200, 100));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainGrid.add(scrollDescricao, gbc);

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
        gbc.gridy = 5;
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
                        "Produtos Cadastrados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Produto:");
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
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 1));
            }
        });

        // Tabela
        String[] colunas = {"Tipo", "Nome", "Código Serial", "Garantia (meses)", "Descrição"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaProdutos = new JTable(modeloTabela);
        tabelaProdutos.setRowHeight(25);
        tabelaProdutos.setShowGrid(false);
        tabelaProdutos.setIntercellSpacing(new Dimension(0, 0));
        tabelaProdutos.setFont(labelFont);

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
        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++) {
            tabelaProdutos.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaProdutos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaProdutos.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaProdutos);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);
        return panel;
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        tfNome.setText("");
        tfCodigoSerial.setText("");
        taDescricao.setText("");
        cbTipoProduto.setSelectedIndex(-1);
        cbGarantiaMeses.setSelectedIndex(0);
    }

    // Salva o produto no banco
    private void salvarProduto() {
        try {
            TipoProduto tipoSelecionado = (TipoProduto) cbTipoProduto.getSelectedItem();
            if (tipoSelecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione um tipo de produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nome = tfNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha o nome do produto.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtém valor da garantia do ComboBox editável
            Object selectedGarantia = cbGarantiaMeses.getSelectedItem();
            String garantiaStr = String.valueOf(selectedGarantia);
            int garantiaMeses = 0;
            try {
                garantiaMeses = Integer.parseInt(garantiaStr);
                if (garantiaMeses < 0) {
                    JOptionPane.showMessageDialog(this, "Garantia não pode ser negativa.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Valor de garantia inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Produto produto = new Produto();
            produto.setTipoProdutoId(tipoSelecionado.getId());
            produto.setNome(nome);
            produto.setCodigoSerial(tfCodigoSerial.getText().trim());
            produto.setDescricao(taDescricao.getText().trim());
            produto.setGarantiaMeses(garantiaMeses);
            produto.setUsuario(Sessao.getUsuarioLogado().getLogin());

            ProdutoController controller = new ProdutoController();
            boolean sucesso = controller.criarProduto(produto, Sessao.getUsuarioLogado().getLogin());

            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
                carregarProdutos();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao salvar o produto.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega os produtos na tabela
    private void carregarProdutos() {
        try {
            ProdutoController controller = new ProdutoController();
            List<Produto> produtos = controller.listarTodos();
            modeloTabela.setRowCount(0);

            TipoProdutoController tipoController = new TipoProdutoController();
            for (Produto p : produtos) {
                String tipoNome = "Desconhecido";
                try {
                    TipoProduto tipo = tipoController.buscarPorId(p.getTipoProdutoId());
                    if (tipo != null) tipoNome = tipo.getNome();
                } catch (SQLException ignored) {}

                modeloTabela.addRow(new Object[]{
                        tipoNome,
                        p.getNome(),
                        p.getCodigoSerial(),
                        p.getGarantiaMeses(),
                        p.getDescricao()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega os tipos de produto no JComboBox
    private void carregarTiposProduto() {
        try {
            TipoProdutoController controller = new TipoProdutoController();
            List<TipoProduto> tipos = controller.listarTodos();
            cbTipoProduto.removeAllItems();
            for (TipoProduto tipo : tipos) {
                cbTipoProduto.addItem(tipo);
            }
            cbTipoProduto.setSelectedIndex(-1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar tipos de produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}