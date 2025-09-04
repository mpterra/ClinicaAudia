package dao;

import model.Paciente;
import util.Database;
import model.Endereco;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

	// -----------------------------
	// Inserir paciente
	// -----------------------------
	public void insert(Paciente paciente) throws SQLException {
		String sql = "INSERT INTO paciente (nome, sexo, cpf, telefone, email, data_nascimento, id_endereco, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, paciente.getNome());
			stmt.setString(2, paciente.getSexo());
			stmt.setString(3, paciente.getCpf());
			stmt.setString(4, paciente.getTelefone());
			stmt.setString(5, paciente.getEmail());
			stmt.setDate(6, Date.valueOf(paciente.getDataNascimento()));
			stmt.setInt(7, paciente.getEndereco() != null ? paciente.getEndereco().getId() : 0);
			stmt.setString(8, paciente.getUsuario());

			stmt.executeUpdate();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					paciente.setId(rs.getInt(1));
				}
			}
		}
	}

	// -----------------------------
	// Atualizar paciente
	// -----------------------------
	public void update(Paciente paciente) throws SQLException {
		String sql = "UPDATE paciente SET nome=?, sexo=?, cpf=?, telefone=?, email=?, data_nascimento=?, id_endereco=?, usuario=?, atualizado_em=NOW() WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, paciente.getNome());
			stmt.setString(2, paciente.getSexo());
			stmt.setString(3, paciente.getCpf());
			stmt.setString(4, paciente.getTelefone());
			stmt.setString(5, paciente.getEmail());
			stmt.setDate(6, Date.valueOf(paciente.getDataNascimento()));
			stmt.setInt(7, paciente.getEndereco() != null ? paciente.getEndereco().getId() : 0);
			stmt.setString(8, paciente.getUsuario());
			stmt.setInt(9, paciente.getId());

			stmt.executeUpdate();
		}
	}

	// -----------------------------
	// Deletar paciente
	// -----------------------------
	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM paciente WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}

	// -----------------------------
	// Buscar por ID
	// -----------------------------
	public Paciente findById(int id) throws SQLException {
		String sql = "SELECT * FROM paciente WHERE id=?";
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

	// -----------------------------
	// Buscar por nome (LIKE)
	// -----------------------------
	public List<Paciente> findByNome(String nome) throws SQLException {
		String sql = "SELECT * FROM paciente WHERE nome LIKE ? ORDER BY nome";
		List<Paciente> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, "%" + nome + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// -----------------------------
	// Buscar por CPF
	// -----------------------------
	public Paciente findByCPF(String cpf) throws SQLException {
		String sql = "SELECT * FROM paciente WHERE cpf=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, cpf);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSet(rs);
				}
			}
		}
		return null;
	}

	// -----------------------------
	// Buscar por telefone parcial
	// -----------------------------
	public List<Paciente> findByTelefoneParcial(String telefoneParcial) throws SQLException {
		String sql = "SELECT * FROM paciente WHERE telefone LIKE ? ORDER BY nome";
		List<Paciente> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, "%" + telefoneParcial + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// -----------------------------
	// Buscar por cidade
	// -----------------------------
	public List<Paciente> findByCidade(String cidade) throws SQLException {
		String sql = "SELECT p.* FROM paciente p " + "JOIN endereco e ON p.id_endereco = e.id "
				+ "WHERE e.cidade LIKE ? ORDER BY p.nome";
		List<Paciente> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, "%" + cidade + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// -----------------------------
	// Buscar por faixa de idade
	// -----------------------------
	public List<Paciente> findByFaixaIdade(int idadeMin, int idadeMax) throws SQLException {
		LocalDate hoje = LocalDate.now();
		LocalDate dataMax = hoje.minusYears(idadeMin);
		LocalDate dataMin = hoje.minusYears(idadeMax);

		String sql = "SELECT * FROM paciente WHERE data_nascimento BETWEEN ? AND ? ORDER BY nome";
		List<Paciente> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, Date.valueOf(dataMin));
			stmt.setDate(2, Date.valueOf(dataMax));

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// -----------------------------
	// Listar todos os pacientes
	// -----------------------------
	public List<Paciente> findAll() throws SQLException {
		String sql = "SELECT * FROM paciente ORDER BY nome";
		List<Paciente> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				lista.add(mapResultSet(rs));
			}
		}
		return lista;
	}

	// -----------------------------
	// Mapear ResultSet para Paciente
	// -----------------------------
	private Paciente mapResultSet(ResultSet rs) throws SQLException {
		int enderecoId = rs.getInt("id_endereco");
		Endereco endereco = null;
		if (enderecoId > 0) {
			// Se precisar dos dados completos, usar EnderecoDAO
			endereco = new Endereco();
			endereco.setId(enderecoId);
		}

		Paciente paciente = new Paciente(rs.getInt("id"), rs.getString("nome"), rs.getString("sexo"),
				rs.getString("cpf"), rs.getString("telefone"), rs.getString("email"),
				rs.getDate("data_nascimento").toLocalDate(), endereco, rs.getString("usuario"));

		return paciente;
	}
}
