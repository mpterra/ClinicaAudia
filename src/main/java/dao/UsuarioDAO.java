package dao;

import model.Usuario;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

	// ============================
	// CREATE
	// ============================
	public Usuario salvar(Usuario usuario) throws SQLException {
		String sql = "INSERT INTO usuario (login, senha, tipo) VALUES (?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, usuario.getLogin());
			stmt.setString(2, usuario.getSenha());
			stmt.setString(3, usuario.getTipo());

			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Falha ao inserir Usuario, nenhuma linha afetada.");
			}

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					usuario.setId(generatedKeys.getInt(1));
				}
			}
		}
		return usuario;
	}

	// ============================
	// READ
	// ============================

	public Usuario buscarPorLogin(String login) throws SQLException {
		String sql = "SELECT * FROM usuario WHERE login = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, login);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		}
		return null;
	}

	public List<Usuario> listarTodos() throws SQLException {
		List<Usuario> lista = new ArrayList<>();
		String sql = "SELECT * FROM usuario ORDER BY login";
		try (Connection conn = Database.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				lista.add(mapRow(rs));
			}
		}
		return lista;
	}

	// ============================
	// UPDATE
	// ============================
	public boolean atualizar(Usuario usuario) throws SQLException {
		String sql = "UPDATE usuario SET login = ?, senha = ?, tipo = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, usuario.getLogin());
			stmt.setString(2, usuario.getSenha());
			stmt.setString(3, usuario.getTipo());
			stmt.setInt(4, usuario.getId());

			return stmt.executeUpdate() > 0;
		}
	}

	// ============================
	// DELETE
	// ============================
	public boolean deletar(int id) throws SQLException {
		String sql = "DELETE FROM usuario WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		}
	}

	// ============================
	// MAP ROW
	// ============================
	private Usuario mapRow(ResultSet rs) throws SQLException {
		Usuario u = new Usuario();
		u.setId(rs.getInt("id"));
		u.setLogin(rs.getString("login"));
		u.setSenha(rs.getString("senha"));
		u.setTipo(rs.getString("tipo"));

		return u;
	}
}
