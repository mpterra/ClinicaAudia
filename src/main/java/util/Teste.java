package util;

import java.sql.SQLException;

import controller.UsuarioController;
import model.Usuario;

public class Teste {

	public static void main(String[] args) throws SQLException {
		
//		Usuario user = new Usuario();
//		user.setLogin("admin");
//		user.setSenha("cba321");
//		user.setTipo("ADMIN");
//		
//		System.out.println(user);
//		
		UsuarioController uc = new UsuarioController();
//		uc.salvar(user);
		
		boolean conectou = uc.login("admin", "cba321");
		System.out.println("Conectou? " + conectou);

	}

}
