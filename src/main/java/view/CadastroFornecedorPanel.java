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
import controller.EnderecoController;
import controller.FornecedorController;
import exception.CampoObrigatorioException;
import model.Endereco;
import model.Fornecedor;
import util.CNPJUtils;
import util.Sessao;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// Painel para cadastro e listagem de fornecedores
public class CadastroFornecedorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfNome, tfEmail, tfPesquisar;
    private JFormattedTextField tfCnpj, tfTelefone;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaFornecedores;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblValidaCnpj;
    private EnderecoPanel enderecoPanel; // Painel de endereço reutilizável

    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 17);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 13);
    private final Font tableFont = new Font("SansSerif", Font.PLAIN, 13);

    // Construtor
    public CadastroFornecedorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 10, 10));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Fornecedor", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaFornecedoresComPesquisa();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.50);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Garantir que o JSplitPane inicie com proporção correta
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.50));
        revalidate();
        repaint();

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarFornecedor();
            } catch (CampoObrigatorioException e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (java.sql.SQLIntegrityConstraintViolationException e1) {
                JOptionPane.showMessageDialog(this, "Erro: já existe um fornecedor cadastrado com este CNPJ!",
                        "CNPJ duplicado", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar fornecedor: " + e1.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar dados iniciais
        try {
            carregarFornecedores();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Painel de informações do fornecedor
        JPanel panelFornecedor = new JPanel(new GridBagLayout());
        panelFornecedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Cadastrar Novo Fornecedor",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panelFornecedor.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Nome
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(labelFont);
        panelFornecedor.add(lblNome, gbc);
        tfNome = new JTextField(20);
        tfNome.setPreferredSize(new Dimension(300, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelFornecedor.add(tfNome, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // CNPJ
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblCnpj = new JLabel("CNPJ:");
        lblCnpj.setFont(labelFont);
        panelFornecedor.add(lblCnpj, gbc);
        try {
            MaskFormatter cnpjMask = new MaskFormatter("##.###.###/####-##");
            cnpjMask.setPlaceholderCharacter('_');
            tfCnpj = new JFormattedTextField(cnpjMask);
            tfCnpj.setPreferredSize(new Dimension(150, 25));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelFornecedor.add(tfCnpj, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Label de validação de CNPJ
        lblValidaCnpj = new JLabel(" ");
        lblValidaCnpj.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblValidaCnpj.setForeground(Color.RED);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelFornecedor.add(lblValidaCnpj, gbc);

        // Validação de CNPJ
        tfCnpj.getDocument().addDocumentListener(new DocumentListener() {
            private void validarCNPJ() {
                String cnpj = tfCnpj.getText().replaceAll("\\D", "");
                if (cnpj.length() == 14) {
                    if (CNPJUtils.isCNPJValido(cnpj)) {
                        lblValidaCnpj.setText("CNPJ válido");
                        lblValidaCnpj.setForeground(new Color(0, 128, 0));
                    } else {
                        lblValidaCnpj.setText("CNPJ inválido");
                        lblValidaCnpj.setForeground(Color.RED);
                    }
                } else {
                    lblValidaCnpj.setText(" ");
                }
            }
            public void insertUpdate(DocumentEvent e) { validarCNPJ(); }
            public void removeUpdate(DocumentEvent e) { validarCNPJ(); }
            public void changedUpdate(DocumentEvent e) { validarCNPJ(); }
        });

        // Telefone
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        panelFornecedor.add(lblTelefone, gbc);
        try {
            MaskFormatter telefoneMask = new MaskFormatter("(##) #####-####");
            telefoneMask.setPlaceholderCharacter('_');
            tfTelefone = new JFormattedTextField(telefoneMask);
            tfTelefone.setPreferredSize(new Dimension(150, 25));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelFornecedor.add(tfTelefone, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        panelFornecedor.add(lblEmail, gbc);
        tfEmail = new JTextField(20);
        tfEmail.setPreferredSize(new Dimension(300, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelFornecedor.add(tfEmail, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Painel de endereço
        enderecoPanel = new EnderecoPanel(primaryColor);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotoes.setBackground(backgroundColor);
        btnLimpar = new JButton("Limpar");
        btnLimpar.setBackground(Color.LIGHT_GRAY);
        btnLimpar.setForeground(Color.BLACK);
        btnLimpar.setPreferredSize(new Dimension(80, 30));
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(primaryColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(80, 30));
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotoes.add(btnLimpar);
        panelBotoes.add(btnSalvar);

        panelWrapper.add(panelFornecedor);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(enderecoPanel);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelBotoes);

        return panelWrapper;
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarTabelaFornecedoresComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Fornecedores Cadastrados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Fornecedor:");
        lblPesquisar.setFont(labelFont);
        tfPesquisar = new JTextField(15);
        tfPesquisar.setPreferredSize(new Dimension(150, 25));
        panelBusca.add(lblPesquisar);
        panelBusca.add(tfPesquisar);

        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Tabela
        String[] colunas = {"Nome", "CNPJ", "Telefone", "Email"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaFornecedores = new JTable(modeloTabela);
        tabelaFornecedores.setRowHeight(20);
        tabelaFornecedores.setShowGrid(false);
        tabelaFornecedores.setIntercellSpacing(new Dimension(0, 0));
        tabelaFornecedores.setFont(tableFont);

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
        for (int i = 0; i < tabelaFornecedores.getColumnCount(); i++) {
            tabelaFornecedores.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaFornecedores.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaFornecedores.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaFornecedores);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);

        return panel;
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        tfNome.setText("");
        tfCnpj.setText("");
        tfTelefone.setText("");
        tfEmail.setText("");
        enderecoPanel.limparCampos();
        lblValidaCnpj.setText(" ");
    }

    // Salva o fornecedor no banco
    private void salvarFornecedor() throws CampoObrigatorioException, SQLException {
        String nome = tfNome.getText().trim();
        String cnpj = tfCnpj.getText();
        String telefone = tfTelefone.getText();
        String email = tfEmail.getText().trim();

        if (nome.isEmpty()) throw new CampoObrigatorioException("Nome obrigatório");
        if (!CNPJUtils.isCNPJValido(cnpj.replaceAll("\\D", ""))) throw new CampoObrigatorioException("CNPJ inválido");

        Endereco endereco = enderecoPanel.getEndereco();

        EnderecoController enderecoController = new EnderecoController();
        enderecoController.adicionarEndereco(endereco);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(nome);
        fornecedor.setCnpj(cnpj);
        fornecedor.setTelefone(telefone);
        fornecedor.setEmail(email);
        fornecedor.setIdEndereco(endereco.getId());
        fornecedor.setUsuario(Sessao.getUsuarioLogado().getLogin());

        FornecedorController controller = new FornecedorController();
        controller.salvar(fornecedor);

        JOptionPane.showMessageDialog(this, "Fornecedor salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        limparCampos();
        carregarFornecedores();
    }

    // Carrega os fornecedores na tabela
    private void carregarFornecedores() throws SQLException {
        modeloTabela.setRowCount(0);
        FornecedorController controller = new FornecedorController();
        List<Fornecedor> lista = controller.listarTodos();
        for (Fornecedor f : lista) {
            modeloTabela.addRow(new Object[]{f.getNome(), f.getCnpj(), f.getTelefone(), f.getEmail()});
        }
    }
}