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
import util.FiltroProduto;
import util.Sessao;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CadastroProdutoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfNome, tfCodigoSerial, tfPreco, tfEstoque, tfPesquisar;
    private JTextArea taDescricao;
    private JComboBox<TipoProduto> cbTipoProduto;
    private JButton btnSalvar, btnLimpar;

    private JTable tabelaProdutos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    public CadastroProdutoPanel() {
        setLayout(new BorderLayout(10, 20));

        // TÍTULO elegante no topo
        JLabel lblTitulo = new JLabel("Cadastro de Produto", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Criar painéis
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panelWrapper.add(splitPane, BorderLayout.CENTER);

        add(panelWrapper, BorderLayout.CENTER);

        // Listeners dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarProduto();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar produto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarTipos();
        carregarProdutos();
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

        // SUBTÍTULO elegante dentro do painel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha os dados do produto");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelCadastro.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // TIPO DE PRODUTO
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCadastro.add(new JLabel("Tipo de Produto:"), gbc);

        cbTipoProduto = new JComboBox<>();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbTipoProduto, gbc);

        // NOME
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelCadastro.add(new JLabel("Nome:"), gbc);

        tfNome = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfNome, gbc);

        // CÓDIGO SERIAL
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Código Serial:"), gbc);

        tfCodigoSerial = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfCodigoSerial, gbc);

        // DESCRIÇÃO
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Descrição:"), gbc);

        taDescricao = new JTextArea(4, 20);
        taDescricao.setLineWrap(true);
        taDescricao.setWrapStyleWord(true);
        JScrollPane scrollDescricao = new JScrollPane(taDescricao);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(scrollDescricao, gbc);

        // PREÇO
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Preço:"), gbc);

        tfPreco = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfPreco, gbc);

        // ESTOQUE
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Estoque:"), gbc);

        tfEstoque = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfEstoque, gbc);

        // BOTOES
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);

        // Cursor de mãozinha
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);

        return panelCadastroWrapper;
    }

    private JPanel criarTabelaComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Tipo", "Nome", "Código Serial", "Descrição", "Preço", "Estoque", "Criado", "Atualizado", "Usuário"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaProdutos = new JTable(modeloTabela);
        tabelaProdutos.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++) {
            tabelaProdutos.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

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
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1)); // pesquisa pelo nome
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        panelTabelaWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelTabelaWrapper.add(scrollTabela, BorderLayout.CENTER);

        panelTabelaWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int totalWidth = panelTabelaWrapper.getWidth();
                tabelaProdutos.getColumnModel().getColumn(0).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaProdutos.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaProdutos.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaProdutos.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth * 0.25));
                tabelaProdutos.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth * 0.1));
                tabelaProdutos.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth * 0.1));
                tabelaProdutos.getColumnModel().getColumn(6).setPreferredWidth((int)(totalWidth * 0.1));
                tabelaProdutos.getColumnModel().getColumn(7).setPreferredWidth((int)(totalWidth * 0.1));
                tabelaProdutos.getColumnModel().getColumn(8).setPreferredWidth((int)(totalWidth * 0.1));
            }
        });

        return panelTabelaWrapper;
    }

    private void limparCampos() {
        tfNome.setText("");
        tfCodigoSerial.setText("");
        taDescricao.setText("");
        tfPreco.setText("");
        tfEstoque.setText("");
        cbTipoProduto.setSelectedIndex(-1);
    }

    private void salvarProduto() throws SQLException {
        TipoProduto tipo = (TipoProduto) cbTipoProduto.getSelectedItem();
        if (tipo == null) {
            JOptionPane.showMessageDialog(this, "Selecione o tipo de produto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nome = tfNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha o nome do produto!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal preco;
        try { preco = new BigDecimal(tfPreco.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int estoque;
        try { estoque = Integer.parseInt(tfEstoque.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Estoque inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Produto produto = new Produto();
        produto.setTipoProduto(tipo);
        produto.setNome(nome);
        produto.setCodigoSerial(tfCodigoSerial.getText().trim());
        produto.setDescricao(taDescricao.getText().trim());
        produto.setPreco(preco);
        produto.setEstoque(estoque);
        produto.setUsuario(Sessao.getUsuarioLogado().getLogin());

        ProdutoController controller = new ProdutoController();
        controller.inserir(produto);

        JOptionPane.showMessageDialog(this, "Produto salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        limparCampos();
        carregarProdutos();
    }

    private void carregarTipos() {
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

    private void carregarProdutos() {
        try {
            ProdutoController controller = new ProdutoController();
            List<Produto> produtos = controller.listarTodos();

            modeloTabela.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Produto p : produtos) {
                String criado = p.getCriadoEm() != null ? p.getCriadoEm().format(formatter) : "";
                String atualizado = p.getAtualizadoEm() != null ? p.getAtualizadoEm().format(formatter) : "";

                modeloTabela.addRow(new Object[]{
                        p.getTipoProduto() != null ? p.getTipoProduto().getNome() : "?",
                        p.getNome(),
                        p.getCodigoSerial(),
                        p.getDescricao(),
                        p.getPreco(),
                        p.getEstoque(),
                        criado,
                        atualizado,
                        p.getUsuario() != null ? p.getUsuario() : "?"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
