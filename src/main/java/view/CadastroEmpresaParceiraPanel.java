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
import util.ViaCepService;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CadastroEmpresaParceiraPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CadastroEmpresaParceiraPanel.class.getName());

    // Componentes de entrada
    private JTextField tfNome, tfEmail, tfRua, tfNumero, tfComplemento, tfBairro, tfCidade, tfPesquisar;
    private JFormattedTextField tfCnpj, tfTelefone, tfCep;
    private JComboBox<String> cbEstado;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaEmpresas;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblValidaCnpj;

    // Estilo visual
    private final Color primaryColor = new Color(138, 43, 226); // Roxo
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightLilac = new Color(230, 230, 250); // Lilás claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 18); // Título principal
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14); // Labels, TitledBorder, tabela

    // Construtor
    public CadastroEmpresaParceiraPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 15, 15, 15));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Empresa Parceira", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaEmpresasComPesquisa();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.49); // Proporção 49-51
        splitPane.setDividerSize(7);
        splitPane.setContinuousLayout(true); // Transição suave ao redimensionar
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Garantir proporção 49-51 do JSplitPane
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.49));
        revalidate();
        repaint();

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarEmpresa();
            } catch (CampoObrigatorioException e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e1) {
                LOGGER.log(Level.SEVERE, "Erro ao salvar empresa: " + e1.getMessage(), e1);
                JOptionPane.showMessageDialog(this, "Erro ao salvar empresa: " + e1.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar dados iniciais
        try {
            carregarEmpresas();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar empresas: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao carregar empresas: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

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
                new EmptyBorder(10, 10, 10, 10)));
        panelEmpresa.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Nome
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(labelFont);
        panelEmpresa.add(lblNome, gbc);
        tfNome = new JTextField(20);
        tfNome.setPreferredSize(new Dimension(300, 30));
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
            tfCnpj.setPreferredSize(new Dimension(150, 30));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar máscara de CNPJ: " + e.getMessage(), e);
        }
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelEmpresa.add(tfCnpj, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Label de validação de CNPJ
        lblValidaCnpj = new JLabel(" ");
        lblValidaCnpj.setFont(new Font("SansSerif", Font.BOLD, 14));
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
            tfTelefone.setPreferredSize(new Dimension(150, 30));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar máscara de telefone: " + e.getMessage(), e);
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
        tfEmail.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelEmpresa.add(tfEmail, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Painel Endereço
        JPanel panelEndereco = new JPanel(new GridBagLayout());
        panelEndereco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Endereço",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panelEndereco.setBackground(backgroundColor);

        GridBagConstraints gbcEnd = new GridBagConstraints();
        gbcEnd.insets = new Insets(5, 10, 5, 10);
        gbcEnd.anchor = GridBagConstraints.WEST;
        gbcEnd.fill = GridBagConstraints.HORIZONTAL;

        // CEP
        gbcEnd.gridx = 0;
        gbcEnd.gridy = 0;
        JLabel lblCep = new JLabel("CEP:");
        lblCep.setFont(labelFont);
        panelEndereco.add(lblCep, gbcEnd);
        try {
            MaskFormatter cepMask = new MaskFormatter("#####-###");
            cepMask.setPlaceholderCharacter('_');
            tfCep = new JFormattedTextField(cepMask);
            tfCep.setPreferredSize(new Dimension(100, 30));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar máscara de CEP: " + e.getMessage(), e);
        }
        gbcEnd.gridx = 1;
        panelEndereco.add(tfCep, gbcEnd);

        // Buscar endereço via CEP
        tfCep.getDocument().addDocumentListener(new DocumentListener() {
            private void buscarEndereco() {
                String cep = tfCep.getText().replaceAll("\\D", "");
                if (cep.length() != 8) return;
                new SwingWorker<Endereco, Void>() {
                    @Override
                    protected Endereco doInBackground() throws Exception {
                        return ViaCepService.buscarEndereco(cep);
                    }
                    @Override
                    protected void done() {
                        try {
                            Endereco endereco = get();
                            if (endereco != null) {
                                tfRua.setText(endereco.getRua());
                                tfBairro.setText(endereco.getBairro());
                                tfCidade.setText(endereco.getCidade());
                                cbEstado.setSelectedItem(endereco.getEstado());
                            } else {
                                tfRua.setText("");
                                tfBairro.setText("");
                                tfCidade.setText("");
                                cbEstado.setSelectedIndex(0);
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Erro ao buscar endereço via CEP: " + ex.getMessage(), ex);
                        }
                    }
                }.execute();
            }
            public void insertUpdate(DocumentEvent e) { buscarEndereco(); }
            public void removeUpdate(DocumentEvent e) { buscarEndereco(); }
            public void changedUpdate(DocumentEvent e) { buscarEndereco(); }
        });

        // Rua
        gbcEnd.gridx = 2;
        gbcEnd.gridy = 0;
        JLabel lblRua = new JLabel("Rua:");
        lblRua.setFont(labelFont);
        panelEndereco.add(lblRua, gbcEnd);
        tfRua = new JTextField(20);
        tfRua.setPreferredSize(new Dimension(150, 30));
        gbcEnd.gridx = 3;
        panelEndereco.add(tfRua, gbcEnd);

        // Número, Complemento, Bairro, Cidade, Estado
        gbcEnd.gridy = 1;
        gbcEnd.gridx = 0;
        JLabel lblNumero = new JLabel("Número:");
        lblNumero.setFont(labelFont);
        panelEndereco.add(lblNumero, gbcEnd);
        tfNumero = new JTextField(8);
        tfNumero.setPreferredSize(new Dimension(100, 30));
        gbcEnd.gridx = 1;
        panelEndereco.add(tfNumero, gbcEnd);

        gbcEnd.gridx = 2;
        JLabel lblComplemento = new JLabel("Complemento:");
        lblComplemento.setFont(labelFont);
        panelEndereco.add(lblComplemento, gbcEnd);
        tfComplemento = new JTextField(15);
        tfComplemento.setPreferredSize(new Dimension(150, 30));
        gbcEnd.gridx = 3;
        panelEndereco.add(tfComplemento, gbcEnd);

        gbcEnd.gridy = 2;
        gbcEnd.gridx = 0;
        JLabel lblBairro = new JLabel("Bairro:");
        lblBairro.setFont(labelFont);
        panelEndereco.add(lblBairro, gbcEnd);
        tfBairro = new JTextField(15);
        tfBairro.setPreferredSize(new Dimension(150, 30));
        gbcEnd.gridx = 1;
        panelEndereco.add(tfBairro, gbcEnd);

        gbcEnd.gridx = 2;
        JLabel lblCidade = new JLabel("Cidade:");
        lblCidade.setFont(labelFont);
        panelEndereco.add(lblCidade, gbcEnd);
        tfCidade = new JTextField(15);
        tfCidade.setPreferredSize(new Dimension(150, 30));
        gbcEnd.gridx = 3;
        panelEndereco.add(tfCidade, gbcEnd);

        gbcEnd.gridy = 3;
        gbcEnd.gridx = 0;
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(labelFont);
        panelEndereco.add(lblEstado, gbcEnd);
        String[] estados = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
                "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
        cbEstado = new JComboBox<>(estados);
        cbEstado.setPreferredSize(new Dimension(100, 30));
        cbEstado.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcEnd.gridx = 1;
        panelEndereco.add(cbEstado, gbcEnd);

        // Botões
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

        panelWrapper.add(panelEmpresa);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelEndereco);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelBotoes);

        return panelWrapper;
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarTabelaEmpresasComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Empresas Cadastradas",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Empresa:");
        lblPesquisar.setFont(labelFont);
        tfPesquisar = new JTextField(15);
        tfPesquisar.setPreferredSize(new Dimension(200, 30));
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
        tabelaEmpresas.setRowHeight(25);
        tabelaEmpresas.setShowGrid(false);
        tabelaEmpresas.setIntercellSpacing(new Dimension(0, 0));
        tabelaEmpresas.setFont(labelFont);

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
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
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
        tfCep.setText("");
        tfRua.setText("");
        tfNumero.setText("");
        tfComplemento.setText("");
        tfBairro.setText("");
        tfCidade.setText("");
        cbEstado.setSelectedIndex(0);
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

        Endereco endereco = new Endereco();
        endereco.setRua(tfRua.getText().trim());
        endereco.setNumero(tfNumero.getText().trim());
        endereco.setComplemento(tfComplemento.getText().trim());
        endereco.setBairro(tfBairro.getText().trim());
        endereco.setCidade(tfCidade.getText().trim());
        endereco.setEstado((String) cbEstado.getSelectedItem());
        endereco.setCep(tfCep.getText().trim());

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