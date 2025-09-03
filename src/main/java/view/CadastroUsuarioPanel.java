package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controller.UsuarioController;
import controller.ProfissionalController;
import exception.LoginDuplicadoException;
import model.Usuario;
import model.Profissional;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CadastroUsuarioPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField tfLogin, tfPesquisar;
    private JPasswordField pfSenha;
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnLimpar;

    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;

    private JRadioButton rbSim, rbNao;
    private JComboBox<String> cbProfissionais;
    private List<Profissional> listaProfissionais = new ArrayList<>();

    public CadastroUsuarioPanel() {
        setLayout(new BorderLayout(10, 20));

        // Criar os painéis
        JPanel panelCadastro = criarPainelCadastro();
        JPanel panelTabela = criarTabelaUsuariosComPesquisa();

        // Divisor horizontal: 50% cadastro, 50% tabela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCadastro, panelTabela);
        splitPane.setResizeWeight(0.5); // 50% cada lado
        splitPane.setContinuousLayout(true); // atualização suave ao arrastar
        splitPane.setDividerSize(5);

        add(splitPane, BorderLayout.CENTER);

        // Listeners
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
            try {
                salvarUsuario();
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar usuário: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (LoginDuplicadoException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Usuário já cadastrado. Utilize outro login", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarUsuarios();
        carregarProfissionais();
    }

    private JPanel criarPainelCadastro() {
        JPanel panelCadastroWrapper = new JPanel(new BorderLayout());
        JPanel panelCadastro = new JPanel(new GridBagLayout());

        panelCadastro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastrar novo usuário",
                TitledBorder.LEADING,
                TitledBorder.TOP));

        panelCadastroWrapper.add(panelCadastro, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // LOGIN
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Login:"), gbc);

        tfLogin = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(tfLogin, gbc);

        // SENHA
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Senha:"), gbc);

        pfSenha = new JPasswordField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(pfSenha, gbc);

        // TIPO
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Tipo:"), gbc);

        cbTipo = new JComboBox<>(new String[]{"ADMIN", "FONOAUDIOLOGO", "SECRETARIA", "FINANCEIRO"});
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbTipo, gbc);

        // Vincular a profissional?
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panelCadastro.add(new JLabel("Vincular a Profissional?"), gbc);

        rbSim = new JRadioButton("Sim");
        rbNao = new JRadioButton("Não", true);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbSim);
        bg.add(rbNao);
        JPanel panelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelRadios.add(rbSim);
        panelRadios.add(rbNao);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(panelRadios, gbc);

        // ComboBox de Profissionais
        cbProfissionais = new JComboBox<>();
        cbProfissionais.setVisible(false);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCadastro.add(cbProfissionais, gbc);

        rbSim.addActionListener(e -> cbProfissionais.setVisible(true));
        rbNao.addActionListener(e -> cbProfissionais.setVisible(false));

        // BOTOES
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);

        return panelCadastroWrapper;
    }

    private JPanel criarTabelaUsuariosComPesquisa() {
        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());

        String[] colunas = {"Login", "Tipo", "Status", "Criação"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setFillsViewportHeight(true);

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaUsuarios.getColumnCount(); i++) {
            tabelaUsuarios.getColumnModel().getColumn(i).setCellRenderer(centralizado);
        }

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaUsuarios.setRowSorter(sorter);

        JScrollPane scrollTabela = new JScrollPane(tabelaUsuarios);

        // Pesquisa
        JPanel panelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPesquisa.add(new JLabel("Pesquisar login:"));
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

        return panelTabelaWrapper;
    }

    private void limparCampos() {
        tfLogin.setText("");
        pfSenha.setText("");
        cbTipo.setSelectedIndex(0);
        rbNao.setSelected(true);
        cbProfissionais.setVisible(false);
    }

    private void salvarUsuario() throws SQLException, LoginDuplicadoException {
        String login = tfLogin.getText().trim();
        String senha = new String(pfSenha.getPassword());
        String tipo = (String) cbTipo.getSelectedItem();

        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario user = new Usuario();
        user.setLogin(login);
        user.setSenha(senha);
        user.setTipo(tipo);

        if (rbSim.isSelected() && cbProfissionais.getSelectedIndex() != -1) {
            int profId = listaProfissionais.get(cbProfissionais.getSelectedIndex()).getId();
            user.setProfissionalId(profId);
        }

        UsuarioController uc = new UsuarioController();
        if (uc.salvar(user)) {
            JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarUsuarios();
        }
    }

    private void carregarUsuarios() {
        try {
            UsuarioController uc = new UsuarioController();
            List<Usuario> usuarios = uc.listarTodos();

            modeloTabela.setRowCount(0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Usuario u : usuarios) {
                String criadoFormatado = u.getCriadoEm() != null ? u.getCriadoEm().format(formatter) : "";

                modeloTabela.addRow(new Object[]{
                        u.getLogin(),
                        u.getTipo(),
                        u.isAtivo() ? "Ativo" : "Inativo",
                        "Criado por " + (u.getUsuario() != null ? u.getUsuario() : "?")
                                + " em " + criadoFormatado
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarProfissionais() {
        try {
            ProfissionalController pc = new ProfissionalController();
            listaProfissionais = pc.buscarPorAtivo(true);
            cbProfissionais.removeAllItems();
            for (Profissional p : listaProfissionais) {
                cbProfissionais.addItem(p.getNome());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar profissionais: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
