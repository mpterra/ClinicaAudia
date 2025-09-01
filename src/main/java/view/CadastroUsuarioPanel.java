package view;

import javax.swing.*;

import controller.UsuarioController;
import exception.LoginDuplicadoException;
import model.Usuario;

import java.awt.*;
import java.sql.SQLException;

public class CadastroUsuarioPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField tfLogin;
    private JPasswordField pfSenha;
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnLimpar;

    public CadastroUsuarioPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // LOGIN
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Login:"), gbc);

        tfLogin = new JTextField(20);
        gbc.gridx = 1;
        add(tfLogin, gbc);

        // SENHA
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Senha:"), gbc);

        pfSenha = new JPasswordField(20);
        gbc.gridx = 1;
        add(pfSenha, gbc);

        // TIPO
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Tipo:"), gbc);

        cbTipo = new JComboBox<>(new String[]{"ADMIN", "FONOAUDIOLOGO", "SECRETARIA", "FINANCEIRO"});
        gbc.gridx = 1;
        add(cbTipo, gbc);

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
        add(panelBotoes, gbc);

        // Ações dos botões
        btnLimpar.addActionListener(e -> limparCampos());
        btnSalvar.addActionListener(e -> {
			try {
				salvarUsuario();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "Erro ao salvar usuário: " + e1.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
			} catch (LoginDuplicadoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "Usuário já cadastrado. Utilize outro login", "Erro", JOptionPane.ERROR_MESSAGE);
			}
		});
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
        if(uc.salvar(user)) {
        	JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        	limparCampos();
        }
    }
}
