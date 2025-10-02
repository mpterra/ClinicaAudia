package view;

import controller.EstoqueController;
import controller.FornecedorController;
import controller.ProdutoController;
import controller.TipoProdutoController;
import model.Estoque;
import model.Fornecedor;
import model.Produto;
import model.TipoProduto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstoquePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de busca
    private JTextField txtBuscaProduto;
    private JComboBox<TipoProduto> cmbTipoProduto;
    private JComboBox<Fornecedor> cmbFornecedor;
    private JButton btnBuscar;

    // Tabela
    private JTable tabelaEstoque;
    private DefaultTableModel modeloTabela;

    // Estilo (mesmo do CompraProdutoPanel)
    private final Color primaryColor = new Color(154, 5, 38); // Vermelho escuro
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightGreen = new Color(230, 255, 230); // Verde muito claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final EstoqueController estoqueController = new EstoqueController();
    private final ProdutoController produtoController = new ProdutoController();
    private final TipoProdutoController tipoProdutoController = new TipoProdutoController();
    private final FornecedorController fornecedorController = new FornecedorController();

    // Cache para dados
    private Map<Integer, Produto> cacheProdutos = new HashMap<>();
    private Map<Integer, Estoque> cacheEstoque = new HashMap<>();
    private Map<Integer, TipoProduto> cacheTipos = new HashMap<>();
    private Map<Integer, Fornecedor> cacheFornecedores = new HashMap<>();

    public EstoquePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa caches
        carregarCachesIniciais();

        // Título
        JLabel lblTitulo = new JLabel("Gerenciamento de Estoque", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painel de busca no topo
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        painelBusca.setBackground(backgroundColor);
        painelBusca.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JLabel lblProduto = new JLabel("Produto:");
        lblProduto.setFont(labelFont);
        painelBusca.add(lblProduto);
        txtBuscaProduto = new JTextField(20);
        txtBuscaProduto.setPreferredSize(new Dimension(200, 25));
        txtBuscaProduto.setFont(fieldFont);
        txtBuscaProduto.setToolTipText("Digite o nome ou código do produto");
        painelBusca.add(txtBuscaProduto);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(labelFont);
        painelBusca.add(lblTipo);
        cmbTipoProduto = new JComboBox<>();
        cmbTipoProduto.setPreferredSize(new Dimension(150, 25));
        cmbTipoProduto.setFont(fieldFont);
        cmbTipoProduto.addItem(null); // Opção "Todos"
        cmbTipoProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Todos");
                } else if (value instanceof TipoProduto) {
                    setText(((TipoProduto) value).getNome());
                }
                return this;
            }
        });
        for (TipoProduto tp : cacheTipos.values()) {
            cmbTipoProduto.addItem(tp);
        }
        painelBusca.add(cmbTipoProduto);

        JLabel lblFornecedor = new JLabel("Fornecedor:");
        lblFornecedor.setFont(labelFont);
        painelBusca.add(lblFornecedor);
        cmbFornecedor = new JComboBox<>();
        cmbFornecedor.setPreferredSize(new Dimension(150, 25));
        cmbFornecedor.setFont(fieldFont);
        cmbFornecedor.addItem(null); // Opção "Todos"
        cmbFornecedor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Todos");
                } else if (value instanceof Fornecedor) {
                    setText(((Fornecedor) value).getNome());
                }
                return this;
            }
        });
        for (Fornecedor f : cacheFornecedores.values()) {
            cmbFornecedor.addItem(f);
        }
        painelBusca.add(cmbFornecedor);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(primaryColor);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setBorder(BorderFactory.createEmptyBorder());
        btnBuscar.setPreferredSize(new Dimension(100, 30));
        btnBuscar.setHorizontalAlignment(SwingConstants.CENTER);
        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> carregarDados());
        painelBusca.add(btnBuscar);

        add(painelBusca, BorderLayout.NORTH);

        // Painel da tabela
        JPanel painelTabela = criarPainelTabela();
        add(painelTabela, BorderLayout.CENTER);

        // Carregar dados iniciais
        carregarDados();
    }

    // Carrega caches iniciais
    private void carregarCachesIniciais() {
        try {
            for (Produto p : produtoController.listarTodos()) {
                cacheProdutos.put(p.getId(), p);
            }
            for (Estoque e : estoqueController.listarTodos()) {
                cacheEstoque.put(e.getProdutoId(), e);
            }
            for (TipoProduto tp : tipoProdutoController.listarTodos()) {
                cacheTipos.put(tp.getId(), tp);
            }
            for (Fornecedor f : fornecedorController.listarFornecedores()) {
                cacheFornecedores.put(f.getId(), f);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cria o painel da tabela
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                        "Itens em Estoque", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        String[] colunas = {"ID", "Nome", "Código", "Tipo", "Quantidade", "Estoque Mínimo", "Preço Custo", "Preço Venda", "Fornecedor"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaEstoque = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                c.setForeground(Color.BLACK);
                if (isRowSelected(row)) {
                    c.setBackground(primaryColor.darker());
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1,
                            column == getColumnCount() - 1 ? 1 : 0, Color.BLACK));
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }
                return c;
            }
        };

        tabelaEstoque.setShowGrid(false);
        tabelaEstoque.setIntercellSpacing(new Dimension(0, 0));
        tabelaEstoque.setFillsViewportHeight(true);
        tabelaEstoque.setRowHeight(25);
        tabelaEstoque.setFont(fieldFont);
        tabelaEstoque.setBackground(backgroundColor);

        JTableHeader header = tabelaEstoque.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaEstoque.getColumnCount(); i++) {
            tabelaEstoque.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(tabelaEstoque);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // Carrega os dados na tabela com base nos filtros
    private void carregarDados() {
        modeloTabela.setRowCount(0);
        String buscaProduto = txtBuscaProduto.getText().trim().toLowerCase();
        TipoProduto tipoSelecionado = (TipoProduto) cmbTipoProduto.getSelectedItem();
        Fornecedor fornecedorSelecionado = (Fornecedor) cmbFornecedor.getSelectedItem();

        try {
            List<Estoque> estoques = estoqueController.listarTodos();
            for (Estoque e : estoques) {
                Produto p = cacheProdutos.get(e.getProdutoId());
                if (p == null) {
                    p = produtoController.buscarPorId(e.getProdutoId());
                    if (p != null) cacheProdutos.put(p.getId(), p);
                }
                if (p == null) continue;

                TipoProduto tp = cacheTipos.get(p.getTipoProdutoId());
                if (tp == null) {
                    tp = tipoProdutoController.buscarPorId(p.getTipoProdutoId());
                    if (tp != null) cacheTipos.put(tp.getId(), tp);
                }

                Fornecedor f = null;
                // Assumindo que fornecedor é obtido do último compra_produto ou similar; aqui simplificado como null se não associado
                // Para real, implementar lógica para buscar fornecedor associado, se necessário

                boolean matchProduto = buscaProduto.isEmpty() ||
                        p.getNome().toLowerCase().contains(buscaProduto) ||
                        (p.getCodigoSerial() != null && p.getCodigoSerial().toLowerCase().contains(buscaProduto));
                boolean matchTipo = tipoSelecionado == null || (tp != null && tp.getId() == tipoSelecionado.getId());
                boolean matchFornecedor = fornecedorSelecionado == null || (f != null && f.getId() == fornecedorSelecionado.getId());

                if (matchProduto && matchTipo && matchFornecedor) {
                    modeloTabela.addRow(new Object[]{
                            p.getId(),
                            p.getNome(),
                            p.getCodigoSerial() != null ? p.getCodigoSerial() : "N/A",
                            tp != null ? tp.getNome() : "N/A",
                            e.getQuantidade(),
                            e.getEstoqueMinimo(),
                            String.format("R$ %.2f", p.getPrecoCusto()),
                            String.format("R$ %.2f", p.getPrecoVenda()),
                            f != null ? f.getNome() : "Não informado"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do estoque: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}