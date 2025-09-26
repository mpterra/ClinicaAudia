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

import controller.PacienteController;
import exception.CampoObrigatorioException;
import model.Paciente;
import model.Endereco;
import util.CPFUtils;
import util.Sessao;
import util.ViaCepService;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Painel para cadastro e listagem de pacientes
public class CadastroPacientePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes de entrada
    private JTextField tfNome, tfEmail, tfRua, tfNumero, tfComplemento, tfBairro, tfCidade, tfPesquisar;
    private JFormattedTextField tfCpf, tfTelefone, tfCep, tfDataNascimento;
    private JComboBox<String> cbEstado, cbSexo;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaPacientes;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblErroData, lblValidaCpf;

    // Estilo visual
    private final Color primaryColor = new Color(30, 144, 255); // Azul
    private final Color backgroundColor = new Color(245, 245, 245); // Fundo geral
    private final Color rowColorLightBlue = new Color(230, 240, 255); // Azul claro para linhas pares
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 17);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 13);
    private final Font tableFont = new Font("SansSerif", Font.PLAIN, 13);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Construtor
    public CadastroPacientePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 10, 10));
        setBackground(backgroundColor);

        // Título do painel
        JLabel lblTitulo = new JLabel("Cadastro de Paciente", SwingConstants.CENTER);
        lblTitulo.setFont(titleFont);
        lblTitulo.setForeground(primaryColor);
        lblTitulo.setBorder(new EmptyBorder(5, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis de cadastro e tabela
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaPacientesComPesquisa();

        // SplitPane para dividir cadastro e tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.48);
        splitPane.setDividerSize(7);
        splitPane.setBackground(backgroundColor);

        add(splitPane, BorderLayout.CENTER);

        // Garantir que o JSplitPane inicie com proporção correta
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.48));
        revalidate();
        repaint();

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarPaciente();
            } catch (CampoObrigatorioException e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar paciente: " + e1.getMessage(), "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar dados iniciais
        carregarPacientes();
    }

    // Cria o painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(backgroundColor);
        panelWrapper.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Painel de informações do paciente
        JPanel panelPaciente = new JPanel(new GridBagLayout());
        panelPaciente.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Cadastrar Novo Paciente",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panelPaciente.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Nome
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(labelFont);
        panelPaciente.add(lblNome, gbc);
        tfNome = new JTextField(20);
        tfNome.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfNome, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Sexo
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblSexo = new JLabel("Sexo:");
        lblSexo.setFont(labelFont);
        panelPaciente.add(lblSexo, gbc);
        cbSexo = new JComboBox<>(new String[]{"Masculino", "Feminino"});
        cbSexo.setPreferredSize(new Dimension(150, 25));
        cbSexo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(cbSexo, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // CPF
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblCpf = new JLabel("CPF:");
        lblCpf.setFont(labelFont);
        panelPaciente.add(lblCpf, gbc);
        try {
            MaskFormatter cpfMask = new MaskFormatter("###.###.###-##");
            cpfMask.setPlaceholderCharacter('_');
            tfCpf = new JFormattedTextField(cpfMask);
            tfCpf.setPreferredSize(new Dimension(100, 25));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfCpf, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Data Nascimento
        gbc.gridx = 2;
        gbc.gridy = 2;
        JLabel lblDataNascimento = new JLabel("Data Nascimento:");
        lblDataNascimento.setFont(labelFont);
        panelPaciente.add(lblDataNascimento, gbc);
        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/####");
            dataMask.setPlaceholderCharacter('_');
            tfDataNascimento = new JFormattedTextField(dataMask);
            tfDataNascimento.setPreferredSize(new Dimension(100, 25));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfDataNascimento, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Labels de alerta
        lblValidaCpf = new JLabel(" ");
        lblValidaCpf.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblValidaCpf.setForeground(Color.RED);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelPaciente.add(lblValidaCpf, gbc);

        lblErroData = new JLabel(" ");
        lblErroData.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblErroData.setForeground(Color.RED);
        gbc.gridx = 3;
        gbc.gridy = 3;
        panelPaciente.add(lblErroData, gbc);

        // Listeners de validação
        tfCpf.getDocument().addDocumentListener(new DocumentListener() {
            private void validarCPF() {
                String cpf = tfCpf.getText().replaceAll("\\D", "");
                if (cpf.length() == 11) {
                    if (CPFUtils.isCPFValido(cpf)) {
                        lblValidaCpf.setText("CPF válido");
                        lblValidaCpf.setForeground(new Color(0, 128, 0));
                    } else {
                        lblValidaCpf.setText("CPF inválido");
                        lblValidaCpf.setForeground(Color.RED);
                    }
                } else {
                    lblValidaCpf.setText(" ");
                }
            }
            public void insertUpdate(DocumentEvent e) { validarCPF(); }
            public void removeUpdate(DocumentEvent e) { validarCPF(); }
            public void changedUpdate(DocumentEvent e) { validarCPF(); }
        });

        tfDataNascimento.getDocument().addDocumentListener(new DocumentListener() {
            private void validarData() {
                String texto = tfDataNascimento.getText().trim();
                if (texto.isEmpty()) {
                    lblErroData.setText("Data obrigatória!");
                    return;
                }
                try {
                    LocalDate data = LocalDate.parse(texto, formatter);
                    LocalDate hoje = LocalDate.now();
                    if (data.isAfter(hoje))
                        lblErroData.setText("Data não pode ser futura!");
                    else if (data.isBefore(hoje.minusYears(125)))
                        lblErroData.setText("Idade máxima: 125 anos!");
                    else
                        lblErroData.setText(" ");
                } catch (Exception e) {
                    lblErroData.setText("Formato inválido!");
                }
            }
            public void insertUpdate(DocumentEvent e) { validarData(); }
            public void removeUpdate(DocumentEvent e) { validarData(); }
            public void changedUpdate(DocumentEvent e) { validarData(); }
        });

        // Telefone
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(labelFont);
        panelPaciente.add(lblTelefone, gbc);
        try {
            MaskFormatter telefoneMask = new MaskFormatter("(##) #####-####");
            telefoneMask.setPlaceholderCharacter('_');
            tfTelefone = new JFormattedTextField(telefoneMask);
            tfTelefone.setPreferredSize(new Dimension(150, 25));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfTelefone, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        panelPaciente.add(lblEmail, gbc);
        tfEmail = new JTextField(20);
        tfEmail.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfEmail, gbc);
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
                new EmptyBorder(5, 5, 5, 5)));
        panelEndereco.setBackground(backgroundColor);

        GridBagConstraints gbcEnd = new GridBagConstraints();
        gbcEnd.insets = new Insets(3, 5, 3, 5);
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
            tfCep.setPreferredSize(new Dimension(100, 25));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbcEnd.gridx = 1;
        panelEndereco.add(tfCep, gbcEnd);

        // Rua
        gbcEnd.gridx = 2;
        gbcEnd.gridy = 0;
        JLabel lblRua = new JLabel("Rua:");
        lblRua.setFont(labelFont);
        panelEndereco.add(lblRua, gbcEnd);
        tfRua = new JTextField(20);
        tfRua.setPreferredSize(new Dimension(150, 25));
        gbcEnd.gridx = 3;
        panelEndereco.add(tfRua, gbcEnd);

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
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }.execute();
            }
            public void insertUpdate(DocumentEvent e) { buscarEndereco(); }
            public void removeUpdate(DocumentEvent e) { buscarEndereco(); }
            public void changedUpdate(DocumentEvent e) { buscarEndereco(); }
        });

        // Número, Complemento, Bairro, Cidade, Estado
        gbcEnd.gridy = 1;
        gbcEnd.gridx = 0;
        JLabel lblNumero = new JLabel("Número:");
        lblNumero.setFont(labelFont);
        panelEndereco.add(lblNumero, gbcEnd);
        tfNumero = new JTextField(8);
        tfNumero.setPreferredSize(new Dimension(100, 25));
        gbcEnd.gridx = 1;
        panelEndereco.add(tfNumero, gbcEnd);

        gbcEnd.gridx = 2;
        JLabel lblComplemento = new JLabel("Complemento:");
        lblComplemento.setFont(labelFont);
        panelEndereco.add(lblComplemento, gbcEnd);
        tfComplemento = new JTextField(15);
        tfComplemento.setPreferredSize(new Dimension(150, 25));
        gbcEnd.gridx = 3;
        panelEndereco.add(tfComplemento, gbcEnd);

        gbcEnd.gridy = 2;
        gbcEnd.gridx = 0;
        JLabel lblBairro = new JLabel("Bairro:");
        lblBairro.setFont(labelFont);
        panelEndereco.add(lblBairro, gbcEnd);
        tfBairro = new JTextField(15);
        tfBairro.setPreferredSize(new Dimension(150, 25));
        gbcEnd.gridx = 1;
        panelEndereco.add(tfBairro, gbcEnd);

        gbcEnd.gridx = 2;
        JLabel lblCidade = new JLabel("Cidade:");
        lblCidade.setFont(labelFont);
        panelEndereco.add(lblCidade, gbcEnd);
        tfCidade = new JTextField(15);
        tfCidade.setPreferredSize(new Dimension(150, 25));
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
        cbEstado.setPreferredSize(new Dimension(100, 25));
        cbEstado.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbcEnd.gridx = 1;
        panelEndereco.add(cbEstado, gbcEnd);

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

        panelWrapper.add(panelPaciente);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelEndereco);
        panelWrapper.add(Box.createVerticalStrut(5));
        panelWrapper.add(panelBotoes);

        return panelWrapper;
    }

    // Cria o painel da tabela com pesquisa
    private JPanel criarTabelaPacientesComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1, true),
                        "Pacientes Cadastrados",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        labelFont,
                        primaryColor),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setBackground(backgroundColor);

        // Pesquisa
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelBusca.setBackground(backgroundColor);
        JLabel lblPesquisar = new JLabel("Pesquisar Paciente:");
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
        String[] colunas = {"Nome", "Sexo", "Telefone", "Email", "Data Nascimento"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaPacientes = new JTable(modeloTabela);
        tabelaPacientes.setRowHeight(20);
        tabelaPacientes.setShowGrid(false);
        tabelaPacientes.setIntercellSpacing(new Dimension(0, 0));
        tabelaPacientes.setFont(tableFont);

        // Renderizador para alternar cores das linhas
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? rowColorLightBlue : Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < tabelaPacientes.getColumnCount(); i++) {
            tabelaPacientes.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JTableHeader header = tabelaPacientes.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaPacientes.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaPacientes);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(panelBusca, BorderLayout.NORTH);
        panel.add(scrollTabela, BorderLayout.CENTER);

        return panel;
    }

    // Limpa os campos do formulário
    private void limparCampos() {
        tfNome.setText("");
        tfCpf.setText("");
        lblValidaCpf.setText(" ");
        tfTelefone.setText("");
        tfEmail.setText("");
        tfDataNascimento.setText("");
        lblErroData.setText(" ");
        cbSexo.setSelectedIndex(0);
        tfRua.setText("");
        tfNumero.setText("");
        tfComplemento.setText("");
        tfBairro.setText("");
        tfCidade.setText("");
        cbEstado.setSelectedIndex(0);
        tfCep.setText("");
    }

    // Salva o paciente no banco
    private void salvarPaciente() throws CampoObrigatorioException, SQLException {
        String nome = tfNome.getText().trim();
        String sexoSelecionado = (String) cbSexo.getSelectedItem();
        String cpf = tfCpf.getText().trim();
        String telefone = tfTelefone.getText().trim();
        String email = tfEmail.getText().trim();
        String dataNascimentoStr = tfDataNascimento.getText().trim();
        String sexo = sexoSelecionado.equals("Masculino") ? "M" : "F";

        // Validação de campos obrigatórios
        if (nome.isEmpty()) throw new CampoObrigatorioException("Nome é obrigatório!");
        if (!CPFUtils.isCPFValido(cpf.replaceAll("\\D", ""))) throw new CampoObrigatorioException("CPF inválido!");
        if (dataNascimentoStr.isEmpty()) throw new CampoObrigatorioException("Data de nascimento é obrigatória!");

        // Validação da data de nascimento
        LocalDate dataNascimento;
        try {
            dataNascimento = LocalDate.parse(dataNascimentoStr, formatter);
            LocalDate hoje = LocalDate.now();
            if (dataNascimento.isAfter(hoje)) {
                lblErroData.setText("Data não pode ser futura!");
                throw new CampoObrigatorioException("Data de nascimento não pode ser futura!");
            }
            if (dataNascimento.isBefore(hoje.minusYears(125))) {
                lblErroData.setText("Idade máxima: 125 anos!");
                throw new CampoObrigatorioException("Idade máxima permitida: 125 anos!");
            }
        } catch (Exception e) {
            lblErroData.setText("Formato inválido! Use dd/MM/aaaa");
            throw new CampoObrigatorioException("Data de nascimento inválida! Use dd/MM/aaaa.");
        }

        // Endereço
        Endereco endereco = new Endereco();
        endereco.setRua(tfRua.getText().trim());
        endereco.setNumero(tfNumero.getText().trim());
        endereco.setComplemento(tfComplemento.getText().trim());
        endereco.setBairro(tfBairro.getText().trim());
        endereco.setCidade(tfCidade.getText().trim());
        endereco.setEstado((String) cbEstado.getSelectedItem());
        endereco.setCep(tfCep.getText().trim());

        // Verifica se o endereço é válido
        boolean enderecoValido = !endereco.getRua().isBlank() && !endereco.getCidade().isBlank() && !endereco.getCep().isBlank();
        if (!enderecoValido) {
            endereco = null;
        }

        // Criação do paciente
        String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
        Paciente paciente = new Paciente(0, nome, sexo, cpf, telefone, email, dataNascimento, endereco, usuarioLogado);

        // Salvamento
        PacienteController pc = new PacienteController();
        if (pc.salvarPaciente(paciente)) {
            JOptionPane.showMessageDialog(this, "Paciente salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarPacientes();
        }
    }

    // Carrega os pacientes na tabela
    private void carregarPacientes() {
        try {
            PacienteController pc = new PacienteController();
            List<Paciente> pacientes = pc.listarTodos();
            modeloTabela.setRowCount(0);
            for (Paciente p : pacientes) {
                String dataFormatada = p.getDataNascimento() != null ? p.getDataNascimento().format(formatter) : "";
                modeloTabela.addRow(new Object[]{
                        p.getNome(),
                        p.getSexo().equals("M") ? "Masculino" : "Feminino",
                        p.getTelefone(),
                        p.getEmail(),
                        dataFormatada
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pacientes: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}