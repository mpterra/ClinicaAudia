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
import controller.EmpresaParceiraController;
import controller.EnderecoController;
import exception.CampoObrigatorioException;
import model.EmpresaParceira;
import model.Endereco;
import util.CNPJUtils;
import util.Sessao;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// Painel para cadastro e listagem de empresas parceiras
public class CadastroEmpresaParceiraPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfNome, tfEmail, tfPesquisar;
    private JFormattedTextField tfCnpj, tfTelefone;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaEmpresas;
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
    public CadastroEmpresaParceiraPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 10, 10));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Empresa Parceira", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaEmpresasComPesquisa();

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
                salvarEmpresa();
            } catch (CampoObrigatorioException e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (java.sql.SQLIntegrityConstraintViolationException e1) {
                JOptionPane.showMessageDialog(this, "Erro: já existe uma empresa cadastrada com este CNPJ!",
                        "CNPJ duplicado", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar empresa: " + e1.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar dados iniciais
        try {
            carregarEmpresas();
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

        // Painel de informações da empresa
        JPanel panelEmpresa = new JPanel(new GridBagLayout());
        panelEmpresa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Cadastrar Nova Empresa Parceira",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panelEmpresa.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Nome
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(labelFont);
        panelEmpresa.add(lblNome, gbc);
        tfNome = new JTextField(20);
        tfNome.setPreferredSize(new Dimension(300, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelEmpresa.add(tfNome, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // CNPJ
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblCnpj = new JLabel("CNPJ:");
        lblCnpj.setFont(labelFont);
        panelEmpresa.add(lblCnpj, gbc);
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
        panelEmpresa.add(tfCnpj, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Label de validação de CNPJ
        lblValidaCnpj = new JLabel(" ");
        lblValidaCnpj.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblValidaCnpj.setForeground(Color.RED);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelEmpresa.add(lblValidaCnpj, gbc);

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
        panelEmpresa.add(lblTelefone, gbc);
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
        panelEmpresa.add(tfTelefone, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        panelEmpresa.add(lblEmail, gbc);
        tfEmail = new JTextField(20);
        tfEmail.setPreferredSize(new Dimension(300, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelEmpresa.add(tfEmail, gbc);
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

        panelWrapper.add(panelEmpresa);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(enderecoPanel);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelBotoes);

        return panelWrapper;
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarTabelaEmpresasComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Empresas Cadastradas",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Empresa:");
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
        tabelaEmpresas = new JTable(modeloTabela);
        tabelaEmpresas.setRowHeight(20);
        tabelaEmpresas.setShowGrid(false);
        tabelaEmpresas.setIntercellSpacing(new Dimension(0, 0));
        tabelaEmpresas.setFont(tableFont);

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
        for (int i = 0; i < tabelaEmpresas.getColumnCount(); i++) {
            tabelaEmpresas.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaEmpresas.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaEmpresas.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaEmpresas);
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

    // Salva a empresa no banco
    private void salvarEmpresa() throws CampoObrigatorioException, SQLException {
        String nome = tfNome.getText().trim();
        String cnpj = tfCnpj.getText();
        String telefone = tfTelefone.getText();
        String email = tfEmail.getText().trim();

        if (nome.isEmpty()) throw new CampoObrigatorioException("Nome obrigatório");
        if (!CNPJUtils.isCNPJValido(cnpj.replaceAll("\\D", ""))) throw new CampoObrigatorioException("CNPJ inválido");

        Endereco endereco = enderecoPanel.getEndereco();

        EnderecoController enderecoController = new EnderecoController();
        enderecoController.adicionarEndereco(endereco);

        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome(nome);
        empresa.setCnpj(cnpj);
        empresa.setTelefone(telefone);
        empresa.setEmail(email);
        empresa.setEndereco(endereco);
        empresa.setUsuario(Sessao.getUsuarioLogado().getLogin());

        EmpresaParceiraController controller = new EmpresaParceiraController();
        controller.salvar(empresa);

        JOptionPane.showMessageDialog(this, "Empresa salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        limparCampos();
        carregarEmpresas();
    }

    // Carrega as empresas na tabela
    private void carregarEmpresas() throws SQLException {
        modeloTabela.setRowCount(0);
        EmpresaParceiraController controller = new EmpresaParceiraController();
        List<EmpresaParceira> lista = controller.listarTodos();
        for (EmpresaParceira e : lista) {
            modeloTabela.addRow(new Object[]{e.getNome(), e.getCnpj(), e.getTelefone(), e.getEmail()});
        }
    }
}