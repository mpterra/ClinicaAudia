package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;

import controller.PacienteController;
import exception.CampoObrigatorioException;
import model.Paciente;
import model.Endereco;
import util.CPFUtils;
import util.Sessao;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CadastroPacientePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfNome, tfEmail;
    private JFormattedTextField tfCpf, tfTelefone, tfCep;
    private JTextField tfDataNascimento;
    private JTextField tfRua, tfNumero, tfComplemento, tfBairro, tfCidade;
    private JComboBox<String> cbEstado;

    private JButton btnSalvar, btnLimpar;
    private JTable tabelaPacientes;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfPesquisar;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lblErroData, lblErroRua, lblValidaCpf;

    public CadastroPacientePanel() {
        setLayout(new BorderLayout(10, 20));

        JLabel lblTitulo = new JLabel("Cadastro de Paciente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaPacientesComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.4); // 40% dados / 60% tabela
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        // Forçar tamanho inicial
        panelCadastro.setPreferredSize(new Dimension(400, 600)); // ajuste conforme seu layout
        panelTabela.setPreferredSize(new Dimension(600, 600));

        add(splitPane, BorderLayout.CENTER);


        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarPaciente();
            } catch (CampoObrigatorioException e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar paciente: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarPacientes();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----------------------------
        // Painel Paciente
        // ----------------------------
        JPanel panelPaciente = new JPanel(new GridBagLayout());
        panelPaciente.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar novo paciente",
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        JLabel lblSubtitulo = new JLabel("Preencha os dados do paciente");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panelPaciente.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        int campoColumns = 25;

        // Nome
        gbc.gridx = 0; gbc.gridy = 1;
        panelPaciente.add(new JLabel("Nome:"), gbc);
        tfNome = new JTextField(campoColumns);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfNome, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // CPF
        tfCpf = criarCampoFormatado(panelPaciente, "CPF:", 2, "###.###.###-##", campoColumns, gbc);
        lblValidaCpf = new JLabel(" "); // inicialmente vazio
        lblValidaCpf.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3;
        panelPaciente.add(lblValidaCpf, gbc);
        gbc.gridwidth = 1;

        // Listener de validação de CPF
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

        // Telefone
        tfTelefone = criarCampoFormatado(panelPaciente, "Telefone:", 4, "(##) #####-####", campoColumns, gbc);

        // Email
        tfEmail = new JTextField(campoColumns);
        gbc.gridx = 0; gbc.gridy = 5;
        panelPaciente.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPaciente.add(tfEmail, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Data de nascimento
        tfDataNascimento = criarCampoFormatado(panelPaciente, "Data Nascimento (dd/MM/aaaa):", 6, "##/##/####", campoColumns, gbc);
        lblErroData = criarLabelErro(panelPaciente, 7, gbc);

        // ----------------------------
        // Painel Endereço
        // ----------------------------
        JPanel panelEndereco = new JPanel(new GridBagLayout());
        panelEndereco.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Endereço",
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));

        GridBagConstraints gbcEnd = new GridBagConstraints();
        gbcEnd.insets = new Insets(5, 5, 5, 5);
        gbcEnd.anchor = GridBagConstraints.WEST;
        gbcEnd.fill = GridBagConstraints.HORIZONTAL;

        // Rua
        gbcEnd.gridx = 0; gbcEnd.gridy = 0;
        panelEndereco.add(new JLabel("Rua:"), gbcEnd);
        tfRua = new JTextField(20);
        gbcEnd.gridx = 1; gbcEnd.gridwidth = 3;
        panelEndereco.add(tfRua, gbcEnd);
        lblErroRua = new JLabel(" "); lblErroRua.setForeground(Color.RED);
        gbcEnd.gridy = 1;
        panelEndereco.add(lblErroRua, gbcEnd);
        gbcEnd.gridwidth = 1;

        // Número | Complemento
        gbcEnd.gridy = 2; gbcEnd.gridx = 0;
        panelEndereco.add(new JLabel("Número:"), gbcEnd);
        tfNumero = new JTextField(8);
        gbcEnd.gridx = 1;
        panelEndereco.add(tfNumero, gbcEnd);

        gbcEnd.gridx = 2;
        panelEndereco.add(new JLabel("Complemento:"), gbcEnd);
        tfComplemento = new JTextField(15);
        gbcEnd.gridx = 3;
        panelEndereco.add(tfComplemento, gbcEnd);

        // Bairro | Cidade
        gbcEnd.gridy = 3; gbcEnd.gridx = 0;
        panelEndereco.add(new JLabel("Bairro:"), gbcEnd);
        tfBairro = new JTextField(15);
        gbcEnd.gridx = 1;
        panelEndereco.add(tfBairro, gbcEnd);

        gbcEnd.gridx = 2;
        panelEndereco.add(new JLabel("Cidade:"), gbcEnd);
        tfCidade = new JTextField(15);
        gbcEnd.gridx = 3;
        panelEndereco.add(tfCidade, gbcEnd);

        // Estado | CEP
        gbcEnd.gridy = 4; gbcEnd.gridx = 0;
        panelEndereco.add(new JLabel("Estado:"), gbcEnd);
        String[] estados = {"AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
                "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"};
        cbEstado = new JComboBox<>(estados);
        gbcEnd.gridx = 1;
        panelEndereco.add(cbEstado, gbcEnd);

        gbcEnd.gridx = 2;
        panelEndereco.add(new JLabel("CEP:"), gbcEnd);
        try {
            MaskFormatter cepMask = new MaskFormatter("#####-###");
            cepMask.setPlaceholderCharacter('_');
            tfCep = new JFormattedTextField(cepMask);
            tfCep.setColumns(10);
        } catch (Exception e) { e.printStackTrace(); }
        gbcEnd.gridx = 3;
        panelEndereco.add(tfCep, gbcEnd);

        // ----------------------------
        // Botões
        // ----------------------------
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        panelWrapper.add(panelPaciente);
        panelWrapper.add(Box.createVerticalStrut(10));
        panelWrapper.add(panelEndereco);
        panelWrapper.add(Box.createVerticalStrut(10));
        panelWrapper.add(panelBotoes);

        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);

        return panelWrapper;
    }

    private JFormattedTextField criarCampoFormatado(JPanel painel, String label, int y, String mask, int columns, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = y;
        painel.add(new JLabel(label), gbc);
        JFormattedTextField tf = null;
        try {
            MaskFormatter mf = new MaskFormatter(mask);
            mf.setPlaceholderCharacter('_');
            tf = new JFormattedTextField(mf);
            tf.setColumns(columns);
        } catch (Exception e) { e.printStackTrace(); }
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painel.add(tf, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        return tf;
    }

    private JLabel criarLabelErro(JPanel painel, int y, GridBagConstraints gbc) {
        gbc.gridx = 1; gbc.gridy = y; gbc.gridwidth = 3;
        JLabel lbl = new JLabel(" "); lbl.setForeground(Color.RED);
        painel.add(lbl, gbc);
        gbc.gridwidth = 1;
        return lbl;
    }

    // ----------------------------
    // Tabela e pesquisa
    // ----------------------------
    private JPanel criarTabelaPacientesComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Nome", "CPF", "Telefone", "Email", "Data Nascimento"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaPacientes = new JTable(modeloTabela);
        tabelaPacientes.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaPacientes.getColumnCount(); i++) {
            tabelaPacientes.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaPacientes.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaPacientes);

        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPesquisar = new JLabel("Pesquisar paciente:");
        lblPesquisar.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblPesquisar.setForeground(Color.DARK_GRAY);
        panelPesquisa.add(lblPesquisar);

        tfPesquisar = new JTextField(20);
        panelPesquisa.add(tfPesquisar);
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText().trim();
                if (texto.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0));
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
                tabelaPacientes.getColumnModel().getColumn(0).setPreferredWidth((int)(totalWidth * 0.2));
                tabelaPacientes.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaPacientes.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaPacientes.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth * 0.25));
                tabelaPacientes.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth * 0.25));
            }
        });

        return panelTabelaWrapper;
    }

    // ----------------------------
    // Limpar e salvar
    // ----------------------------
    private void limparCampos() {
        tfNome.setText("");
        tfCpf.setText(""); lblValidaCpf.setText(" ");
        tfTelefone.setText("");
        tfEmail.setText("");
        tfDataNascimento.setText(""); lblErroData.setText(" ");
        tfRua.setText(""); lblErroRua.setText(" ");
        tfNumero.setText("");
        tfComplemento.setText("");
        tfBairro.setText("");
        tfCidade.setText("");
        cbEstado.setSelectedIndex(0);
        tfCep.setText("");
    }

    private void salvarPaciente() throws CampoObrigatorioException, SQLException {
        String nome = tfNome.getText().trim();
        String cpf = tfCpf.getText().trim();
        String telefone = tfTelefone.getText().trim();
        String email = tfEmail.getText().trim();
        String dataNascimentoStr = tfDataNascimento.getText().trim();

        boolean temErro = false;

        if (nome.isEmpty()) temErro = true;
        if (cpf.isEmpty()) temErro = true;
        if (dataNascimentoStr.isEmpty()) { lblErroData.setText("Data obrigatória!"); temErro = true; } else lblErroData.setText(" ");

        if (temErro) throw new CampoObrigatorioException("Preencha os campos obrigatórios!");

        LocalDate dataNascimento;
        try {
            dataNascimento = LocalDate.parse(dataNascimentoStr, formatter);
        } catch (DateTimeParseException e) {
            throw new CampoObrigatorioException("Data de Nascimento inválida! Use dd/MM/aaaa.");
        }

        Endereco endereco = new Endereco();
        endereco.setRua(tfRua.getText().trim());
        endereco.setNumero(tfNumero.getText().trim());
        endereco.setComplemento(tfComplemento.getText().trim());
        endereco.setBairro(tfBairro.getText().trim());
        endereco.setCidade(tfCidade.getText().trim());
        endereco.setEstado((String) cbEstado.getSelectedItem());
        endereco.setCep(tfCep.getText().trim());

        String usuarioLogado = Sessao.getUsuarioLogado().getLogin();
        Paciente paciente = new Paciente(0, nome, cpf, telefone, email, dataNascimento, endereco, usuarioLogado);

        PacienteController pc = new PacienteController();
        if (pc.salvarPaciente(paciente)) {
            JOptionPane.showMessageDialog(this, "Paciente salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarPacientes();
        }
    }

    private void carregarPacientes() {
        try {
            PacienteController pc = new PacienteController();
            List<Paciente> pacientes = pc.listarTodos();

            modeloTabela.setRowCount(0);

            for (Paciente p : pacientes) {
                String dataFormatada = p.getDataNascimento() != null ? p.getDataNascimento().format(formatter) : "";
                modeloTabela.addRow(new Object[]{
                        p.getNome(),
                        p.getCpf(),
                        p.getTelefone(),
                        p.getEmail(),
                        dataFormatada
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar pacientes: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
