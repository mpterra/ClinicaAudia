package dao;

import model.Endereco;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnderecoDAO {

	// Inserir novo endereço
	public void insert(Endereco endereco) throws SQLException {
		String sql = "INSERT INTO endereco (cep, numero, rua, complemento, bairro, cidade, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, endereco.getCep());
			stmt.setString(2, endereco.getNumero());
			stmt.setString(3, endereco.getRua());
			stmt.setString(4, endereco.getComplemento());
			stmt.setString(5, endereco.getBairro());
			stmt.setString(6, endereco.getCidade());
			stmt.setString(7, endereco.getEstado());
			stmt.executeUpdate();

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					endereco.setId(rs.getInt(1));
				}
			}
		}
	}

	// Atualizar endereço
	public void update(Endereco endereco) throws SQLException {
		String sql = "UPDATE endereco SET cep=?, numero=?, rua=?, complemento=?, bairro=?, cidade=?, estado=? WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, endereco.getCep());
			stmt.setString(2, endereco.getNumero());
			stmt.setString(3, endereco.getRua());
			stmt.setString(4, endereco.getComplemento());
			stmt.setString(5, endereco.getBairro());
			stmt.setString(6, endereco.getCidade());
			stmt.setString(7, endereco.getEstado());
			stmt.setInt(8, endereco.getId());
			stmt.executeUpdate();
		}
	}

	// Deletar endereço
	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM endereco WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}

	// Buscar por ID
	public Endereco findById(int id) throws SQLException {
		String sql = "SELECT * FROM endereco WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSet(rs);
				}
			}
		}
		return null;
	}

	// Buscar por CEP
	public List<Endereco> findByCep(String cep) throws SQLException {
		return findByColumn("cep", cep);
	}

	// Buscar por cidade (com LIKE)
	public List<Endereco> findByCidade(String cidade) throws SQLException {
		return findByColumnLike("cidade", cidade);
	}

	// Buscar por bairro (com LIKE)
	public List<Endereco> findByBairro(String bairro) throws SQLException {
		return findByColumnLike("bairro", bairro);
	}

	// Métodos auxiliares
	private List<Endereco> findByColumn(String column, String value) throws SQLException {
		String sql = "SELECT * FROM endereco WHERE " + column + " = ?";
		List<Endereco> enderecos = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, value);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					enderecos.add(mapResultSet(rs));
				}
			}
		}
		return enderecos;
	}

	private List<Endereco> findByColumnLike(String column, String value) throws SQLException {
		String sql = "SELECT * FROM endereco WHERE " + column + " LIKE ?";
		List<Endereco> enderecos = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, "%" + value + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					enderecos.add(mapResultSet(rs));
				}
			}
		}
		return enderecos;
	}

	// Mapear ResultSet para Endereco
	private Endereco mapResultSet(ResultSet rs) throws SQLException {
		return new Endereco(rs.getInt("id"), rs.getString("cep"), rs.getString("numero"), rs.getString("rua"),
				rs.getString("complemento"), rs.getString("bairro"), rs.getString("cidade"), rs.getString("estado"));
	}
}
