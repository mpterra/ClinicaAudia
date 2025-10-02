package view;

import controller.CaixaController;
import controller.CaixaMovimentoController;
import controller.CompraController;
import controller.CompraProdutoController;
import controller.EstoqueController;
import controller.MovimentoEstoqueController;
import controller.PagamentoCompraController;
import controller.ProdutoController;
import controller.FornecedorController;
import model.Caixa;
import model.CaixaMovimento;
import model.Compra;
import model.CompraProduto;
import model.Estoque;
import model.MovimentoEstoque;
import model.PagamentoCompra;
import model.Produto;
import model.Fornecedor;
import util.Sessao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompraProdutoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// Componentes do formulário
	private JTextField txtBuscaProdutoNome;
	private JTextField txtBuscaProdutoCodigo;
	private JTextField txtNomeProduto;
	private JTextField txtEstoqueAtual;
	private JTextField txtPrecoUnitario;
	private JComboBox<Fornecedor> cbFornecedor;
	private JLabel lblFornecedorDados;
	private JSpinner spinnerQuantidade;
	private JComboBox<String> cbMetodoPagamento;
	private JSpinner spinnerParcelas;
	private JTable tabelaItensCompra;
	private DefaultTableModel modeloTabelaItens;
	private JLabel lblValorTotal;

	// Estilo
	private final Color primaryColor = new Color(154, 5, 38); // Vermelho escuro
	private final Color secondaryColor = new Color(94, 5, 38); // Vermelho claro
	private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
	private final Color rowColorLightGreen = new Color(230, 255, 230); // Verde muito claro
	private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
	private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
	private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

	// Controladores
	private final ProdutoController produtoController = new ProdutoController();
	private final CompraController compraController = new CompraController();
	private final CompraProdutoController compraProdutoController = new CompraProdutoController();
	private final EstoqueController estoqueController = new EstoqueController();
	private final MovimentoEstoqueController movimentoEstoqueController = new MovimentoEstoqueController();
	private final CaixaController caixaController = new CaixaController();
	private final CaixaMovimentoController caixaMovimentoController = new CaixaMovimentoController();
	private final PagamentoCompraController pagamentoCompraController = new PagamentoCompraController();
	private final FornecedorController fornecedorController = new FornecedorController();

	// Variáveis de estado
	private Produto produtoSelecionado;
	private List<CompraProduto> itensCompraAtual;
	private BigDecimal valorTotalCompra;
	private Map<Integer, Produto> cacheProdutos;
	private Map<Integer, Estoque> cacheEstoque;
	private Map<Integer, Fornecedor> cacheFornecedores;

	// Formas de pagamento disponíveis
	private static final String[] FORMAS_PAGAMENTO = { "DINHEIRO", "PIX", "DEBITO", "CREDITO", "BOLETO" };

	// Construtor padrão
	public CompraProdutoPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(backgroundColor);

		// Inicializa estado
		itensCompraAtual = new ArrayList<>();
		valorTotalCompra = BigDecimal.ZERO;
		cacheProdutos = new HashMap<>();
		cacheEstoque = new HashMap<>();
		cacheFornecedores = new HashMap<>();

		// Inicializa componentes de pagamento
		cbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO);
		cbMetodoPagamento.setPreferredSize(new Dimension(120, 25));
		cbMetodoPagamento.setFont(fieldFont);
		cbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
		spinnerParcelas.setPreferredSize(new Dimension(80, 25));
		spinnerParcelas.setFont(fieldFont);
		spinnerParcelas.setEnabled(false);

		// Carrega dados iniciais
		carregarCacheInicial();

		// Título
		JLabel lblTitulo = new JLabel("Compra de Produtos", SwingConstants.CENTER);
		lblTitulo.setFont(titleFont);
		lblTitulo.setForeground(primaryColor);
		lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
		add(lblTitulo, BorderLayout.NORTH);

		// Painéis de formulário e tabela
		JPanel painelFormulario = criarPainelFormulario();
		JPanel painelTabela = criarPainelTabela();

		// Configura o JSplitPane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelFormulario, painelTabela);
		splitPane.setResizeWeight(0.45);
		splitPane.setDividerSize(5);
		splitPane.setContinuousLayout(true);
		splitPane.setBackground(backgroundColor);
		SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.43));
		add(splitPane, BorderLayout.CENTER);
	}

	// Carrega dados iniciais em cache
	private void carregarCacheInicial() {
		try {
			for (Produto p : produtoController.listarTodos()) {
				cacheProdutos.put(p.getId(), p);
			}
			for (Estoque e : estoqueController.listarTodos()) {
				cacheEstoque.put(e.getProdutoId(), e);
			}
			for (Fornecedor f : fornecedorController.listarTodos()) {
				cacheFornecedores.put(f.getId(), f);
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Cria o painel de formulário
	private JPanel criarPainelFormulario() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
								"Registrar Compra", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
						new EmptyBorder(5, 5, 5, 5)));
		panel.setBackground(backgroundColor);

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBackground(backgroundColor);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;

		// Seção de Busca
		JPanel buscaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		buscaPanel.setBackground(backgroundColor);
		buscaPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		JLabel lblBuscaNome = new JLabel("Produto:");
		lblBuscaNome.setFont(labelFont);
		buscaPanel.add(lblBuscaNome);
		txtBuscaProdutoNome = new JTextField(15);
		txtBuscaProdutoNome.setPreferredSize(new Dimension(150, 25));
		txtBuscaProdutoNome.setFont(fieldFont);
		txtBuscaProdutoNome.setToolTipText("Digite o nome do produto");
		buscaPanel.add(txtBuscaProdutoNome);

		JLabel lblBuscaCodigo = new JLabel("Código:");
		lblBuscaCodigo.setFont(labelFont);
		buscaPanel.add(lblBuscaCodigo);
		txtBuscaProdutoCodigo = new JTextField(15);
		txtBuscaProdutoCodigo.setPreferredSize(new Dimension(150, 25));
		txtBuscaProdutoCodigo.setFont(fieldFont);
		txtBuscaProdutoCodigo.setToolTipText("Digite o código serial do produto");
		buscaPanel.add(txtBuscaProdutoCodigo);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		mainPanel.add(buscaPanel, gbc);

		// Seção de Dados
		JPanel dataPanel = new JPanel(new GridBagLayout());
		dataPanel.setBackground(backgroundColor);
		dataPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		GridBagConstraints gbcData = new GridBagConstraints();
		gbcData.insets = new Insets(2, 2, 2, 2);
		gbcData.fill = GridBagConstraints.HORIZONTAL;
		gbcData.anchor = GridBagConstraints.WEST;

		// Dados do Fornecedor
		JLabel lblFornecedorTitle = new JLabel("Dados do Fornecedor");
		lblFornecedorTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblFornecedorTitle.setForeground(primaryColor);
		gbcData.gridx = 0;
		gbcData.gridy = 0;
		gbcData.gridwidth = 2;
		dataPanel.add(lblFornecedorTitle, gbcData);

		JLabel lblFornecedor = new JLabel("Fornecedor:");
		lblFornecedor.setFont(labelFont);
		gbcData.gridx = 0;
		gbcData.gridy = 1;
		gbcData.gridwidth = 1;
		gbcData.weightx = 0.0;
		dataPanel.add(lblFornecedor, gbcData);

		cbFornecedor = new JComboBox<>();
		cbFornecedor.setPreferredSize(new Dimension(150, 25));
		cbFornecedor.setFont(fieldFont);
		cbFornecedor.addItem(null); // Opção para nenhum fornecedor
		cbFornecedor.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value == null) {
					setText("Nenhum");
				} else if (value instanceof Fornecedor) {
					setText(((Fornecedor) value).getNome());
				}
				return this;
			}
		});
		for (Fornecedor f : cacheFornecedores.values()) {
			cbFornecedor.addItem(f);
		}
		gbcData.gridx = 1;
		gbcData.weightx = 1.0;
		dataPanel.add(cbFornecedor, gbcData);

		lblFornecedorDados = new JLabel();
		lblFornecedorDados.setFont(fieldFont);
		lblFornecedorDados.setForeground(Color.GRAY);
		lblFornecedorDados.setVerticalAlignment(SwingConstants.TOP);
		gbcData.gridx = 0;
		gbcData.gridy = 2;
		gbcData.gridwidth = 2;
		gbcData.weighty = 1.0;
		gbcData.fill = GridBagConstraints.BOTH; // Para permitir expansão vertical
		dataPanel.add(lblFornecedorDados, gbcData);

		// Dados do Produto
		JLabel lblProdutoTitle = new JLabel("Dados do Produto");
		lblProdutoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblProdutoTitle.setForeground(primaryColor);
		gbcData.gridx = 2;
		gbcData.gridy = 0;
		gbcData.gridwidth = 2;
		gbcData.weighty = 0.0;
		gbcData.fill = GridBagConstraints.HORIZONTAL;
		dataPanel.add(lblProdutoTitle, gbcData);

		JLabel lblNomeProduto = new JLabel("Produto:");
		lblNomeProduto.setFont(labelFont);
		gbcData.gridx = 2;
		gbcData.gridy = 1;
		gbcData.gridwidth = 1;
		gbcData.weightx = 0.0;
		dataPanel.add(lblNomeProduto, gbcData);

		txtNomeProduto = new JTextField(15);
		txtNomeProduto.setEditable(false);
		txtNomeProduto.setBackground(Color.WHITE);
		txtNomeProduto.setPreferredSize(new Dimension(150, 25));
		txtNomeProduto.setFont(fieldFont);
		gbcData.gridx = 3;
		gbcData.weightx = 1.0;
		dataPanel.add(txtNomeProduto, gbcData);

		JLabel lblEstoque = new JLabel("Estoque Atual:");
		lblEstoque.setFont(labelFont);
		gbcData.gridx = 2;
		gbcData.gridy = 2;
		gbcData.weightx = 0.0;
		dataPanel.add(lblEstoque, gbcData);

		txtEstoqueAtual = new JTextField(15);
		txtEstoqueAtual.setEditable(false);
		txtEstoqueAtual.setBackground(Color.WHITE);
		txtEstoqueAtual.setPreferredSize(new Dimension(150, 25));
		txtEstoqueAtual.setFont(fieldFont);
		gbcData.gridx = 3;
		gbcData.weightx = 1.0;
		dataPanel.add(txtEstoqueAtual, gbcData);

		JLabel lblQuantidade = new JLabel("Quantidade:");
		lblQuantidade.setFont(labelFont);
		gbcData.gridx = 2;
		gbcData.gridy = 3;
		gbcData.weightx = 0.0;
		dataPanel.add(lblQuantidade, gbcData);

		spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
		spinnerQuantidade.setPreferredSize(new Dimension(80, 25));
		spinnerQuantidade.setFont(fieldFont);
		gbcData.gridx = 3;
		gbcData.weightx = 1.0;
		dataPanel.add(spinnerQuantidade, gbcData);

		JLabel lblPreco = new JLabel("Preço Unitário:");
		lblPreco.setFont(labelFont);
		gbcData.gridx = 2;
		gbcData.gridy = 4;
		gbcData.weightx = 0.0;
		dataPanel.add(lblPreco, gbcData);

		txtPrecoUnitario = new JTextField(15);
		txtPrecoUnitario.setText("0,00");
		txtPrecoUnitario.setPreferredSize(new Dimension(80, 25));
		txtPrecoUnitario.setFont(fieldFont);
		((AbstractDocument) txtPrecoUnitario.getDocument()).setDocumentFilter(new CurrencyDocumentFilter());
		gbcData.gridx = 3;
		gbcData.weightx = 1.0;
		dataPanel.add(txtPrecoUnitario, gbcData);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weighty = 0.4;
		mainPanel.add(dataPanel, gbc);

		// Seção de Botões
		JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
		botoesPanel.setBackground(backgroundColor);

		JButton btnLimpar = new JButton("Limpar");
		btnLimpar.setBackground(Color.LIGHT_GRAY);
		btnLimpar.setForeground(Color.BLACK);
		btnLimpar.setBorder(BorderFactory.createEmptyBorder());
		btnLimpar.setPreferredSize(new Dimension(80, 30));
		btnLimpar.setHorizontalAlignment(SwingConstants.CENTER);
		btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLimpar.setToolTipText("Limpar todos os campos");
		botoesPanel.add(btnLimpar);

		JButton btnAdicionarItem = new JButton("Adicionar Item");
		btnAdicionarItem.setBackground(primaryColor);
		btnAdicionarItem.setForeground(Color.WHITE);
		btnAdicionarItem.setBorder(BorderFactory.createEmptyBorder());
		btnAdicionarItem.setPreferredSize(new Dimension(100, 30));
		btnAdicionarItem.setHorizontalAlignment(SwingConstants.CENTER);
		btnAdicionarItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnAdicionarItem.setToolTipText("Adicionar produto à compra");
		botoesPanel.add(btnAdicionarItem);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.EAST;
		mainPanel.add(botoesPanel, gbc);

		// Listeners
		btnAdicionarItem.addActionListener(e -> adicionarItemCompra());
		btnLimpar.addActionListener(e -> limparCampos());
		txtBuscaProdutoNome.getDocument().addDocumentListener(new BuscaDocumentListener());
		txtBuscaProdutoCodigo.getDocument().addDocumentListener(new BuscaDocumentListener());
		cbMetodoPagamento.addActionListener(e -> atualizarParcelas());
		cbFornecedor.addActionListener(e -> atualizarDadosFornecedor());

		panel.add(mainPanel, BorderLayout.CENTER);
		return panel;
	}

	// Cria o painel da tabela de itens da compra atual
	private JPanel criarPainelTabela() {
	    JPanel panel = new JPanel(new BorderLayout(5, 5));
	    panel.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
	                    "Itens da Compra Atual", TitledBorder.LEFT, TitledBorder.TOP, labelFont, primaryColor),
	            new EmptyBorder(5, 5, 5, 5)));
	    panel.setBackground(backgroundColor);

	    String[] colunas = {"Produto", "Quantidade", "Preço Unitário", "Subtotal", "Fornecedor"};
	    modeloTabelaItens = new DefaultTableModel(colunas, 0) {
	        @Override
	        public boolean isCellEditable(int row, int col) {
	            return false;
	        }
	    };

	    tabelaItensCompra = new JTable(modeloTabelaItens) {
	        @Override
	        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
	            Component c = super.prepareRenderer(renderer, row, column);
	            c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
	            c.setForeground(Color.BLACK);
	            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
	            return c;
	        }

	        @Override
	        public void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            if (getSelectedRow() >= 0) {
	                Rectangle rect = getCellRect(getSelectedRow(), 0, true);
	                rect.width = getWidth();
	                g.setColor(Color.BLACK);
	                g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
	            }
	        }
	    };

		tabelaItensCompra.setShowGrid(false);
		tabelaItensCompra.setIntercellSpacing(new Dimension(0, 0));
		tabelaItensCompra.setFillsViewportHeight(true);
		tabelaItensCompra.setRowHeight(25);
		tabelaItensCompra.setFont(fieldFont);
		tabelaItensCompra.setBackground(backgroundColor);

		JTableHeader header = tabelaItensCompra.getTableHeader();
		header.setFont(new Font("SansSerif", Font.BOLD, 14));
		header.setBackground(primaryColor);
		header.setForeground(Color.WHITE);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < tabelaItensCompra.getColumnCount(); i++) {
			tabelaItensCompra.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		JScrollPane scroll = new JScrollPane(tabelaItensCompra);
		scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panel.add(scroll, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		southPanel.setBackground(backgroundColor);

		lblValorTotal = new JLabel("Valor Total: R$ 0,00");
		lblValorTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
		lblValorTotal.setForeground(primaryColor);
		lblValorTotal.setBorder(new EmptyBorder(5, 5, 5, 5));
		southPanel.add(lblValorTotal);

		// Seção de Pagamento
		JPanel pagamentoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		pagamentoPanel.setBackground(backgroundColor);
		pagamentoPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		JLabel lblMetodo = new JLabel("Método Pagamento:");
		lblMetodo.setFont(labelFont);
		pagamentoPanel.add(lblMetodo);
		pagamentoPanel.add(cbMetodoPagamento);

		JLabel lblParcelas = new JLabel("Parcelas:");
		lblParcelas.setFont(labelFont);
		pagamentoPanel.add(lblParcelas);
		pagamentoPanel.add(spinnerParcelas);

		southPanel.add(pagamentoPanel);

		// Botão Realizar Compra
		JButton btnRealizarCompra = new JButton("Realizar Compra");
		btnRealizarCompra.setBackground(primaryColor);
		btnRealizarCompra.setForeground(Color.WHITE);
		btnRealizarCompra.setBorder(BorderFactory.createEmptyBorder());
		btnRealizarCompra.setPreferredSize(new Dimension(100, 30));
		btnRealizarCompra.setHorizontalAlignment(SwingConstants.CENTER);
		btnRealizarCompra.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRealizarCompra.setToolTipText("Finalizar a compra");
		btnRealizarCompra.addActionListener(e -> realizarCompra());
		southPanel.add(btnRealizarCompra);

		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	// Atualiza os dados do produto
	private void atualizarProduto() {
		String buscaNome = txtBuscaProdutoNome.getText().trim().toLowerCase();
		String buscaCodigo = txtBuscaProdutoCodigo.getText().trim().toLowerCase();
		produtoSelecionado = null;

		if (buscaNome.isEmpty() && buscaCodigo.isEmpty()) {
			limparCamposProduto();
			return;
		}

		for (Produto p : cacheProdutos.values()) {
			boolean matchNome = buscaNome.isEmpty() || p.getNome().toLowerCase().contains(buscaNome);
			boolean matchCodigo = buscaCodigo.isEmpty()
					|| (p.getCodigoSerial() != null && p.getCodigoSerial().toLowerCase().contains(buscaCodigo));
			if (matchNome && matchCodigo) {
				if (produtoSelecionado == null || p.getId() > produtoSelecionado.getId()) {
					produtoSelecionado = p;
				}
			}
		}

		if (produtoSelecionado != null) {
			txtNomeProduto.setText(produtoSelecionado.getNome());
			Estoque estoque = cacheEstoque.get(produtoSelecionado.getId());
			txtEstoqueAtual.setText(estoque != null ? String.valueOf(estoque.getQuantidade()) : "0");
			txtPrecoUnitario.setText(String.format("%.2f", produtoSelecionado.getPrecoCusto()).replace(".", ","));
			spinnerQuantidade.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
		} else {
			limparCamposProduto();
		}
	}

	// Limpa os campos do produto
	private void limparCamposProduto() {
		txtNomeProduto.setText("");
		txtEstoqueAtual.setText("");
		txtPrecoUnitario.setText("0,00");
		spinnerQuantidade.setValue(1);
	}

	// Atualiza opções de parcelas
	private void atualizarParcelas() {
		String metodo = (String) cbMetodoPagamento.getSelectedItem();
		if ("CREDITO".equals(metodo) || "BOLETO".equals(metodo)) {
			spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 12, 1));
			spinnerParcelas.setEnabled(true);
		} else {
			spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 1, 1));
			spinnerParcelas.setEnabled(false);
		}
	}

	// Atualiza os dados do fornecedor exibidos
	private void atualizarDadosFornecedor() {
		Fornecedor fornecedor = (Fornecedor) cbFornecedor.getSelectedItem();
		if (fornecedor != null) {
			StringBuilder dados = new StringBuilder("<html>");
			dados.append("CNPJ: ").append(fornecedor.getCnpj() != null ? fornecedor.getCnpj() : "N/A").append("<br>");
			dados.append("Telefone: ").append(fornecedor.getTelefone() != null ? fornecedor.getTelefone() : "N/A")
					.append("<br>");
			dados.append("Email: ").append(fornecedor.getEmail() != null ? fornecedor.getEmail() : "N/A")
					.append("</html>");
			lblFornecedorDados.setText(dados.toString());
		} else {
			lblFornecedorDados.setText("");
		}
	}

	// Adiciona um item à compra atual
	private void adicionarItemCompra() {
		try {
			if (produtoSelecionado == null) {
				throw new IllegalArgumentException("Selecione um produto!");
			}
			Fornecedor fornecedor = (Fornecedor) cbFornecedor.getSelectedItem();
			int quantidade = (Integer) spinnerQuantidade.getValue();
			BigDecimal precoUnitario;
			try {
				String text = txtPrecoUnitario.getText().replace(".", "").replace(",", ".");
				precoUnitario = new BigDecimal(text);
				if (precoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
					throw new IllegalArgumentException("Preço unitário deve ser maior que zero!");
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Preço unitário inválido!");
			}

			CompraProduto compraProduto = new CompraProduto();
			compraProduto.setProdutoId(produtoSelecionado.getId());
			compraProduto.setQuantidade(quantidade);
			compraProduto.setPrecoUnitario(precoUnitario);
			compraProduto.setFornecedorId(fornecedor != null ? fornecedor.getId() : null);

			itensCompraAtual.add(compraProduto);
			atualizarTabelaItens();

			txtBuscaProdutoNome.setText("");
			txtBuscaProdutoCodigo.setText("");
			limparCamposProduto();
			produtoSelecionado = null;

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Erro ao adicionar item: " + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Atualiza a tabela de itens da compra atual
	private void atualizarTabelaItens() {
		modeloTabelaItens.setRowCount(0);
		valorTotalCompra = BigDecimal.ZERO;

		for (CompraProduto cp : itensCompraAtual) {
			Produto p = cacheProdutos.get(cp.getProdutoId());
			if (p == null) {
				try {
					p = produtoController.buscarPorId(cp.getProdutoId());
					cacheProdutos.put(p.getId(), p);
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(this, "Erro ao carregar produto: " + e.getMessage(), "Erro",
							JOptionPane.ERROR_MESSAGE);
					continue;
				}
			}
			Fornecedor f = null;
			if (cp.getFornecedorId() != null) {
				f = cacheFornecedores.get(cp.getFornecedorId());
				if (f == null) {
					try {
						f = fornecedorController.buscarPorId(cp.getFornecedorId());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cacheFornecedores.put(f.getId(), f);
				}
			}
			BigDecimal subtotal = cp.getPrecoUnitario().multiply(BigDecimal.valueOf(cp.getQuantidade()));
			valorTotalCompra = valorTotalCompra.add(subtotal);
			modeloTabelaItens.addRow(
					new Object[] { p.getNome(), cp.getQuantidade(), String.format("R$ %.2f", cp.getPrecoUnitario()),
							String.format("R$ %.2f", subtotal), f != null ? f.getNome() : "Não informado" });
		}

		lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalCompra));
	}

	// Realiza a compra
	private void realizarCompra() {
		try {
			if (itensCompraAtual.isEmpty()) {
				throw new IllegalArgumentException("Adicione pelo menos um produto à compra!");
			}
			int parcelas = (Integer) spinnerParcelas.getValue();
			String metodo = (String) cbMetodoPagamento.getSelectedItem();

			Caixa caixa = caixaController.getCaixaAberto();
			if (caixa == null && ("DINHEIRO".equals(metodo) || "PIX".equals(metodo) || "DEBITO".equals(metodo)
					|| parcelas == 1)) {
				throw new IllegalStateException("Nenhum caixa aberto encontrado!");
			}

			Compra compra = new Compra();
			compra.setUsuario(Sessao.getUsuarioLogado().getLogin());
			compra.setDataCompra(Timestamp.valueOf(LocalDateTime.now()));

			if (!compraController.criarCompra(compra, Sessao.getUsuarioLogado().getLogin())) {
				throw new SQLException("Falha ao registrar compra!");
			}

			int compraId = compra.getId();

			for (CompraProduto cp : itensCompraAtual) {
				cp.setCompraId(compraId);
				if (!compraProdutoController.adicionarProdutoCompra(cp)) {
					throw new SQLException("Falha ao registrar produto da compra!");
				}

				Estoque estoque = cacheEstoque.get(cp.getProdutoId());
				if (estoque == null) {
					estoque = new Estoque();
					estoque.setProdutoId(cp.getProdutoId());
					estoque.setQuantidade(0);
					estoque.setEstoqueMinimo(0);
				}
				estoque.setQuantidade(estoque.getQuantidade() + cp.getQuantidade());
				estoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
				if (!estoqueController.salvarOuAtualizarEstoque(estoque, Sessao.getUsuarioLogado().getLogin())) {
					throw new SQLException("Falha ao atualizar estoque!");
				}
				cacheEstoque.put(estoque.getProdutoId(), estoque);

				MovimentoEstoque movimentoEstoque = new MovimentoEstoque();
				movimentoEstoque.setProdutoId(cp.getProdutoId());
				movimentoEstoque.setQuantidade(cp.getQuantidade());
				movimentoEstoque.setTipo(MovimentoEstoque.Tipo.ENTRADA);
				movimentoEstoque.setObservacoes("Entrada por compra ID " + compraId);
				movimentoEstoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
				if (!movimentoEstoqueController.registrarMovimento(movimentoEstoque,
						Sessao.getUsuarioLogado().getLogin())) {
					throw new SQLException("Falha ao registrar movimento de estoque!");
				}
			}

			BigDecimal valorParcela = valorTotalCompra.divide(BigDecimal.valueOf(parcelas), 2,
					BigDecimal.ROUND_HALF_UP);
			for (int i = 1; i <= parcelas; i++) {
				PagamentoCompra pagamento = new PagamentoCompra();
				pagamento.setCompraId(compraId);
				pagamento.setValor(valorParcela);
				pagamento.setMetodoPagamento(PagamentoCompra.MetodoPagamento.valueOf(metodo));
				pagamento.setParcela(i);
				pagamento.setTotalParcelas(parcelas);
				pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
				pagamento.setDataHora(Timestamp.valueOf(LocalDateTime.now()));
				pagamento.setDataVencimento(Date.valueOf(LocalDate.now().plusMonths(i - 1)));
				pagamento.setStatus(
						i == 1 && ("DINHEIRO".equals(metodo) || "PIX".equals(metodo) || "DEBITO".equals(metodo))
								? PagamentoCompra.StatusPagamento.PAGO
								: PagamentoCompra.StatusPagamento.PENDENTE);

				if (!pagamentoCompraController.registrarPagamento(pagamento, Sessao.getUsuarioLogado().getLogin())) {
					throw new SQLException("Falha ao registrar pagamento!");
				}

				if (i == 1 && ("DINHEIRO".equals(metodo) || "PIX".equals(metodo) || "DEBITO".equals(metodo)
						|| parcelas == 1)) {
					CaixaMovimento movimentoCaixa = new CaixaMovimento();
					movimentoCaixa.setCaixa(caixa);
					movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.SAIDA);
					movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_COMPRA);
					movimentoCaixa.setPagamentoCompra(pagamento);
					movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(metodo));
					movimentoCaixa.setValor(valorParcela);
					movimentoCaixa
							.setDescricao("Pagamento " + (parcelas == 1 ? "à vista" : "parcela " + i + "/" + parcelas)
									+ " de compra ID " + compraId);
					movimentoCaixa.setUsuario(Sessao.getUsuarioLogado().getLogin());
					movimentoCaixa.setDataHora(LocalDateTime.now());
					caixaMovimentoController.adicionarMovimento(movimentoCaixa);
				}
			}

			JOptionPane.showMessageDialog(this, "Compra realizada com sucesso!", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			limparCampos();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Erro ao realizar compra: " + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Limpa os campos do formulário
	private void limparCampos() {
		txtBuscaProdutoNome.setText("");
		txtBuscaProdutoCodigo.setText("");
		txtNomeProduto.setText("");
		txtEstoqueAtual.setText("");
		txtPrecoUnitario.setText("0,00");
		cbFornecedor.setSelectedIndex(0); // Seleciona "Nenhum"
		lblFornecedorDados.setText("");
		spinnerQuantidade.setValue(1);
		cbMetodoPagamento.setSelectedIndex(0);
		spinnerParcelas.setValue(1);
		spinnerParcelas.setEnabled(false);
		produtoSelecionado = null;
		itensCompraAtual.clear();
		atualizarTabelaItens();
	}

	// Listener para busca de produtos
	private class BuscaDocumentListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			atualizarProduto();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			atualizarProduto();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			atualizarProduto();
		}
	}

	// Filtro para formatar entrada de valores monetários
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
			return text.matches("[0-9,.]*");
		}

		private String removeNonDigits(String text) {
			return text.replaceAll("[^0-9]", "");
		}

		private String formatCurrency(String digits) {
			if (digits.isEmpty())
				return "0,00";
			while (digits.length() < 3) {
				digits = "0" + digits;
			}
			String cents = digits.substring(digits.length() - 2);
			String reais = digits.substring(0, digits.length() - 2);
			reais = reais.replaceFirst("^0+(?!$)", "");
			if (reais.isEmpty())
				reais = "0";
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