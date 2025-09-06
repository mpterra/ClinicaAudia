package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;

import controller.ProfissionalController;
import exception.CampoObrigatorioException;
import model.Endereco;
import model.Profissional;
import util.CPFUtils;
import util.Sessao;
import util.ViaCepService;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CadastroProfissionalPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfNome, tfEmail;
    private JFormattedTextField tfCpf, tfTelefone, tfCep;
    private JTextField tfDataNascimento;
    private JTextField tfRua, tfNumero, tfComplemento, tfBairro, tfCidade;
    private JComboBox<String> cbEstado;
    private JComboBox<String> cbSexo, cbTipo;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaProfissionais;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfPesquisar;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lblErroData, lblValidaCpf;

    public CadastroProfissionalPanel() throws SQLException {
        setLayout(new BorderLayout(10, 20));

        JLabel lblTitulo = new JLabel("Cadastro de Profissional", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaProfissionaisComPesquisa();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        panelCadastro.setPreferredSize(new Dimension(500, 600));
        panelTabela.setPreferredSize(new Dimension(500, 600));

        add(splitPane, BorderLayout.CENTER);

        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarProfissional();
            } catch (CampoObrigatorioException e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar profissional: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarProfissionais();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelProfissional = new JPanel(new GridBagLayout());
        panelProfissional.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar novo profissional",
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int campoColumns = 25;
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        // Nome
        gbc.gridx = 0; gbc.gridy = 1;
        panelProfissional.add(new JLabel("Nome:"), gbc);
        tfNome = new JTextField(campoColumns);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelProfissional.add(tfNome, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Sexo
        gbc.gridx = 0; gbc.gridy = 2;
        panelProfissional.add(new JLabel("Sexo:"), gbc);
        cbSexo = new JComboBox<>(new String[]{"Masculino", "Feminino"});
        cbSexo.setCursor(handCursor);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelProfissional.add(cbSexo, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // CPF
        tfCpf = criarCampoFormatado(panelProfissional, "CPF:", 3, "###.###.###-##", campoColumns, gbc);
        lblValidaCpf = new JLabel("            ");
        lblValidaCpf.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 2; gbc.gridy = 3; // ao lado do campo CPF
        panelProfissional.add(lblValidaCpf, gbc);

        tfCpf.getDocument().addDocumentListener(new DocumentListener() {
            private void validarCPF() {
                String cpf = tfCpf.getText().replaceAll("\\D", "");
                if (cpf.length() == 11) {
                    if (CPFUtils.isCPFValido(cpf)) {
                        lblValidaCpf.setText("CPF válido  ");
                        lblValidaCpf.setForeground(new Color(0, 128, 0));
                    } else {
                        lblValidaCpf.setText("CPF inválido");
                        lblValidaCpf.setForeground(Color.RED);
                    }
                } else {
                    lblValidaCpf.setText("            ");
                }
            }
            public void insertUpdate(DocumentEvent e) { validarCPF(); }
            public void removeUpdate(DocumentEvent e) { validarCPF(); }
            public void changedUpdate(DocumentEvent e) { validarCPF(); }
        });

        // Telefone
        tfTelefone = criarCampoFormatado(panelProfissional, "Telefone:", 5, "(##) #####-####", campoColumns, gbc);

        // Email
        tfEmail = new JTextField(campoColumns);
        gbc.gridx = 0; gbc.gridy = 6;
        panelProfissional.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelProfissional.add(tfEmail, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Data Nascimento
        tfDataNascimento = criarCampoFormatado(panelProfissional, "Data Nascimento:", 7, "##/##/####", campoColumns, gbc);
        lblErroData = new JLabel("   ");
        lblErroData.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblErroData.setForeground(Color.RED);
        gbc.gridx = 2; gbc.gridy = 7; // ao lado do campo de data
        panelProfissional.add(lblErroData, gbc);

        tfDataNascimento.getDocument().addDocumentListener(new DocumentListener() {
            private void validarData() {
                String texto = tfDataNascimento.getText().trim();
                if (texto.isEmpty()) {
                    lblErroData.setText("Data obrigatória!       ");
                    return;
                }
                try {
                    LocalDate data = LocalDate.parse(texto, formatter);
                    LocalDate hoje = LocalDate.now();
                    if (data.isAfter(hoje)) {
                        lblErroData.setText("Data não pode ser futura!");
                    } else if (data.isBefore(hoje.minusYears(125))) {
                        lblErroData.setText("Idade máxima: 125 anos!  ");
                    } else {
                        lblErroData.setText("                         ");
                    }
                } catch (Exception e) {
                    lblErroData.setText("Formato inválido!        ");
                }
            }
            public void insertUpdate(DocumentEvent e) { validarData(); }
            public void removeUpdate(DocumentEvent e) { validarData(); }
            public void changedUpdate(DocumentEvent e) { validarData(); }
        });

        // Tipo
        gbc.gridx = 0; gbc.gridy = 9;
        panelProfissional.add(new JLabel("Tipo:"), gbc);
        cbTipo = new JComboBox<>(new String[]{"FONOAUDIOLOGA", "SECRETARIA"});
        cbTipo.setCursor(handCursor);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelProfissional.add(cbTipo, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Painel Endereço
        JPanel panelEndereco = new JPanel(new GridBagLayout());
        panelEndereco.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Endereço",
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));

        GridBagConstraints gbcEnd = new GridBagConstraints();
        gbcEnd.insets = new Insets(5,5,5,5);
        gbcEnd.anchor = GridBagConstraints.WEST;
        gbcEnd.fill = GridBagConstraints.HORIZONTAL;

        // CEP
        gbcEnd.gridx = 0; gbcEnd.gridy = 0;
        panelEndereco.add(new JLabel("CEP:"), gbcEnd);
        try {
            MaskFormatter cepMask = new MaskFormatter("#####-###");
            cepMask.setPlaceholderCharacter('_');
            tfCep = new JFormattedTextField(cepMask);
            tfCep.setColumns(10);
        } catch (Exception e) { e.printStackTrace(); }
        gbcEnd.gridx = 1;
        panelEndereco.add(tfCep, gbcEnd);

        // Rua
        gbcEnd.gridx = 2;
        panelEndereco.add(new JLabel("Rua:"), gbcEnd);
        tfRua = new JTextField(20);
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
                                tfRua.setText(""); tfBairro.setText(""); tfCidade.setText(""); cbEstado.setSelectedIndex(0);
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
        gbcEnd.gridy = 1; gbcEnd.gridx = 0; panelEndereco.add(new JLabel("Número:"), gbcEnd);
        tfNumero = new JTextField(8); gbcEnd.gridx = 1; panelEndereco.add(tfNumero, gbcEnd);

        gbcEnd.gridx = 2; panelEndereco.add(new JLabel("Complemento:"), gbcEnd);
        tfComplemento = new JTextField(15); gbcEnd.gridx = 3; panelEndereco.add(tfComplemento, gbcEnd);

        gbcEnd.gridy = 2; gbcEnd.gridx = 0; panelEndereco.add(new JLabel("Bairro:"), gbcEnd);
        tfBairro = new JTextField(15); gbcEnd.gridx = 1; panelEndereco.add(tfBairro, gbcEnd);

        gbcEnd.gridx = 2; panelEndereco.add(new JLabel("Cidade:"), gbcEnd);
        tfCidade = new JTextField(15); gbcEnd.gridx = 3; panelEndereco.add(tfCidade, gbcEnd);

        gbcEnd.gridy = 3; gbcEnd.gridx = 0; panelEndereco.add(new JLabel("Estado:"), gbcEnd);
        String[] estados = {"AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
                "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"};
        cbEstado = new JComboBox<>(estados); cbEstado.setCursor(handCursor);
        gbcEnd.gridx = 1; panelEndereco.add(cbEstado, gbcEnd);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        panelWrapper.add(panelProfissional);
        panelWrapper.add(Box.createVerticalStrut(10));
        panelWrapper.add(panelEndereco);
        panelWrapper.add(Box.createVerticalStrut(10));
        panelWrapper.add(panelBotoes);

        btnSalvar.setCursor(handCursor);
        btnLimpar.setCursor(handCursor);

        return panelWrapper;
    }

    private JFormattedTextField criarCampoFormatado(JPanel panel, String label, int y, String mask, int columns, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        try {
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setColumns(columns);
            gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(field, gbc);
            gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
            return field;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private void limparCampos() {
        tfNome.setText(""); tfEmail.setText(""); tfCpf.setText(""); tfTelefone.setText(""); tfDataNascimento.setText("");
        cbSexo.setSelectedIndex(0); cbTipo.setSelectedIndex(0);
        tfCep.setText(""); tfRua.setText(""); tfNumero.setText(""); tfComplemento.setText("");
        tfBairro.setText(""); tfCidade.setText(""); cbEstado.setSelectedIndex(0);
        lblErroData.setText(" "); lblValidaCpf.setText(" ");
    }

    private void salvarProfissional() throws CampoObrigatorioException, SQLException {
        String nome = tfNome.getText().trim();
        String email = tfEmail.getText().trim();
        String cpf = tfCpf.getText().replaceAll("\\D", "");
        String telefone = tfTelefone.getText().replaceAll("\\D", "");
        String dataStr = tfDataNascimento.getText().trim();
        String sexo = cbSexo.getSelectedItem().toString();
        String tipo = cbTipo.getSelectedItem().toString();

        if (nome.isEmpty()) throw new CampoObrigatorioException("Nome obrigatório");
        if (!CPFUtils.isCPFValido(cpf)) throw new CampoObrigatorioException("CPF inválido");
        if (dataStr.isEmpty()) throw new CampoObrigatorioException("Data obrigatória");

        LocalDate dataNascimento = LocalDate.parse(dataStr, formatter);

        Endereco endereco = new Endereco();
        endereco.setRua(tfRua.getText().trim());
        endereco.setNumero(tfNumero.getText().trim());
        endereco.setComplemento(tfComplemento.getText().trim());
        endereco.setBairro(tfBairro.getText().trim());
        endereco.setCidade(tfCidade.getText().trim());
        endereco.setEstado((String) cbEstado.getSelectedItem());
        endereco.setCep(tfCep.getText().trim());

        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setEmail(email);
        profissional.setCpf(cpf);
        profissional.setTelefone(telefone);
        profissional.setDataNascimento(dataNascimento);
        profissional.setSexo(sexo);
        profissional.setTipo(tipo);
        profissional.setAtivo(true);
        profissional.setEndereco(endereco);
        profissional.setUsuario(Sessao.getUsuarioLogado().getLogin());

        ProfissionalController controller = new ProfissionalController();
        controller.salvar(profissional);

        JOptionPane.showMessageDialog(this, "Profissional salvo com sucesso!");
        limparCampos();
        carregarProfissionais();
    }

    private JPanel criarTabelaProfissionaisComPesquisa() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        modeloTabela = new DefaultTableModel(new Object[]{"Nome", "CPF", "Sexo", "Tipo", "Email"},0);
        tabelaProfissionais = new JTable(modeloTabela);
        tabelaProfissionais.setDefaultEditor(Object.class, null);
        tabelaProfissionais.getTableHeader().setReorderingAllowed(false);

        // Centralizar colunas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i=0; i<tabelaProfissionais.getColumnCount(); i++)
            tabelaProfissionais.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaProfissionais.setRowSorter(sorter);

        tfPesquisar = new JTextField();
        tfPesquisar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisar.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
            }
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Painel para label + campo de pesquisa
        JPanel painelPesquisa = new JPanel(new BorderLayout(5,5));
        JLabel lblPesquisar = new JLabel("Pesquisar profissional: ");
        lblPesquisar.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblPesquisar.setForeground(Color.DARK_GRAY);
        painelPesquisa.add(lblPesquisar, BorderLayout.WEST);
        painelPesquisa.add(tfPesquisar, BorderLayout.CENTER);

        panel.add(painelPesquisa, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabelaProfissionais), BorderLayout.CENTER);

        return panel;
    }

    private void carregarProfissionais() throws SQLException {
        modeloTabela.setRowCount(0);
        ProfissionalController controller = new ProfissionalController();
        List<Profissional> lista = controller.listarTodos();
        for (Profissional p : lista) {
            modeloTabela.addRow(new Object[]{
                    p.getNome(), p.getCpf(), p.getSexo(), p.getTipo(), p.getEmail()
            });
        }
    }
}
