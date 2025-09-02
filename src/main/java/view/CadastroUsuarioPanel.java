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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

        criarPainelCadastro();
        criarTabelaUsuariosComPesquisa();

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

    private void criarPainelCadastro() {
        JPanel panelCadastroWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JPanel panelCadastro = new JPanel(new GridBagLayout());
        
        // Alarga o painel de cadastro para ocupar 33% da largura do frame
        int panelWidth = Toolkit.getDefaultToolkit().getScreenSize().width / 3; 
        panelCadastro.setPreferredSize(new Dimension(panelWidth, 350));
        panelCadastro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastro de Usuário",
                TitledBorder.LEADING,
                TitledBorder.TOP));

        panelCadastroWrapper.add(panelCadastro);
        add(panelCadastroWrapper, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // LOGIN
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCadastro.add(new JLabel("Login:"), gbc);

        tfLogin = new JTextField();
        tfLogin.setPreferredSize(new Dimension(panelWidth - 40, 30)); // alarga o campo
        gbc.gridx = 1;
        panelCadastro.add(tfLogin, gbc);

        // SENHA
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCadastro.add(new JLabel("Senha:"), gbc);

        pfSenha = new JPasswordField();
        pfSenha.setPreferredSize(new Dimension(panelWidth - 40, 30));
        gbc.gridx = 1;
        panelCadastro.add(pfSenha, gbc);

        // TIPO
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCadastro.add(new JLabel("Tipo:"), gbc);

        cbTipo = new JComboBox<>(new String[]{"ADMIN", "FONOAUDIOLOGO", "SECRETARIA", "FINANCEIRO"});
        cbTipo.setPreferredSize(new Dimension(panelWidth - 40, 30));
        gbc.gridx = 1;
        panelCadastro.add(cbTipo, gbc);

        // Vincular a profissional?
        gbc.gridx = 0;
        gbc.gridy = 3;
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
        panelCadastro.add(panelRadios, gbc);

        // ComboBox de Profissionais
        cbProfissionais = new JComboBox<>();
        cbProfissionais.setVisible(false);
        cbProfissionais.setPreferredSize(new Dimension(panelWidth - 40, 30));
        gbc.gridy = 4;
        panelCadastro.add(cbProfissionais, gbc);

        // Mostrar/ocultar combo ao clicar radio
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
        gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);
    }


    private void criarTabelaUsuariosComPesquisa() {
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
        tabelaUsuarios.setPreferredScrollableViewportSize(new Dimension(800, 350));

        // Centralizar texto
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

        JPanel panelTabelaWrapper = new JPanel(new BorderLayout());
        panelTabelaWrapper.add(panelPesquisa, BorderLayout.NORTH);
        panelTabelaWrapper.add(scrollTabela, BorderLayout.CENTER);

        add(panelTabelaWrapper, BorderLayout.CENTER);
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
            listaProfissionais = pc.buscarPorAtivo(true); // só ativos
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
