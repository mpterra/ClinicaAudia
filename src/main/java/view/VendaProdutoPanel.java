package view;

import com.toedter.calendar.JCalendar;
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
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.*;
import model.*;
import util.Sessao;

// Painel para registro de vendas de produtos com suporte a múltiplos itens, associadas a paciente, atendimento e/ou orçamento
public class VendaProdutoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes do formulário
    private JTextField txtBuscaPaciente;
    private JTextField txtBuscaProduto;
    private JTextField txtNomePaciente;
    private JTextField txtTelefone;
    private JTextField txtIdade;
    private JTextField txtEmail;
    private JTextField txtNomeProduto;
    private JTextField txtEstoque;
    private JTextField txtCodigoSerial;
    private JSpinner spinnerQuantidade;
    private JTextField txtPrecoUnitario;
    private JComboBox<String> cbMetodoPagamento;
    private JSpinner spinnerParcelas;
    private JTable tabelaItensVenda;
    private DefaultTableModel modeloTabelaItens;
    private JLabel lblValorTotal;
    private JFormattedTextField dateVencimentoInicial;
    private JPopupMenu calendarPopup;
    private JCalendar calendar;

    // Estilo
    private final Color primaryColor = new Color(34, 139, 34); // Verde
    private final Color secondaryColor = new Color(200, 255, 200); // Verde claro
    private final Color thirdiaryColor = new Color(45, 99, 255); // Azul claro
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightGreen = new Color(230, 255, 230); // Verde muito claro
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);

    // Controladores
    private final PacienteController pacienteController = new PacienteController();
    private final ProdutoController produtoController = new ProdutoController();
    private final VendaController vendaController = new VendaController();
    private final VendaProdutoController vendaProdutoController = new VendaProdutoController();
    private final EstoqueController estoqueController = new EstoqueController();
    private final MovimentoEstoqueController movimentoEstoqueController = new MovimentoEstoqueController();
    private final CaixaController caixaController = new CaixaController();
    private final CaixaMovimentoController caixaMovimentoController = new CaixaMovimentoController();
    private final PagamentoVendaController pagamentoVendaController = new PagamentoVendaController();
    private final OrcamentoController orcamentoController = new OrcamentoController();
    private final OrcamentoProdutoController orcamentoProdutoController = new OrcamentoProdutoController();
    private final AtendimentoController atendimentoController = new AtendimentoController();

    // Variáveis de estado
    private Paciente pacienteSelecionado;
    private Produto produtoSelecionado;
    private Atendimento atendimentoSelecionado;
    private Orcamento orcamentoSelecionado;
    private List<VendaProduto> itensVendaAtual;
    private BigDecimal valorTotalVenda;
    private Map<Integer, Paciente> cachePacientes;
    private Map<Integer, Produto> cacheProdutos;
    private Map<Integer, Estoque> cacheEstoque;

    // Formas de pagamento disponíveis
    private static final String[] FORMAS_PAGAMENTO = {"DINHEIRO", "PIX", "DEBITO", "CREDITO", "BOLETO"};

    // Construtor padrão
    public VendaProdutoPanel() {
        this(null, null);
    }

    // Construtor para inicializar com atendimento
    public VendaProdutoPanel(Integer atendimentoId) {
        this(atendimentoId, null);
    }

    // Construtor para inicializar com orçamento
    public VendaProdutoPanel(Orcamento orcamento) {
        this(null, orcamento);
    }

    // Construtor para inicializar com atendimento e/ou orçamento
    public VendaProdutoPanel(Integer atendimentoId, Orcamento orcamento) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        // Inicializa estado
        itensVendaAtual = new ArrayList<>();
        valorTotalVenda = BigDecimal.ZERO;
        cachePacientes = new HashMap<>();
        cacheProdutos = new HashMap<>();
        cacheEstoque = new HashMap<>();
        atendimentoSelecionado = null;
        orcamentoSelecionado = orcamento;

        // Inicializa componentes de pagamento
        cbMetodoPagamento = new JComboBox<>(FORMAS_PAGAMENTO);
        cbMetodoPagamento.setPreferredSize(new Dimension(106, 25)); // Largura reduzida em 12%
        cbMetodoPagamento.setFont(fieldFont);
        cbMetodoPagamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        spinnerParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerParcelas.setPreferredSize(new Dimension(70, 25)); // Largura reduzida em 12%
        spinnerParcelas.setFont(fieldFont);
        spinnerParcelas.setEnabled(false);

        // Inicializa o JFormattedTextField com máscara
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            dateVencimentoInicial = new JFormattedTextField(dateMask);
            dateVencimentoInicial.setPreferredSize(new Dimension(120, 25));
            dateVencimentoInicial.setFont(fieldFont);
            dateVencimentoInicial.setEnabled(false);
            dateVencimentoInicial.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao configurar formato de data: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

        // Inicializa o JCalendar e JPopupMenu
        calendarPopup = new JPopupMenu();
        calendar = new JCalendar();
        calendar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendar.setDecorationBackgroundColor(backgroundColor);
        calendar.setTodayButtonVisible(true);
        calendarPopup.add(calendar);
        calendarPopup.setPreferredSize(new Dimension(400, 300)); // Aumenta o tamanho do calendário
        calendar.addPropertyChangeListener("calendar", evt -> {
            java.util.Calendar selectedDate = calendar.getCalendar();
            if (selectedDate != null) {
                LocalDate date = selectedDate.getTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                dateVencimentoInicial.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                calendarPopup.setVisible(false);
            }
        });

        // Listener para abrir o calendário centralizado
        dateVencimentoInicial.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (dateVencimentoInicial.isEnabled()) {
                    // Calcula a posição central da tela
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Dimension popupSize = calendarPopup.getPreferredSize();
                    int x = (screenSize.width - popupSize.width) / 2;
                    int y = (screenSize.height - popupSize.height) / 2;
                    calendarPopup.show(dateVencimentoInicial, x - dateVencimentoInicial.getLocationOnScreen().x, y - dateVencimentoInicial.getLocationOnScreen().y);
                }
            }
        });

        // Carrega dados iniciais
        carregarCacheInicial();
        if (atendimentoId != null) {
            carregarAtendimento(atendimentoId);
        }
        if (orcamento != null) {
            carregarOrcamento(orcamento);
        }

        // Título
        JLabel lblTitulo = new JLabel("Venda de Produtos", SwingConstants.CENTER);
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
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.45));
        add(splitPane, BorderLayout.CENTER);
    }

    // Carrega dados iniciais em cache
    private void carregarCacheInicial() {
        try {
            for (Paciente p : pacienteController.listarTodos()) {
                cachePacientes.put(p.getId(), p);
            }
            for (Produto p : produtoController.listarTodos()) {
                cacheProdutos.put(p.getId(), p);
            }
            for (Estoque e : estoqueController.listarTodos()) {
                cacheEstoque.put(e.getProdutoId(), e);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega dados do atendimento
    private void carregarAtendimento(Integer atendimentoId) {
        try {
            atendimentoSelecionado = atendimentoController.buscarPorId(atendimentoId);
            if (atendimentoSelecionado != null) {
                pacienteSelecionado = atendimentoSelecionado.getPaciente();
                if (pacienteSelecionado != null) {
                    txtBuscaPaciente.setText(pacienteSelecionado.getNome());
                    atualizarPaciente();
                    txtBuscaPaciente.setEditable(false); // Bloqueia edição do paciente
                }
            } else {
                throw new IllegalArgumentException("Atendimento não encontrado!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar atendimento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carrega dados do orçamento e seus produtos
    private void carregarOrcamento(Orcamento orcamento) {
        try {
            orcamentoSelecionado = orcamento;
            if (orcamentoSelecionado.getPacienteId() != null) {
                pacienteSelecionado = pacienteController.buscarPorId(orcamentoSelecionado.getPacienteId());
                txtBuscaPaciente.setText(pacienteSelecionado.getNome());
                atualizarPaciente();
                txtBuscaPaciente.setEditable(false); // Bloqueia edição do paciente
            }
            // Carrega produtos do orçamento
            List<OrcamentoProduto> produtosOrcamento = orcamentoProdutoController.listarPorOrcamento(orcamentoSelecionado.getId());
            for (OrcamentoProduto op : produtosOrcamento) {
                VendaProduto vp = new VendaProduto();
                vp.setProdutoId(op.getProdutoId());
                vp.setQuantidade(op.getQuantidade());
                vp.setPrecoUnitario(op.getPrecoUnitario());
                vp.setDataVenda(Timestamp.valueOf(LocalDateTime.now()));
                vp.setCogidoSerial(""); // Código serial será preenchido manualmente
                itensVendaAtual.add(vp);
            }
            atualizarTabelaItens();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar orçamento: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cria o painel de formulário
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Registrar Venda",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
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
        JLabel lblBuscaPaciente = new JLabel("Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        buscaPanel.add(lblBuscaPaciente);
        txtBuscaPaciente = new JTextField(15);
        txtBuscaPaciente.setPreferredSize(new Dimension(150, 25));
        txtBuscaPaciente.setFont(fieldFont);
        txtBuscaPaciente.setToolTipText("Digite o nome do paciente");
        buscaPanel.add(txtBuscaPaciente);
        JLabel lblBuscaProduto = new JLabel("Produto:");
        lblBuscaProduto.setFont(labelFont);
        buscaPanel.add(lblBuscaProduto);
        txtBuscaProduto = new JTextField(15);
        txtBuscaProduto.setPreferredSize(new Dimension(150, 25));
        txtBuscaProduto.setFont(fieldFont);
        txtBuscaProduto.setToolTipText("Digite o nome do produto");
        buscaPanel.add(txtBuscaProduto);
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

        // Dados do Paciente
        JLabel lblPacienteTitle = new JLabel("Dados do Paciente");
        lblPacienteTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblPacienteTitle.setForeground(primaryColor);
        gbcData.gridx = 0;
        gbcData.gridy = 0;
        gbcData.gridwidth = 2;
        dataPanel.add(lblPacienteTitle, gbcData);

        JLabel lblNomePaciente = new JLabel("Nome:");
        lblNomePaciente.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 1;
        gbcData.gridwidth = 1;
        gbcData.weightx = 0.0;
        dataPanel.add(lblNomePaciente, gbcData);
        txtNomePaciente = new JTextField(15);
        txtNomePaciente.setEditable(false);
        txtNomePaciente.setBackground(Color.WHITE);
        txtNomePaciente.setPreferredSize(new Dimension(150, 25));
        txtNomePaciente.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtNomePaciente, gbcData);

        JLabel lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 2;
        gbcData.weightx = 0.0;
        dataPanel.add(lblTelefone, gbcData);
        txtTelefone = new JTextField(15);
        txtTelefone.setEditable(false);
        txtTelefone.setBackground(Color.WHITE);
        txtTelefone.setPreferredSize(new Dimension(150, 25));
        txtTelefone.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtTelefone, gbcData);

        JLabel lblIdade = new JLabel("Idade:");
        lblIdade.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 3;
        gbcData.weightx = 0.0;
        dataPanel.add(lblIdade, gbcData);
        txtIdade = new JTextField(15);
        txtIdade.setEditable(false);
        txtIdade.setBackground(Color.WHITE);
        txtIdade.setPreferredSize(new Dimension(150, 25));
        txtIdade.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtIdade, gbcData);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        gbcData.gridx = 0;
        gbcData.gridy = 4;
        gbcData.weightx = 0.0;
        dataPanel.add(lblEmail, gbcData);
        txtEmail = new JTextField(15);
        txtEmail.setEditable(false);
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setPreferredSize(new Dimension(150, 25));
        txtEmail.setFont(fieldFont);
        gbcData.gridx = 1;
        gbcData.weightx = 1.0;
        dataPanel.add(txtEmail, gbcData);

        // Dados do Produto
        JLabel lblProdutoTitle = new JLabel("Dados do Produto");
        lblProdutoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblProdutoTitle.setForeground(primaryColor);
        gbcData.gridx = 2;
        gbcData.gridy = 0;
        gbcData.gridwidth = 2;
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

        JLabel lblEstoque = new JLabel("Estoque:");
        lblEstoque.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 2;
        gbcData.weightx = 0.0;
        dataPanel.add(lblEstoque, gbcData);
        txtEstoque = new JTextField(15);
        txtEstoque.setEditable(false);
        txtEstoque.setBackground(Color.WHITE);
        txtEstoque.setPreferredSize(new Dimension(150, 25));
        txtEstoque.setFont(fieldFont);
        gbcData.gridx = 3;
        gbcData.weightx = 1.0;
        dataPanel.add(txtEstoque, gbcData);

        JLabel lblCodigoSerial = new JLabel("Código Serial:");
        lblCodigoSerial.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 3;
        gbcData.weightx = 0.0;
        dataPanel.add(lblCodigoSerial, gbcData);
        txtCodigoSerial = new JTextField(15);
        txtCodigoSerial.setPreferredSize(new Dimension(150, 25));
        txtCodigoSerial.setFont(fieldFont);
        txtCodigoSerial.setToolTipText("Digite o código serial do produto");
        gbcData.gridx = 3;
        gbcData.weightx = 1.0;
        dataPanel.add(txtCodigoSerial, gbcData);

        JLabel lblQuantidade = new JLabel("Quantidade:");
        lblQuantidade.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 4;
        gbcData.weightx = 0.0;
        dataPanel.add(lblQuantidade, gbcData);
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        spinnerQuantidade.setPreferredSize(new Dimension(80, 25));
        spinnerQuantidade.setFont(fieldFont);
        gbcData.gridx = 3;
        gbcData.weightx = 1.0;
        dataPanel.add(spinnerQuantidade, gbcData);

        JLabel lblPreco = new JLabel("Preço Unitário:");
        lblPreco.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 5;
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
        btnAdicionarItem.setBackground(thirdiaryColor);
        btnAdicionarItem.setForeground(Color.WHITE);
        btnAdicionarItem.setBorder(BorderFactory.createEmptyBorder());
        btnAdicionarItem.setPreferredSize(new Dimension(100, 30));
        btnAdicionarItem.setHorizontalAlignment(SwingConstants.CENTER);
        btnAdicionarItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdicionarItem.setToolTipText("Adicionar produto à venda");
        botoesPanel.add(btnAdicionarItem);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(botoesPanel, gbc);

        // Listeners
        btnAdicionarItem.addActionListener(e -> adicionarItemVenda());
        btnLimpar.addActionListener(e -> limparCampos());
        txtBuscaPaciente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarPaciente(); }
            public void removeUpdate(DocumentEvent e) { atualizarPaciente(); }
            public void changedUpdate(DocumentEvent e) { atualizarPaciente(); }
        });
        txtBuscaProduto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarProduto(); }
            public void removeUpdate(DocumentEvent e) { atualizarProduto(); }
            public void changedUpdate(DocumentEvent e) { atualizarProduto(); }
        });
        cbMetodoPagamento.addActionListener(e -> atualizarParcelas());

        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }

 // Cria o painel da tabela de itens da venda atual
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Itens da Venda Atual",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);
        String[] colunas = {"Código Serial", "Produto", "Quantidade", "Preço Unitário", "Subtotal"};
        modeloTabelaItens = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaItensVenda = new JTable(modeloTabelaItens) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                c.setForeground(Color.BLACK);
                if (isRowSelected(row)) {
                    c.setBackground(secondaryColor);
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1, column == getColumnCount() - 1 ? 1 : 0, Color.BLACK));
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                }
                return c;
            }
        };
        tabelaItensVenda.setShowGrid(false);
        tabelaItensVenda.setIntercellSpacing(new Dimension(0, 0));
        tabelaItensVenda.setFillsViewportHeight(true);
        tabelaItensVenda.setRowHeight(25);
        tabelaItensVenda.setFont(fieldFont);
        tabelaItensVenda.setBackground(backgroundColor);
        JTableHeader header = tabelaItensVenda.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaItensVenda.getColumnCount(); i++) {
            tabelaItensVenda.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        JScrollPane scroll = new JScrollPane(tabelaItensVenda);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);
        // Painel inferior com layout vertical
        JPanel southPanel = new JPanel(new GridBagLayout());
        southPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        // Label do valor total
        lblValorTotal = new JLabel("Valor Total: R$ 0,00");
        lblValorTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValorTotal.setForeground(primaryColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        southPanel.add(lblValorTotal, gbc);
        // Painel auxiliar para limitar a largura do pagamentoPanel
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapperPanel.setBackground(backgroundColor);
        // Seção de Pagamento
        JPanel pagamentoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        pagamentoPanel.setBackground(backgroundColor);
        pagamentoPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        JLabel lblMetodo = new JLabel("Forma pgto:");
        lblMetodo.setFont(labelFont);
        pagamentoPanel.add(lblMetodo);
        pagamentoPanel.add(cbMetodoPagamento);
        JLabel lblParcelas = new JLabel("Parcelas:");
        lblParcelas.setFont(labelFont);
        pagamentoPanel.add(lblParcelas);
        pagamentoPanel.add(spinnerParcelas);
        JLabel lblDataVencimento = new JLabel("1º Vencimento:");
        lblDataVencimento.setFont(labelFont);
        pagamentoPanel.add(lblDataVencimento);
        pagamentoPanel.add(dateVencimentoInicial);
        wrapperPanel.add(pagamentoPanel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        southPanel.add(wrapperPanel, gbc);
        // Botão Realizar Venda
        JButton btnRealizarVenda = new JButton("Realizar Venda");
        btnRealizarVenda.setBackground(primaryColor);
        btnRealizarVenda.setForeground(Color.WHITE);
        btnRealizarVenda.setBorder(BorderFactory.createEmptyBorder());
        btnRealizarVenda.setPreferredSize(new Dimension(100, 30));
        btnRealizarVenda.setHorizontalAlignment(SwingConstants.CENTER);
        btnRealizarVenda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRealizarVenda.setToolTipText("Finalizar a venda");
        btnRealizarVenda.addActionListener(e -> realizarVenda());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        southPanel.add(btnRealizarVenda, gbc);
        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    // Atualiza os dados do paciente
    private void atualizarPaciente() {
        if (!txtBuscaPaciente.isEditable()) return; // Ignora se o campo não é editável
        String busca = txtBuscaPaciente.getText().trim().toLowerCase();
        pacienteSelecionado = null;
        if (busca.isEmpty()) {
            limparCamposPaciente();
            return;
        }
        for (Paciente p : cachePacientes.values()) {
            if (p.getNome().toLowerCase().contains(busca)) {
                if (pacienteSelecionado == null || p.getId() > pacienteSelecionado.getId()) {
                    pacienteSelecionado = p;
                }
            }
        }
        if (pacienteSelecionado != null) {
            txtNomePaciente.setText(pacienteSelecionado.getNome());
            txtTelefone.setText(pacienteSelecionado.getTelefone() != null ? pacienteSelecionado.getTelefone() : "N/A");
            long idade = pacienteSelecionado.getDataNascimento() != null
                    ? java.time.temporal.ChronoUnit.YEARS.between(pacienteSelecionado.getDataNascimento(), LocalDate.now())
                    : 0;
            txtIdade.setText(String.valueOf(idade));
            txtEmail.setText(pacienteSelecionado.getEmail() != null ? pacienteSelecionado.getEmail() : "N/A");
        } else {
            limparCamposPaciente();
        }
    }

    // Limpa os campos do paciente
    private void limparCamposPaciente() {
        txtNomePaciente.setText("");
        txtTelefone.setText("");
        txtIdade.setText("");
        txtEmail.setText("");
    }

    // Atualiza os dados do produto
    private void atualizarProduto() {
        String busca = txtBuscaProduto.getText().trim().toLowerCase();
        produtoSelecionado = null;
        if (busca.isEmpty()) {
            limparCamposProduto();
            return;
        }
        for (Produto p : cacheProdutos.values()) {
            if (p.getNome().toLowerCase().contains(busca)) {
                if (produtoSelecionado == null || p.getId() > produtoSelecionado.getId()) {
                    produtoSelecionado = p;
                }
            }
        }
        if (produtoSelecionado != null) {
            txtNomeProduto.setText(produtoSelecionado.getNome());
            Estoque estoque = cacheEstoque.get(produtoSelecionado.getId());
            txtEstoque.setText(estoque != null ? String.valueOf(estoque.getQuantidade()) : "0");
            txtPrecoUnitario.setText(String.format("%.2f", produtoSelecionado.getPrecoVenda()).replace(".", ","));
            txtCodigoSerial.setText("");
        } else {
            limparCamposProduto();
        }
    }

    // Limpa os campos do produto
    private void limparCamposProduto() {
        txtNomeProduto.setText("");
        txtEstoque.setText("");
        txtPrecoUnitario.setText("0,00");
        txtCodigoSerial.setText("");
    }

    // Atualiza opções de parcelas
    private void atualizarParcelas() {
        String metodo = (String) cbMetodoPagamento.getSelectedItem();
        if ("BOLETO".equals(metodo)) {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 12, 1));
            spinnerParcelas.setEnabled(true);
            dateVencimentoInicial.setEnabled(true);
        } else if ("CREDITO".equals(metodo)) {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 12, 1));
            spinnerParcelas.setEnabled(true);
            dateVencimentoInicial.setEnabled(false);
        } else {
            spinnerParcelas.setModel(new SpinnerNumberModel(1, 1, 1, 1));
            spinnerParcelas.setEnabled(false);
            dateVencimentoInicial.setEnabled(false);
        }
    }

    // Adiciona um item à venda atual
    private void adicionarItemVenda() {
        try {
            if (produtoSelecionado == null) {
                throw new IllegalArgumentException("Selecione um produto!");
            }
            String codigoSerial = txtCodigoSerial.getText().trim();
            if (codigoSerial.isEmpty()) {
                int resposta = JOptionPane.showConfirmDialog(
                        this,
                        "O produto não possui código serial. Tem certeza que deseja continuar?",
                        "Confirmação",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (resposta != JOptionPane.YES_OPTION) {
                    return; // Interrompe se o usuário clicar em "Não"
                }
            } else if (vendaProdutoController.serialExiste(codigoSerial)) {
                throw new IllegalArgumentException("Código serial já utilizado em outra venda!");
            }
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
            Estoque estoque = cacheEstoque.get(produtoSelecionado.getId());
            if (estoque == null || estoque.getQuantidade() < quantidade) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto!");
            }
            VendaProduto vendaProduto = new VendaProduto();
            vendaProduto.setProdutoId(produtoSelecionado.getId());
            vendaProduto.setQuantidade(quantidade);
            vendaProduto.setPrecoUnitario(precoUnitario);
            vendaProduto.setCogidoSerial(codigoSerial);
            LocalDate dataVenda = LocalDate.now();
            vendaProduto.setDataVenda(Timestamp.valueOf(dataVenda.atStartOfDay()));
            vendaProduto.setGarantiaMeses(produtoSelecionado.getGarantiaMeses());
            if (produtoSelecionado.getGarantiaMeses() > 0) {
                vendaProduto.setFimGarantia(Date.valueOf(dataVenda.plusMonths(produtoSelecionado.getGarantiaMeses())));
            }
            itensVendaAtual.add(vendaProduto);
            atualizarTabelaItens();
            txtBuscaProduto.setText("");
            txtCodigoSerial.setText("");
            spinnerQuantidade.setValue(1);
            limparCamposProduto();
            produtoSelecionado = null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar item: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza a tabela de itens da venda atual
    private void atualizarTabelaItens() {
        modeloTabelaItens.setRowCount(0);
        valorTotalVenda = BigDecimal.ZERO;
        for (VendaProduto vp : itensVendaAtual) {
            Produto p = cacheProdutos.get(vp.getProdutoId());
            if (p == null) {
                try {
                    p = produtoController.buscarPorId(vp.getProdutoId());
                    cacheProdutos.put(p.getId(), p);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Erro ao carregar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            }
            BigDecimal subtotal = vp.getPrecoUnitario().multiply(BigDecimal.valueOf(vp.getQuantidade()));
            valorTotalVenda = valorTotalVenda.add(subtotal);
            modeloTabelaItens.addRow(new Object[]{
                    vp.getCogidoSerial(),
                    p.getNome(),
                    vp.getQuantidade(),
                    String.format("R$ %.2f", vp.getPrecoUnitario()),
                    String.format("R$ %.2f", subtotal)
            });
        }
        lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalVenda));
    }

    // Realiza a venda
    private void realizarVenda() {
        try {
            if (itensVendaAtual.isEmpty()) {
                throw new IllegalArgumentException("Adicione pelo menos um produto à venda!");
            }
            int parcelas = (Integer) spinnerParcelas.getValue();
            String metodo = (String) cbMetodoPagamento.getSelectedItem();
            LocalDate dataVencimento = LocalDate.now(); // Padrão: data atual
            // Validar e obter a data de vencimento inicial apenas para BOLETO
            if ("BOLETO".equals(metodo)) {
                String dataText = dateVencimentoInicial.getText();
                try {
                    dataVencimento = LocalDate.parse(dataText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (dataVencimento.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("A data de vencimento inicial não pode ser anterior à data atual!");
                    }
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Data de vencimento inválida!");
                }
            }
            // Verificar caixa aberto apenas para métodos que não sejam BOLETO
            Caixa caixa = null;
            if (!metodo.equals("BOLETO")) {
                caixa = caixaController.getCaixaAberto();
                if (caixa == null) {
                    throw new IllegalStateException("Nenhum caixa aberto encontrado!");
                }
            }
            Venda venda = new Venda();
            venda.setPacienteId(pacienteSelecionado != null ? pacienteSelecionado.getId() : null);
            venda.setAtendimentoId(atendimentoSelecionado != null ? atendimentoSelecionado.getId() : null);
            venda.setOrcamentoId(orcamentoSelecionado != null ? orcamentoSelecionado.getId() : null);
            venda.setValorTotal(valorTotalVenda);
            venda.setUsuario(Sessao.getUsuarioLogado().getLogin());
            venda.setDataHora(Timestamp.valueOf(LocalDateTime.now()));
            if (!vendaController.registrarVenda(venda, Sessao.getUsuarioLogado().getLogin())) {
                throw new SQLException("Falha ao registrar venda!");
            }
            int vendaId = venda.getId();
            for (VendaProduto vp : itensVendaAtual) {
                vp.setVendaId(vendaId);
                if (!vendaProdutoController.adicionarProdutoVenda(vp)) {
                    throw new SQLException("Falha ao registrar produto da venda!");
                }
                Estoque estoque = cacheEstoque.get(vp.getProdutoId());
                estoque.setQuantidade(estoque.getQuantidade() - vp.getQuantidade());
                estoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!estoqueController.salvarOuAtualizarEstoque(estoque, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao atualizar estoque!");
                }
                cacheEstoque.put(estoque.getProdutoId(), estoque);
                MovimentoEstoque movimentoEstoque = new MovimentoEstoque();
                movimentoEstoque.setProdutoId(vp.getProdutoId());
                movimentoEstoque.setQuantidade(vp.getQuantidade());
                movimentoEstoque.setTipo(MovimentoEstoque.Tipo.SAIDA);
                movimentoEstoque.setObservacoes("Saída por venda ID " + vendaId);
                movimentoEstoque.setUsuario(Sessao.getUsuarioLogado().getLogin());
                if (!movimentoEstoqueController.registrarMovimento(movimentoEstoque, Sessao.getUsuarioLogado().getLogin())) {
                    throw new SQLException("Falha ao registrar movimento de estoque!");
                }
            }
            // Para CREDITO, registrar uma única parcela no caixa com o valor total
            if ("CREDITO".equals(metodo)) {
                PagamentoVenda pagamento = new PagamentoVenda();
                pagamento.setVenda(venda);
                pagamento.setValor(valorTotalVenda);
                pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.CREDITO);
                pagamento.setParcela(1);
                pagamento.setTotalParcelas(parcelas); // Mantém o número de parcelas para registro
                pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                pagamento.setDataHora(LocalDateTime.now());
                pagamento.setDataVencimento(LocalDate.now()); // Data atual para CREDITO
                pagamentoVendaController.inserir(pagamento);
                CaixaMovimento movimentoCaixa = new CaixaMovimento();
                movimentoCaixa.setCaixa(caixa);
                movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
                movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
                movimentoCaixa.setPagamentoVenda(pagamento);
                movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.CREDITO);
                movimentoCaixa.setValor(valorTotalVenda);
                movimentoCaixa.setDescricao("Pagamento à vista (crédito) de venda ID " + vendaId);
                movimentoCaixa.setUsuario(Sessao.getUsuarioLogado().getLogin());
                movimentoCaixa.setDataHora(LocalDateTime.now());
                caixaMovimentoController.adicionarMovimento(movimentoCaixa);
            }
            // Para BOLETO, registrar parcelas com datas de vencimento, sem movimento no caixa
            else if ("BOLETO".equals(metodo)) {
                BigDecimal valorParcela = valorTotalVenda.divide(BigDecimal.valueOf(parcelas), 2, BigDecimal.ROUND_HALF_UP);
                for (int i = 1; i <= parcelas; i++) {
                    PagamentoVenda pagamento = new PagamentoVenda();
                    pagamento.setVenda(venda);
                    pagamento.setValor(valorParcela);
                    pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.BOLETO);
                    pagamento.setParcela(i);
                    pagamento.setTotalParcelas(parcelas);
                    pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                    pagamento.setDataHora(LocalDateTime.now());
                    pagamento.setDataVencimento(dataVencimento.plusDays((i - 1) * 30)); // Incrementa 30 dias por parcela
                    pagamentoVendaController.inserir(pagamento);
                }
            }
            // Para outros métodos (DINHEIRO, PIX, DEBITO), registrar uma única parcela no caixa
            else {
                PagamentoVenda pagamento = new PagamentoVenda();
                pagamento.setVenda(venda);
                pagamento.setValor(valorTotalVenda);
                pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(metodo));
                pagamento.setParcela(1);
                pagamento.setTotalParcelas(1);
                pagamento.setUsuario(Sessao.getUsuarioLogado().getLogin());
                pagamento.setDataHora(LocalDateTime.now());
                pagamento.setDataVencimento(LocalDate.now()); // Data atual para métodos à vista
                pagamentoVendaController.inserir(pagamento);
                CaixaMovimento movimentoCaixa = new CaixaMovimento();
                movimentoCaixa.setCaixa(caixa);
                movimentoCaixa.setTipo(CaixaMovimento.TipoMovimento.ENTRADA);
                movimentoCaixa.setOrigem(CaixaMovimento.OrigemMovimento.PAGAMENTO_VENDA);
                movimentoCaixa.setPagamentoVenda(pagamento);
                movimentoCaixa.setFormaPagamento(CaixaMovimento.FormaPagamento.valueOf(metodo));
                movimentoCaixa.setValor(valorTotalVenda);
                movimentoCaixa.setDescricao("Pagamento à vista de venda ID " + vendaId);
                movimentoCaixa.setUsuario(Sessao.getUsuarioLogado().getLogin());
                movimentoCaixa.setDataHora(LocalDateTime.now());
                caixaMovimentoController.adicionarMovimento(movimentoCaixa);
            }
            JOptionPane.showMessageDialog(this, "Venda realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao realizar venda: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        txtBuscaPaciente.setText("");
        txtBuscaProduto.setText("");
        txtNomePaciente.setText("");
        txtTelefone.setText("");
        txtIdade.setText("");
        txtEmail.setText("");
        txtNomeProduto.setText("");
        txtEstoque.setText("");
        txtCodigoSerial.setText("");
        spinnerQuantidade.setValue(1);
        txtPrecoUnitario.setText("0,00");
        cbMetodoPagamento.setSelectedIndex(0);
        spinnerParcelas.setValue(1);
        spinnerParcelas.setEnabled(false);
        dateVencimentoInicial.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateVencimentoInicial.setEnabled(false);
        pacienteSelecionado = null;
        produtoSelecionado = null;
        atendimentoSelecionado = null;
        orcamentoSelecionado = null;
        txtBuscaPaciente.setEditable(true);
        itensVendaAtual.clear();
        atualizarTabelaItens();
    }

    // Filtro para formatar entrada de valores monetários
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
            return text.matches("[0-9,.]*");
        }

        private String removeNonDigits(String text) {
            return text.replaceAll("[^0-9]", "");
        }

        private String formatCurrency(String digits) {
            if (digits.isEmpty()) return "0,00";
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
            return formattedReais + "," + cents;
        }
    }
}