package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.ProfissionalController;
import model.Endereco;
import model.Profissional;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CadastroProfissionalPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfNome, tfCPF, tfEmail, tfTelefone, tfPesquisar;
    private JComboBox<String> cbSexo, cbTipo;
    private JFormattedTextField ftfDataNascimento;
    private JCheckBox chbAtivo;
    private JButton btnSalvar, btnLimpar;

    private JTable tabelaProfissionais;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    public CadastroProfissionalPanel() {
        setLayout(new BorderLayout(10, 20));

        // Título
        JLabel lblTitulo = new JLabel("Cadastro de Profissional", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 60));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Painéis
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaProfissionaisComPesquisa();

        // SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panelWrapper.add(splitPane, BorderLayout.CENTER);
        add(panelWrapper, BorderLayout.CENTER);

        // Botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> salvarProfissional());

        carregarProfissionais();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelWrapper = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar novo profissional",
                TitledBorder.LEADING,
                TitledBorder.TOP));

        panelWrapper.add(panel, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.WEST;

        // Subtítulo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblSubtitulo = new JLabel("Preencha os dados do profissional");
        lblSubtitulo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSubtitulo.setForeground(Color.DARK_GRAY);
        panel.add(lblSubtitulo, gbc);
        gbc.gridwidth = 1;

        // Nome
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Nome:"), gbc);
        tfNome = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tfNome, gbc);

        // Sexo
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Sexo:"), gbc);
        cbSexo = new JComboBox<>(new String[]{"M", "F"});
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cbSexo, gbc);

        // CPF
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("CPF:"), gbc);
        tfCPF = new JTextField();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tfCPF, gbc);

        // Data de Nascimento
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Data Nascimento (yyyy-MM-dd):"), gbc);
        ftfDataNascimento = new JFormattedTextField();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(ftfDataNascimento, gbc);

        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Email:"), gbc);
        tfEmail = new JTextField();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tfEmail, gbc);

        // Telefone
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Telefone:"), gbc);
        tfTelefone = new JTextField();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tfTelefone, gbc);

        // Tipo
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Tipo:"), gbc);
        cbTipo = new JComboBox<>(new String[]{"FONOAUDIOLOGA", "SECRETARIA"});
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cbTipo, gbc);

        // Ativo
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Ativo:"), gbc);
        chbAtivo = new JCheckBox();
        chbAtivo.setSelected(true);
        gbc.gridx = 1;
        panel.add(chbAtivo, gbc);

        // Botões
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);
        panel.add(panelBotoes, gbc);

        return panelWrapper;
    }

    private JPanel criarTabelaProfissionaisComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Nome", "Sexo", "CPF", "Data Nascimento", "Tipo", "Status", "Usuário"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaProfissionais = new JTable(modeloTabela);
        tabelaProfissionais.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaProfissionais.getColumnCount(); i++) {
            tabelaProfissionais.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaProfissionais.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaProfissionais);

        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPesquisar = new JLabel("Pesquisar nome:");
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
                tabelaProfissionais.getColumnModel().getColumn(0).setPreferredWidth((int)(totalWidth * 0.2));
                tabelaProfissionais.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth * 0.1));
                tabelaProfissionais.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaProfissionais.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaProfissionais.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth * 0.15));
                tabelaProfissionais.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth * 0.1));
                tabelaProfissionais.getColumnModel().getColumn(6).setPreferredWidth((int)(totalWidth * 0.15));
            }
        });

        return panelTabelaWrapper;
    }

    private void limparCampos() {
        tfNome.setText("");
        tfCPF.setText("");
        ftfDataNascimento.setText("");
        tfEmail.setText("");
        tfTelefone.setText("");
        cbSexo.setSelectedIndex(0);
        cbTipo.setSelectedIndex(0);
        chbAtivo.setSelected(true);
    }

    private void salvarProfissional() {
        String nome = tfNome.getText().trim();
        String cpf = tfCPF.getText().trim();
        String email = tfEmail.getText().trim();
        String telefone = tfTelefone.getText().trim();
        String sexo = (String) cbSexo.getSelectedItem();
        String tipoStr = (String) cbTipo.getSelectedItem();
        boolean ativo = chbAtivo.isSelected();
        LocalDate dataNasc = null;
        if (!ftfDataNascimento.getText().trim().isEmpty()) {
            dataNasc = LocalDate.parse(ftfDataNascimento.getText().trim());
        }

        if (nome.isEmpty() || cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os campos obrigatórios: Nome e CPF", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Profissional p = new Profissional();
        p.setNome(nome);
        p.setCpf(cpf);
        p.setEmail(email);
        p.setTelefone(telefone);
        p.setSexo(sexo);
        p.setTipo(Profissional.TipoProfissional.valueOf(tipoStr));
        p.setDataNascimento(dataNasc);
        p.setAtivo(ativo);

        ProfissionalController pc = new ProfissionalController();
        try {
            if (pc.salvar(p)) {
                JOptionPane.showMessageDialog(this, "Profissional salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
                carregarProfissionais();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao salvar profissional", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar profissional: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarProfissionais() {
        ProfissionalController pc = new ProfissionalController();
        try {
            List<Profissional> lista = pc.listarTodos();
            modeloTabela.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Profissional p : lista) {
                String dataStr = p.getDataNascimento() != null ? p.getDataNascimento().format(fmt) : "";
                modeloTabela.addRow(new Object[]{
                        p.getNome(),
                        p.getSexo(),
                        p.getCpf(),
                        dataStr,
                        p.getTipo(),
                        p.isAtivo() ? "Ativo" : "Inativo",
                        p.getUsuario() != null ? p.getUsuario() : "?"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
