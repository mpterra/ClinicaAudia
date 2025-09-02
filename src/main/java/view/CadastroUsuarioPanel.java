package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import controller.UsuarioController;
import exception.LoginDuplicadoException;
import model.Usuario;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CadastroUsuarioPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField tfLogin;
    private JPasswordField pfSenha;
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnLimpar;
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;

    public CadastroUsuarioPanel() {
        setLayout(new BorderLayout(10, 10));

        // Painel de Cadastro com borda preta
        JPanel panelCadastro = new JPanel(new GridBagLayout());
        panelCadastro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Cadastro de Usuário",
                TitledBorder.LEADING,
                TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // LOGIN
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCadastro.add(new JLabel("Login:"), gbc);

        tfLogin = new JTextField(20);
        gbc.gridx = 1;
        panelCadastro.add(tfLogin, gbc);

        // SENHA
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCadastro.add(new JLabel("Senha:"), gbc);

        pfSenha = new JPasswordField(20);
        gbc.gridx = 1;
        panelCadastro.add(pfSenha, gbc);

        // TIPO
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCadastro.add(new JLabel("Tipo:"), gbc);

        cbTipo = new JComboBox<>(new String[]{"ADMIN", "FONOAUDIOLOGO", "SECRETARIA", "FINANCEIRO"});
        gbc.gridx = 1;
        panelCadastro.add(cbTipo, gbc);

        // BOTOES
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSalvar = new JButton("Salvar");
        btnLimpar = new JButton("Limpar");
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panelCadastro.add(panelBotoes, gbc);

        // ==== TABELA DE USUÁRIOS ====
        String[] colunas = {"Login", "Tipo", "Status", "Criado em"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabela somente leitura
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setFillsViewportHeight(true);
        tabelaUsuarios.setPreferredScrollableViewportSize(new Dimension(500, 200));

        JScrollPane scrollTabela = new JScrollPane(tabelaUsuarios);

        // Adiciona os dois painéis ao layout principal
        add(panelCadastro, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);

        // Ações dos botões
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
    }

    private void limparCampos() {
        tfLogin.setText("");
        pfSenha.setText("");
        cbTipo.setSelectedIndex(0);
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

            modeloTabela.setRowCount(0); // limpa tabela
            for (Usuario u : usuarios) {
                modeloTabela.addRow(new Object[]{
                        u.getLogin(),
                        u.getTipo(),
                        u.isAtivo() ? "Ativo" : "Inativo",
                        u.getCriadoEm()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
