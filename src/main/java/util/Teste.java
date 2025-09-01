package util;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import controller.UsuarioController;
import exception.LoginDuplicadoException;
import model.Usuario;

public class Teste {

    public static void main(String[] args) {
        UsuarioController uc = new UsuarioController();

        // Testar cadastro
        Usuario user = new Usuario();
        user.setLogin("admin");
        user.setSenha("cba321");
        user.setTipo("ADMIN");

        try {
            Usuario salvo = uc.salvar(user);
            JOptionPane.showMessageDialog(null, "Usuário cadastrado!");
            System.out.println("Usuário cadastrado: " + salvo);

        } catch (LoginDuplicadoException e) {
        	JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            System.out.println("Erro: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Testar login
        try {
            boolean conectou = uc.login("admin", "cba321");
            System.out.println("Conectou? " + conectou);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
