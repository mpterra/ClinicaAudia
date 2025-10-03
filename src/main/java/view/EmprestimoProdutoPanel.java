package view;

import controller.EmprestimoProdutoController;
import controller.EstoqueController;
import controller.PacienteController;
import controller.ProfissionalController;
import controller.ProdutoController;
import model.EmprestimoProduto;
import model.Paciente;
import model.Profissional;
import model.Produto;
import util.Sessao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.toedter.calendar.JCalendar;

/**
 * Painel para gerenciamento de empréstimos de produtos.
 * Permite adicionar, editar, deletar e marcar devolução de empréstimos.
 * Integra com controllers para operações no banco de dados.
 */
public class EmprestimoProdutoPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    // Componentes do formulário
    private JTextField txtBuscaPaciente;
    private JTextField txtBuscaProduto;
    private JTextField txtNomePaciente;
    private JTextField txtTelefone;
    private JTextField txtIdade;
    private JTextField txtEmail;
    private JTextField txtNomeProduto;
    private JTextField txtCodigoSerial;
    private JTextField txtDescricaoProduto;
    private JTextArea txtObservacoes;
    private JFormattedTextField dateEmprestimo;
    private JFormattedTextField dateDevolucao;
    private JPopupMenu calendarPopupEmprestimo;
    private JPopupMenu calendarPopupDevolucao;
    private JCalendar calendarEmprestimo;
    private JCalendar calendarDevolucao;
    private JTable tabelaEmprestimos;
    private DefaultTableModel modeloTabelaEmprestimos;
    // Estilo
    private final Color primaryColor = new Color(34, 139, 34); // Verde
    private final Color secondaryColor = new Color(200, 255, 200); // Verde claro
    private final Color thirdiaryColor = new Color(45, 99, 255); // Azul claro
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightGreen = new Color(230, 255, 230); // Verde muito claro
    private final Color warningColor = new Color(255, 255, 204); // Amarelo para alerta (3 dias antes)
    private final Color overdueColor = new Color(255, 204, 204); // Vermelho para atraso
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font fieldFont = new Font("SansSerif", Font.PLAIN, 12);
    // Controladores
    private final EmprestimoProdutoController emprestimoController;
    private final PacienteController pacienteController;
    private final ProfissionalController profissionalController;
    private final ProdutoController produtoController;
    private final EstoqueController estoqueController;
    // Variáveis de estado
    private Paciente pacienteSelecionado;
    private Produto produtoSelecionado;
    private Map<Integer, Paciente> cachePacientes;
    private Map<Integer, Produto> cacheProdutos;
    private Map<Integer, Profissional> cacheProfissionais;
    private List<EmprestimoProduto> emprestimosAtuais;

    /**
     * Construtor padrão.
     */
    public EmprestimoProdutoPanel() {
        emprestimoController = new EmprestimoProdutoController();
        pacienteController = new PacienteController();
        profissionalController = new ProfissionalController();
        produtoController = new ProdutoController();
        estoqueController = new EstoqueController();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);
        // Inicializa estado
        cachePacientes = new HashMap<>();
        cacheProdutos = new HashMap<>();
        cacheProfissionais = new HashMap<>();
        emprestimosAtuais = new ArrayList<>();
        // Inicializa componentes de data
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            dateEmprestimo = new JFormattedTextField(dateMask);
            dateEmprestimo.setPreferredSize(new Dimension(200, 25));
            dateEmprestimo.setFont(fieldFont);
            dateEmprestimo.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateDevolucao = new JFormattedTextField(dateMask);
            dateDevolucao.setPreferredSize(new Dimension(200, 25));
            dateDevolucao.setFont(fieldFont);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao configurar formato de data: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        // Inicializa JCalendars e JPopupMenus para data de empréstimo
        calendarPopupEmprestimo = new JPopupMenu();
        calendarEmprestimo = new JCalendar();
        calendarEmprestimo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarEmprestimo.setDecorationBackgroundColor(backgroundColor);
        calendarEmprestimo.setTodayButtonVisible(true);
        calendarPopupEmprestimo.add(calendarEmprestimo);
        calendarPopupEmprestimo.setPreferredSize(new Dimension(400, 300));
        calendarEmprestimo.addPropertyChangeListener("calendar", evt -> {
            java.util.Calendar selectedDate = calendarEmprestimo.getCalendar();
            if (selectedDate != null) {
                LocalDate date = selectedDate.getTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                dateEmprestimo.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                calendarPopupEmprestimo.setVisible(false);
            }
        });
        // Listener para abrir calendário de empréstimo
        dateEmprestimo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension popupSize = calendarPopupEmprestimo.getPreferredSize();
                int x = (screenSize.width - popupSize.width) / 2;
                int y = (screenSize.height - popupSize.height) / 2;
                calendarPopupEmprestimo.show(dateEmprestimo, x - dateEmprestimo.getLocationOnScreen().x, y - dateEmprestimo.getLocationOnScreen().y);
            }
        });
        // Inicializa JCalendars e JPopupMenus para data de devolução
        calendarPopupDevolucao = new JPopupMenu();
        calendarDevolucao = new JCalendar();
        calendarDevolucao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarDevolucao.setDecorationBackgroundColor(backgroundColor);
        calendarDevolucao.setTodayButtonVisible(true);
        calendarPopupDevolucao.add(calendarDevolucao);
        calendarPopupDevolucao.setPreferredSize(new Dimension(400, 300));
        calendarDevolucao.addPropertyChangeListener("calendar", evt -> {
            java.util.Calendar selectedDate = calendarDevolucao.getCalendar();
            if (selectedDate != null) {
                LocalDate date = selectedDate.getTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                dateDevolucao.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                calendarPopupDevolucao.setVisible(false);
            }
        });
        // Listener para abrir calendário de devolução
        dateDevolucao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension popupSize = calendarPopupDevolucao.getPreferredSize();
                int x = (screenSize.width - popupSize.width) / 2;
                int y = (screenSize.height - popupSize.height) / 2;
                calendarPopupDevolucao.show(dateDevolucao, x - dateDevolucao.getLocationOnScreen().x, y - dateDevolucao.getLocationOnScreen().y);
            }
        });
        // Carrega dados iniciais
        carregarCacheInicial();
        // Título
        JLabel lblTitulo = new JLabel("Empréstimo de Produtos", SwingConstants.CENTER);
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
        // Carrega tabela inicial
        atualizarTabelaEmprestimos();
    }

    /**
     * Carrega dados iniciais em cache para pacientes, produtos e profissionais.
     */
    private void carregarCacheInicial() {
        try {
            for (Paciente p : pacienteController.listarTodos()) {
                cachePacientes.put(p.getId(), p);
            }
            for (Produto p : produtoController.listarTodos()) {
                cacheProdutos.put(p.getId(), p);
            }
            for (Profissional p : profissionalController.listarTodos()) {
                cacheProfissionais.put(p.getId(), p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cria o painel de formulário com campos para registro de empréstimos.
     */
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Registrar Empréstimo",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        // Seção de Busca
        JPanel buscaPanel = new JPanel(new GridBagLayout());
        buscaPanel.setBackground(backgroundColor);
        GridBagConstraints gbcBusca = new GridBagConstraints();
        gbcBusca.insets = new Insets(5, 5, 5, 5);
        gbcBusca.fill = GridBagConstraints.HORIZONTAL;
        gbcBusca.anchor = GridBagConstraints.WEST;
        JLabel lblBuscaPaciente = new JLabel("Paciente:");
        lblBuscaPaciente.setFont(labelFont);
        gbcBusca.gridx = 0;
        gbcBusca.gridy = 0;
        gbcBusca.weightx = 0.0;
        buscaPanel.add(lblBuscaPaciente, gbcBusca);
        txtBuscaPaciente = new JTextField(15);
        txtBuscaPaciente.setPreferredSize(new Dimension(200, 25));
        txtBuscaPaciente.setFont(fieldFont);
        txtBuscaPaciente.setToolTipText("Digite o nome do paciente");
        gbcBusca.gridx = 1;
        gbcBusca.gridy = 0;
        gbcBusca.weightx = 1.0;
        buscaPanel.add(txtBuscaPaciente, gbcBusca);
        JLabel lblBuscaProduto = new JLabel("Produto:");
        lblBuscaProduto.setFont(labelFont);
        gbcBusca.gridx = 2;
        gbcBusca.gridy = 0;
        gbcBusca.weightx = 0.0;
        buscaPanel.add(lblBuscaProduto, gbcBusca);
        txtBuscaProduto = new JTextField(15);
        txtBuscaProduto.setPreferredSize(new Dimension(200, 25));
        txtBuscaProduto.setFont(fieldFont);
        txtBuscaProduto.setToolTipText("Digite o nome do produto");
        gbcBusca.gridx = 3;
        gbcBusca.gridy = 0;
        gbcBusca.weightx = 1.0;
        buscaPanel.add(txtBuscaProduto, gbcBusca);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(buscaPanel, gbc);
        // Seção de Dados
        JPanel dataPanel = new JPanel(new GridBagLayout());
        dataPanel.setBackground(backgroundColor);
        GridBagConstraints gbcData = new GridBagConstraints();
        gbcData.insets = new Insets(5, 5, 5, 5);
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
        txtNomePaciente.setPreferredSize(new Dimension(200, 25));
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
        txtTelefone.setPreferredSize(new Dimension(200, 25));
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
        txtIdade.setPreferredSize(new Dimension(200, 25));
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
        txtEmail.setPreferredSize(new Dimension(200, 25));
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
        JLabel lblNomeProduto = new JLabel("Nome:");
        lblNomeProduto.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 1;
        gbcData.gridwidth = 1;
        gbcData.weightx = 0.0;
        dataPanel.add(lblNomeProduto, gbcData);
        txtNomeProduto = new JTextField(15);
        txtNomeProduto.setEditable(false);
        txtNomeProduto.setBackground(Color.WHITE);
        txtNomeProduto.setPreferredSize(new Dimension(200, 25));
        txtNomeProduto.setFont(fieldFont);
        gbcData.gridx = 3;
        gbcData.weightx = 1.0;
        dataPanel.add(txtNomeProduto, gbcData);
        JLabel lblCodigoSerial = new JLabel("Código Serial:");
        lblCodigoSerial.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 2;
        gbcData.weightx = 0.0;
        dataPanel.add(lblCodigoSerial, gbcData);
        txtCodigoSerial = new JTextField(15);
        txtCodigoSerial.setEditable(true);
        txtCodigoSerial.setPreferredSize(new Dimension(200, 25));
        txtCodigoSerial.setFont(fieldFont);
        gbcData.gridx = 3;
        gbcData.weightx = 1.0;
        dataPanel.add(txtCodigoSerial, gbcData);
        JLabel lblDescricaoProduto = new JLabel("Descrição:");
        lblDescricaoProduto.setFont(labelFont);
        gbcData.gridx = 2;
        gbcData.gridy = 3;
        gbcData.weightx = 0.0;
        dataPanel.add(lblDescricaoProduto, gbcData);
        txtDescricaoProduto = new JTextField(15);
        txtDescricaoProduto.setEditable(false);
        txtDescricaoProduto.setBackground(Color.WHITE);
        txtDescricaoProduto.setPreferredSize(new Dimension(200, 25));
        txtDescricaoProduto.setFont(fieldFont);
        gbcData.gridx = 3;
        gbcData.weightx = 1.0;
        dataPanel.add(txtDescricaoProduto, gbcData);
        // Dados do Empréstimo
        JPanel emprestimoPanel = new JPanel(new GridBagLayout());
        emprestimoPanel.setBackground(backgroundColor);
        GridBagConstraints gbcEmprestimo = new GridBagConstraints();
        gbcEmprestimo.insets = new Insets(5, 5, 5, 10);
        gbcEmprestimo.fill = GridBagConstraints.HORIZONTAL;
        gbcEmprestimo.anchor = GridBagConstraints.WEST;
        JLabel lblEmprestimoTitle = new JLabel("Dados do Empréstimo");
        lblEmprestimoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEmprestimoTitle.setForeground(primaryColor);
        gbcEmprestimo.gridx = 0;
        gbcEmprestimo.gridy = 0;
        gbcEmprestimo.gridwidth = 2;
        emprestimoPanel.add(lblEmprestimoTitle, gbcEmprestimo);
        JLabel lblDataEmprestimo = new JLabel("Data Empréstimo:");
        lblDataEmprestimo.setFont(labelFont);
        lblDataEmprestimo.setPreferredSize(new Dimension(120, 25));
        gbcEmprestimo.gridx = 0;
        gbcEmprestimo.gridy = 1;
        gbcEmprestimo.gridwidth = 1;
        gbcEmprestimo.weightx = 0.0;
        emprestimoPanel.add(lblDataEmprestimo, gbcEmprestimo);
        gbcEmprestimo.gridx = 1;
        gbcEmprestimo.gridy = 1;
        gbcEmprestimo.weightx = 1.0;
        emprestimoPanel.add(dateEmprestimo, gbcEmprestimo);
        JLabel lblDataDevolucao = new JLabel("Data Devolução:");
        lblDataDevolucao.setFont(labelFont);
        lblDataDevolucao.setPreferredSize(new Dimension(120, 25));
        gbcEmprestimo.gridx = 0;
        gbcEmprestimo.gridy = 2;
        gbcEmprestimo.weightx = 0.0;
        emprestimoPanel.add(lblDataDevolucao, gbcEmprestimo);
        gbcEmprestimo.gridx = 1;
        gbcEmprestimo.gridy = 2;
        gbcEmprestimo.weightx = 1.0;
        emprestimoPanel.add(dateDevolucao, gbcEmprestimo);
        JLabel lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setFont(labelFont);
        lblObservacoes.setPreferredSize(new Dimension(120, 25));
        gbcEmprestimo.gridx = 0;
        gbcEmprestimo.gridy = 3;
        gbcEmprestimo.weightx = 0.0;
        emprestimoPanel.add(lblObservacoes, gbcEmprestimo);
        txtObservacoes = new JTextArea(3, 15);
        txtObservacoes.setFont(fieldFont);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setPreferredSize(new Dimension(200, 60));
        gbcEmprestimo.gridx = 1;
        gbcEmprestimo.gridy = 3;
        gbcEmprestimo.weightx = 1.0;
        emprestimoPanel.add(scrollObservacoes, gbcEmprestimo);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.4;
        mainPanel.add(dataPanel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        mainPanel.add(emprestimoPanel, gbc);
        // Seção de Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        botoesPanel.setBackground(backgroundColor);
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setBorder(BorderFactory.createEmptyBorder());
        btnLimpar.setPreferredSize(new Dimension(80, 30));
        btnLimpar.setHorizontalAlignment(SwingConstants.CENTER);
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar.setToolTipText("Limpar todos os campos");
        btnLimpar.addActionListener(e -> limparCampos());
        botoesPanel.add(btnLimpar);
        JButton btnAdicionar = new JButton("Adicionar Empréstimo");
        btnAdicionar.setBackground(thirdiaryColor);
        btnAdicionar.setForeground(Color.WHITE);
        btnAdicionar.setBorder(BorderFactory.createEmptyBorder());
        btnAdicionar.setPreferredSize(new Dimension(140, 30));
        btnAdicionar.setHorizontalAlignment(SwingConstants.CENTER);
        btnAdicionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdicionar.setToolTipText("Adicionar empréstimo");
        btnAdicionar.addActionListener(e -> adicionarEmprestimo());
        botoesPanel.add(btnAdicionar);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(botoesPanel, gbc);
        // Listeners para busca
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
        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Cria o painel da tabela de empréstimos atuais.
     */
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Empréstimos Atuais",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);
        // Definir colunas sem a coluna "ID"
        String[] colunas = {"Produto", "Serial", "Paciente", "Profissional", "Data Empréstimo", "Data Devolução", "Devolvido", "Observações"};
        modeloTabelaEmprestimos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaEmprestimos = new JTable(modeloTabelaEmprestimos) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                try {
                    // Obter o ID da linha para buscar o EmprestimoProduto
                    int id = (int) modeloTabelaEmprestimos.getValueAt(row, 0); // ID está armazenado internamente
                    EmprestimoProduto emp = emprestimoController.buscarPorId(id);
                    if (emp != null && !emp.isDevolvido() && emp.getDataDevolucao() != null) {
                        LocalDate dataDevolucao = emp.getDataDevolucao().toLocalDate();
                        LocalDate hoje = LocalDate.now();
                        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoje, dataDevolucao);
                        if (diasRestantes <= 3 && diasRestantes > 0) {
                            c.setBackground(warningColor); // Amarelo para 3 dias ou menos
                        } else if (diasRestantes <= 0) {
                            c.setBackground(overdueColor); // Vermelho para atrasado
                        } else {
                            c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                        }
                    } else {
                        c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                    }
                } catch (SQLException ex) {
                    c.setBackground(row % 2 == 0 ? rowColorLightGreen : Color.WHITE);
                }
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
        tabelaEmprestimos.setShowGrid(false);
        tabelaEmprestimos.setIntercellSpacing(new Dimension(0, 0));
        tabelaEmprestimos.setFillsViewportHeight(true);
        tabelaEmprestimos.setRowHeight(25);
        tabelaEmprestimos.setFont(fieldFont);
        tabelaEmprestimos.setBackground(backgroundColor);
        JTableHeader header = tabelaEmprestimos.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Centralizar texto
        for (int i = 0; i < tabelaEmprestimos.getColumnCount(); i++) {
            tabelaEmprestimos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        JScrollPane scroll = new JScrollPane(tabelaEmprestimos);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scroll, BorderLayout.CENTER);
        // Painel inferior com botões
        JPanel southPanel = new JPanel(new GridBagLayout());
        southPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBackground(thirdiaryColor);
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setBorder(BorderFactory.createEmptyBorder());
        btnEditar.setPreferredSize(new Dimension(100, 30));
        btnEditar.setHorizontalAlignment(SwingConstants.CENTER);
        btnEditar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEditar.setToolTipText("Editar empréstimo selecionado");
        btnEditar.addActionListener(e -> editarEmprestimo());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        southPanel.add(btnEditar, gbc);
        JButton btnDeletar = new JButton("Deletar");
        btnDeletar.setBackground(Color.RED);
        btnDeletar.setForeground(Color.WHITE);
        btnDeletar.setBorder(BorderFactory.createEmptyBorder());
        btnDeletar.setPreferredSize(new Dimension(100, 30));
        btnDeletar.setHorizontalAlignment(SwingConstants.CENTER);
        btnDeletar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeletar.setToolTipText("Deletar empréstimo selecionado");
        btnDeletar.addActionListener(e -> deletarEmprestimo());
        gbc.gridx = 1;
        southPanel.add(btnDeletar, gbc);
        JButton btnMarcarDevolucao = new JButton("Marcar Devolução");
        btnMarcarDevolucao.setBackground(primaryColor);
        btnMarcarDevolucao.setForeground(Color.WHITE);
        btnMarcarDevolucao.setBorder(BorderFactory.createEmptyBorder());
        btnMarcarDevolucao.setPreferredSize(new Dimension(120, 30));
        btnMarcarDevolucao.setHorizontalAlignment(SwingConstants.CENTER);
        btnMarcarDevolucao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMarcarDevolucao.setToolTipText("Marcar devolução do empréstimo selecionado");
        btnMarcarDevolucao.addActionListener(e -> marcarDevolucao());
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        southPanel.add(btnMarcarDevolucao, gbc);
        panel.add(southPanel, BorderLayout.SOUTH);
        // Listener para seleção na tabela
        tabelaEmprestimos.getSelectionModel().addListSelectionListener(e -> preencherCamposComSelecao());
        return panel;
    }

    /**
     * Atualiza os dados do paciente com base no texto de busca.
     */
    private void atualizarPaciente() {
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

    /**
     * Limpa os campos do paciente.
     */
    private void limparCamposPaciente() {
        txtNomePaciente.setText("");
        txtTelefone.setText("");
        txtIdade.setText("");
        txtEmail.setText("");
    }

    /**
     * Atualiza os dados do produto com base no texto de busca.
     */
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
            txtDescricaoProduto.setText(produtoSelecionado.getDescricao() != null ? produtoSelecionado.getDescricao() : "N/A");
        } else {
            limparCamposProduto();
        }
    }

    /**
     * Limpa os campos do produto.
     */
    private void limparCamposProduto() {
        txtNomeProduto.setText("");
        txtCodigoSerial.setText("");
        txtDescricaoProduto.setText("");
    }

    /**
     * Adiciona um novo empréstimo ao banco de dados.
     */
    private void adicionarEmprestimo() {
        try {
            if (produtoSelecionado == null) {
                throw new IllegalArgumentException("Selecione um produto!");
            }
            if (pacienteSelecionado == null) {
                throw new IllegalArgumentException("Selecione um paciente!");
            }
            if (Sessao.getUsuarioLogado() == null || Sessao.getUsuarioLogado().getProfissionalId() == null) {
                throw new IllegalArgumentException("Nenhum profissional associado ao usuário logado!");
            }
            String codigoSerial = txtCodigoSerial.getText().trim();
            if (codigoSerial.isEmpty()) {
                throw new IllegalArgumentException("Informe o código serial!");
            }
            EmprestimoProduto emp = criarEmprestimoFromCampos(false);
            emp.setProdutoId(produtoSelecionado.getId());
            emp.setCodigoSerial(codigoSerial);
            String usuarioLogado = Sessao.getUsuarioLogado().getUsuario();
            // Verifica e reduz estoque
            estoqueController.reduzirEstoque(emp.getProdutoId(), 1, "Empréstimo para paciente ID: " + emp.getPacienteId(), usuarioLogado);
            emprestimoController.adicionar(emp);
            atualizarTabelaEmprestimos();
            limparCampos();
            JOptionPane.showMessageDialog(this, "Empréstimo adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar empréstimo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Edita o empréstimo selecionado.
     */
    private void editarEmprestimo() {
        int row = tabelaEmprestimos.getSelectedRow();
        if (row >= 0) {
            try {
                int id = (int) modeloTabelaEmprestimos.getValueAt(row, 0); // ID está na primeira coluna internamente
                EmprestimoProduto antigo = emprestimoController.buscarPorId(id);
                EmprestimoProduto novo = criarEmprestimoFromCampos(true);
                novo.setId(id);
                novo.setProdutoId(produtoSelecionado != null ? produtoSelecionado.getId() : antigo.getProdutoId());
                String codigoSerial = txtCodigoSerial.getText().trim();
                if (codigoSerial.isEmpty()) {
                    throw new IllegalArgumentException("Informe o código serial!");
                }
                novo.setCodigoSerial(codigoSerial);
                String usuarioLogado = Sessao.getUsuarioLogado().getUsuario();
                boolean wasEmprestado = !antigo.isDevolvido();
                boolean willBeEmprestado = !novo.isDevolvido();
                int oldProdutoId = antigo.getProdutoId();
                int newProdutoId = novo.getProdutoId();
                if (wasEmprestado && !willBeEmprestado) {
                    estoqueController.incrementarEstoque(oldProdutoId, 1, "Devolução via edição de empréstimo", usuarioLogado);
                } else if (!wasEmprestado && willBeEmprestado) {
                    estoqueController.reduzirEstoque(newProdutoId, 1, "Empréstimo via edição", usuarioLogado);
                } else if (wasEmprestado && willBeEmprestado && oldProdutoId != newProdutoId) {
                    estoqueController.incrementarEstoque(oldProdutoId, 1, "Ajuste de produto em empréstimo via edição", usuarioLogado);
                    estoqueController.reduzirEstoque(newProdutoId, 1, "Ajuste de produto em empréstimo via edição", usuarioLogado);
                }
                emprestimoController.atualizar(novo);
                atualizarTabelaEmprestimos();
                limparCampos();
                JOptionPane.showMessageDialog(this, "Empréstimo atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao editar empréstimo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Deleta o empréstimo selecionado.
     */
    private void deletarEmprestimo() {
        int row = tabelaEmprestimos.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Confirma exclusão?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (int) modeloTabelaEmprestimos.getValueAt(row, 0);
                    EmprestimoProduto emp = emprestimoController.buscarPorId(id);
                    String usuarioLogado = Sessao.getUsuarioLogado().getUsuario();
                    if (!emp.isDevolvido()) {
                        estoqueController.incrementarEstoque(emp.getProdutoId(), 1, "Devolução via deleção de empréstimo", usuarioLogado);
                    }
                    emprestimoController.remover(id);
                    atualizarTabelaEmprestimos();
                    limparCampos();
                    JOptionPane.showMessageDialog(this, "Empréstimo deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao deletar empréstimo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo para deletar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Marca a devolução do empréstimo selecionado.
     */
    private void marcarDevolucao() {
        int row = tabelaEmprestimos.getSelectedRow();
        if (row >= 0) {
            try {
                int id = (int) modeloTabelaEmprestimos.getValueAt(row, 0);
                EmprestimoProduto emp = emprestimoController.buscarPorId(id);
                String usuarioLogado = Sessao.getUsuarioLogado().getUsuario();
                if (!emp.isDevolvido()) {
                    estoqueController.incrementarEstoque(emp.getProdutoId(), 1, "Devolução de empréstimo", usuarioLogado);
                    emprestimoController.marcarDevolucao(id);
                    atualizarTabelaEmprestimos();
                    limparCampos();
                    JOptionPane.showMessageDialog(this, "Devolução marcada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Empréstimo já devolvido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao marcar devolução: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo para marcar devolução.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Atualiza a tabela de empréstimos com dados do banco, exibindo apenas empréstimos não devolvidos.
     */
    private void atualizarTabelaEmprestimos() {
        modeloTabelaEmprestimos.setRowCount(0);
        try {
            List<EmprestimoProduto> lista = emprestimoController.listarTodos();
            for (EmprestimoProduto emp : lista) {
                // Filtra apenas empréstimos não devolvidos
                if (!emp.isDevolvido()) {
                    Produto produto = cacheProdutos.get(emp.getProdutoId());
                    if (produto == null) {
                        produto = produtoController.buscarPorId(emp.getProdutoId());
                        if (produto != null) cacheProdutos.put(produto.getId(), produto);
                    }
                    Paciente paciente = cachePacientes.get(emp.getPacienteId());
                    if (paciente == null) {
                        paciente = pacienteController.buscarPorId(emp.getPacienteId());
                        if (paciente != null) cachePacientes.put(paciente.getId(), paciente);
                    }
                    Profissional profissional = cacheProfissionais.get(emp.getProfissionalId());
                    if (profissional == null) {
                        profissional = profissionalController.buscarPorId(emp.getProfissionalId());
                        if (profissional != null) cacheProfissionais.put(profissional.getId(), profissional);
                    }
                    // Adiciona a linha sem a coluna ID visível, mas mantém ID internamente
                    modeloTabelaEmprestimos.addRow(new Object[]{
                            emp.getId(), // Armazena ID internamente, mas não exibido
                            produto != null ? produto.getNome() : "Desconhecido",
                            emp.getCodigoSerial() != null ? emp.getCodigoSerial() : "N/A",
                            paciente != null ? paciente.getNome() : "Desconhecido",
                            profissional != null ? profissional.getNome() : "Desconhecido",
                            emp.getDataEmprestimo() != null ? emp.getDataEmprestimo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "",
                            emp.getDataDevolucao() != null ? emp.getDataDevolucao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "",
                            emp.isDevolvido() ? "Sim" : "Não",
                            emp.getObservacoes() != null ? emp.getObservacoes() : ""
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar tabela: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Preenche os campos do formulário com os dados da linha selecionada na tabela.
     */
    private void preencherCamposComSelecao() {
        int row = tabelaEmprestimos.getSelectedRow();
        if (row >= 0) {
            int id = (int) modeloTabelaEmprestimos.getValueAt(row, 0); // ID na primeira coluna internamente
            try {
                EmprestimoProduto emp = emprestimoController.buscarPorId(id);
                if (emp != null) {
                    // Preenche produto
                    produtoSelecionado = cacheProdutos.get(emp.getProdutoId());
                    if (produtoSelecionado != null) {
                        txtBuscaProduto.setText(produtoSelecionado.getNome());
                        atualizarProduto();
                    }
                    // Preenche paciente
                    pacienteSelecionado = cachePacientes.get(emp.getPacienteId());
                    if (pacienteSelecionado != null) {
                        txtBuscaPaciente.setText(pacienteSelecionado.getNome());
                        atualizarPaciente();
                    }
                    // Datas
                    if (emp.getDataEmprestimo() != null) {
                        dateEmprestimo.setText(emp.getDataEmprestimo().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                    if (emp.getDataDevolucao() != null) {
                        dateDevolucao.setText(emp.getDataDevolucao().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                    txtObservacoes.setText(emp.getObservacoes() != null ? emp.getObservacoes() : "");
                    txtCodigoSerial.setText(emp.getCodigoSerial() != null ? emp.getCodigoSerial() : "");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar empréstimo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Cria um objeto EmprestimoProduto a partir dos campos do formulário.
     * @param isEdicao Indica se é edição.
     * @return Objeto EmprestimoProduto preenchido.
     * @throws IllegalArgumentException Se dados inválidos.
     */
    private EmprestimoProduto criarEmprestimoFromCampos(boolean isEdicao) {
        EmprestimoProduto emp = new EmprestimoProduto();
        if (pacienteSelecionado == null) {
            throw new IllegalArgumentException("Selecione um paciente!");
        }
        emp.setPacienteId(pacienteSelecionado.getId());
        if (Sessao.getUsuarioLogado() == null || Sessao.getUsuarioLogado().getProfissionalId() == null) {
            throw new IllegalArgumentException("Nenhum profissional associado ao usuário logado!");
        }
        emp.setProfissionalId(Sessao.getUsuarioLogado().getProfissionalId());
        // Data de empréstimo
        String dataText = dateEmprestimo.getText();
        if (!dataText.matches("\\d{2}/\\d{2}/\\d{4}")) {
            throw new IllegalArgumentException("Data de empréstimo inválida!");
        }
        LocalDate date = LocalDate.parse(dataText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        emp.setDataEmprestimo(date.atStartOfDay());
        // Data de devolução opcional
        String dataDevolucaoText = dateDevolucao.getText();
        if (!dataDevolucaoText.equals("__/__/____") && !dataDevolucaoText.isEmpty()) {
            if (!dataDevolucaoText.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new IllegalArgumentException("Data de devolução inválida!");
            }
            LocalDate dateDev = LocalDate.parse(dataDevolucaoText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            emp.setDataDevolucao(dateDev.atStartOfDay());
        }
        emp.setObservacoes(txtObservacoes.getText().trim());
        emp.setDevolvido(emp.getDataDevolucao() != null);
        return emp;
    }

    /**
     * Limpa todos os campos do formulário.
     */
    private void limparCampos() {
        txtBuscaPaciente.setText("");
        txtBuscaProduto.setText("");
        limparCamposPaciente();
        limparCamposProduto();
        dateEmprestimo.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateDevolucao.setText("");
        txtObservacoes.setText("");
        pacienteSelecionado = null;
        produtoSelecionado = null;
        tabelaEmprestimos.clearSelection();
    }
}