package dao;

import model.Endereco;
import model.Profissional;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfissionalDAO {

	// -----------------------------
	// CRUD Básico
	// -----------------------------

	public void salvar(Profissional prof) throws SQLException {
		String sql = "INSERT INTO profissional (nome, sexo, cpf, email, telefone, tipo, endereco_id, ativo, usuario) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, prof.getNome());
			stmt.setString(2, prof.getSexo());
			stmt.setString(2, prof.getCpf());
			stmt.setString(3, prof.getEmail());
			stmt.setString(4, prof.getTelefone());
			stmt.setString(5, prof.getTipo().name());
			stmt.setInt(6, prof.getEndereco() != null ? prof.getEndereco().getId() : Types.NULL);
			stmt.setBoolean(7, prof.isAtivo());
			stmt.setString(8, prof.getUsuario());

			stmt.executeUpdate();

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					prof.setId(rs.getInt(1));
				}
			}
		}
	}

	public void atualizar(Profissional prof) throws SQLException {
		String sql = "UPDATE profissional SET nome=?, sexo=? cpf=?, email=?, telefone=?, tipo=?, endereco_id=?, ativo=?, usuario=?, atualizado_em=CURRENT_TIMESTAMP WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, prof.getNome());
			stmt.setString(2, prof.getSexo());
			stmt.setString(2, prof.getCpf());
			stmt.setString(3, prof.getEmail());
			stmt.setString(4, prof.getTelefone());
			stmt.setString(5, prof.getTipo().name());
			stmt.setInt(6, prof.getEndereco() != null ? prof.getEndereco().getId() : Types.NULL);
			stmt.setBoolean(7, prof.isAtivo());
			stmt.setString(8, prof.getUsuario());
			stmt.setInt(9, prof.getId());

			stmt.executeUpdate();
		}
	}

	public void deletar(int id) throws SQLException {
		String sql = "DELETE FROM profissional WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}

	public Profissional buscarPorId(int id) throws SQLException {
		String sql = "SELECT * FROM profissional WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProfissional(rs);
				}
			}
		}
		return null;
	}

	// -----------------------------
	// Buscas simples
	// -----------------------------

	public List<Profissional> buscarPorNome(String nome) throws SQLException {
		String sql = "SELECT * FROM profissional WHERE nome LIKE ?";
		List<Profissional> lista = new ArrayList<>();

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, "%" + nome + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProfissional(rs));
				}
			}
		}
		return lista;
	}

	public List<Profissional> buscarPorTipo(Profissional.TipoProfissional tipo) throws SQLException {
		String sql = "SELECT * FROM profissional WHERE tipo=?";
		List<Profissional> lista = new ArrayList<>();

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, tipo.name());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProfissional(rs));
				}
			}
		}
		return lista;
	}

	public List<Profissional> buscarPorAtivo(boolean ativo) throws SQLException {
		String sql = "SELECT * FROM profissional WHERE ativo=?";
		List<Profissional> lista = new ArrayList<>();

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setBoolean(1, ativo);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProfissional(rs));
				}
			}
		}
		return lista;
	}

	// -----------------------------
	// Busca avançada (filtros opcionais)
	// -----------------------------
	public List<Profissional> buscarAvancado(String nome, Profissional.TipoProfissional tipo, Boolean ativo)
			throws SQLException {
		List<Profissional> lista = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT * FROM profissional WHERE 1=1");

		if (nome != null && !nome.isEmpty())
			sql.append(" AND nome LIKE ?");
		if (tipo != null)
			sql.append(" AND tipo=?");
		if (ativo != null)
			sql.append(" AND ativo=?");

		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			int index = 1;
			if (nome != null && !nome.isEmpty())
				stmt.setString(index++, "%" + nome + "%");
			if (tipo != null)
				stmt.setString(index++, tipo.name());
			if (ativo != null)
				stmt.setBoolean(index++, ativo);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProfissional(rs));
				}
			}
		}
		return lista;
	}

	// -----------------------------
	// Método auxiliar para mapear ResultSet
	// -----------------------------
	private Profissional mapearProfissional(ResultSet rs) throws SQLException {
		Profissional p = new Profissional();
		p.setId(rs.getInt("id"));
		p.setNome(rs.getString("nome"));
		p.setSexo(rs.getString("sexo"));
		p.setCpf(rs.getString("cpf"));
		p.setEmail(rs.getString("email"));
		p.setTelefone(rs.getString("telefone"));

		String tipoStr = rs.getString("tipo");
		if (tipoStr != null)
			p.setTipo(Profissional.TipoProfissional.valueOf(tipoStr));

		int enderecoId = rs.getInt("endereco_id");
		if (!rs.wasNull()) {
			Endereco e = new Endereco();
			e.setId(enderecoId);
			p.setEndereco(e);
		}

		p.setAtivo(rs.getBoolean("ativo"));
		p.setUsuario(rs.getString("usuario"));

		return p;
	}
}
