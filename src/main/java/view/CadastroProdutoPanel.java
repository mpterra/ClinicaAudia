package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.ProdutoController;
import controller.TipoProdutoController;
import model.Produto;
import model.TipoProduto;
import util.Sessao;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CadastroProdutoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfNome, tfCodigoSerial, tfPesquisar;
    private JTextArea taDescricao;
    private JComboBox<TipoProduto> cbTipoProduto;
    private JSpinner spGarantiaMeses;
    private JButton btnSalvar, btnLimpar;

    private JTable tabelaProdutos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    public CadastroProdutoPanel() {
        setLayout(new BorderLayout(10, 20));

        JLabel lblTitulo = new JLabel("Cadastro de Produto", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.4);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.4));

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panelWrapper.add(splitPane, BorderLayout.CENTER);

        add(panelWrapper, BorderLayout.CENTER);

        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarProduto();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar produto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarProdutos();
        carregarTiposProduto();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelCadastroWrapper = new JPanel(new BorderLayout());
        JPanel panelCadastro = new JPanel(new GridBagLayout());

        panelCadastro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar novo produto",
                TitledBorder.LEADING,
                TitledBorder.TOP));

        panelCadastroWrapper.add(panelCadastro, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha os dados do produto");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // Tipo de produto
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCadastro.add(new JLabel("Tipo de Produto:"), gbc);

        cbTipoProduto = new JComboBox<>();
        cbTipoProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TipoProduto) setText(((TipoProduto) value).getNome());
                else setText("");
                return this;
            }
        });

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbTipoProduto, gbc);

        // Nome
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Nome:"), gbc);

        tfNome = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfNome, gbc);

        // Código Serial
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCadastro.add(new JLabel("Código Serial:"), gbc);

        tfCodigoSerial = new JTextField();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfCodigoSerial, gbc);

        // Garantia em meses
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCadastro.add(new JLabel("Garantia (meses):"), gbc);

        spGarantiaMeses = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(spGarantiaMeses, gbc);

        // Descrição
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCadastro.add(new JLabel("Descrição:"), gbc);

        taDescricao = new JTextArea(4, 20);
        taDescricao.setLineWrap(true);
        taDescricao.setWrapStyleWord(true);
        JScrollPane scrollDescricao = new JScrollPane(taDescricao);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(scrollDescricao, gbc);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(panelBotoes, gbc);

        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);

        return panelCadastroWrapper;
    }

    private JPanel criarTabelaComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Tipo", "Nome", "Código Serial", "Garantia (meses)", "Descrição"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaProdutos = new JTable(modeloTabela);
        tabelaProdutos.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++)
            tabelaProdutos.getColumnModel().getColumn(i).setCellRenderer(centralizado);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaProdutos.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaProdutos);

        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPesquisar = new JLabel("Pesquisar produto:");
        lblPesquisar.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblPesquisar.setForeground(Color.DARK_GRAY);
        panelPesquisa.add(lblPesquisar);

        tfPesquisar = new JTextField(20);
        panelPesquisa.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                if (texto.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        panelTabelaWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelTabelaWrapper.add(scrollTabela, BorderLayout.CENTER);

        return panelTabelaWrapper;
    }

    private void limparCampos() {
        tfNome.setText("");
        tfCodigoSerial.setText("");
        taDescricao.setText("");
        cbTipoProduto.setSelectedIndex(-1);
        spGarantiaMeses.setValue(0);
    }

    private void salvarProduto() throws SQLException {
        TipoProduto tipoSelecionado = (TipoProduto) cbTipoProduto.getSelectedItem();
        String nome = tfNome.getText().trim();
        String codigoSerial = tfCodigoSerial.getText().trim();
        String descricao = taDescricao.getText().trim();
        int garantiaMeses = (int) spGarantiaMeses.getValue();

        if (tipoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um tipo de produto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha o nome do produto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Produto produto = new Produto();
        produto.setTipoProdutoId(tipoSelecionado.getId());
        produto.setNome(nome);
        produto.setCodigoSerial(codigoSerial);
        produto.setDescricao(descricao);
        produto.setGarantiaMeses(garantiaMeses);
        produto.setUsuario(Sessao.getUsuarioLogado().getLogin());

        ProdutoController controller = new ProdutoController();
        boolean sucesso = controller.criarProduto(produto, Sessao.getUsuarioLogado().getLogin());

        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarProdutos();
        }
    }

    private void carregarProdutos() {
        try {
            ProdutoController controller = new ProdutoController();
            List<Produto> produtos = controller.listarTodos();
            modeloTabela.setRowCount(0);

            for (Produto p : produtos) {
                String tipoNome = "?";
                try {
                    TipoProdutoController tipoController = new TipoProdutoController();
                    TipoProduto tipo = tipoController.buscarPorId(p.getTipoProdutoId());
                    if (tipo != null) tipoNome = tipo.getNome();
                } catch (SQLException ignored) {}
                modeloTabela.addRow(new Object[]{
                        tipoNome, p.getNome(), p.getCodigoSerial(),
                        p.getGarantiaMeses(), p.getDescricao()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTiposProduto() {
        try {
            TipoProdutoController controller = new TipoProdutoController();
            List<TipoProduto> tipos = controller.listarTodos();
            cbTipoProduto.removeAllItems();
            for (TipoProduto t : tipos) cbTipoProduto.addItem(t);
            cbTipoProduto.setSelectedIndex(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar tipos de produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
