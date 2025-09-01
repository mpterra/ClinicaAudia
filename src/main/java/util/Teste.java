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
        Usuario user = new Usuario();
		user = uc.buscarPorLogin("admin");
        System.out.println(user);
        
        user.setSenha("54321");
        
        uc.atualizar(user);

    }
}
