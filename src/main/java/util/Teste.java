package util;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import controller.UsuarioController;
import exception.LoginDuplicadoException;
import model.Usuario;

public class Teste {

    public static void main(String[] args) throws SQLException, LoginDuplicadoException {

        // Testar cadastro
        UsuarioController uc = new UsuarioController();
        Usuario user = new Usuario("admin", "54321");
        user.setTipo("ADMIN");
        
		boolean teste = uc.salvar(user);
        System.out.println(teste ? "Usuário salvo com sucesso!" : "Falha ao salvar usuário.");
    }
}
