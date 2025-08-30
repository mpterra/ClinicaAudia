package dao;

import model.Venda;
import util.Database;
import model.Atendimento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

	// ============================
	// CREATE
	// ============================
	public Venda salvar(Venda venda) throws SQLException {
		String sql = "INSERT INTO venda (atendimento_id, valor_total, usuario) VALUES (?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			if (venda.getAtendimento() != null) {
				stmt.setInt(1, venda.getAtendimento().getId());
			} else {
				stmt.setNull(1, Types.INTEGER);
			}

			stmt.setBigDecimal(2, venda.getValorTotal());
			stmt.setString(3, venda.getUsuario());

			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Falha ao inserir Venda, nenhuma linha afetada.");
			}

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					venda.setId(generatedKeys.getInt(1));
				}
			}
		}
		return venda;
	}

	// ============================
	// READ
	// ============================
	public Venda buscarPorId(int id) throws SQLException {
		String sql = "SELECT * FROM venda WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		}
		return null;
	}

	public List<Venda> listarTodos() throws SQLException {
		List<Venda> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda ORDER BY data_hora DESC";
		try (Connection conn = Database.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				lista.add(mapRow(rs));
			}
		}
		return lista;
	}

	public List<Venda> buscarPorUsuario(String usuario) throws SQLException {
		List<Venda> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda WHERE usuario LIKE ? ORDER BY data_hora DESC";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, "%" + usuario + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		}
		return lista;
	}

	// ============================
	// UPDATE
	// ============================
	public boolean atualizar(Venda venda) throws SQLException {
		String sql = "UPDATE venda SET atendimento_id = ?, valor_total = ?, usuario = ?, data_hora = CURRENT_TIMESTAMP WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			if (venda.getAtendimento() != null) {
				stmt.setInt(1, venda.getAtendimento().getId());
			} else {
				stmt.setNull(1, Types.INTEGER);
			}

			stmt.setBigDecimal(2, venda.getValorTotal());
			stmt.setString(3, venda.getUsuario());
			stmt.setInt(4, venda.getId());

			return stmt.executeUpdate() > 0;
		}
	}

	// ============================
	// DELETE
	// ============================
	public boolean deletar(int id) throws SQLException {
		String sql = "DELETE FROM venda WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		}
	}

	// Lista vendas de um paciente específico
	public List<Venda> listarPorPaciente(int pacienteId) throws SQLException {
		List<Venda> lista = new ArrayList<>();
		String sql = """
				SELECT v.*
				FROM venda v
				JOIN atendimento a ON v.atendimento_id = a.id
				WHERE a.paciente_id = ?
				ORDER BY v.data_hora DESC
				""";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, pacienteId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		}
		return lista;
	}

	// Lista vendas em um intervalo de datas
	public List<Venda> listarPorData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
		List<Venda> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda WHERE data_hora BETWEEN ? AND ? ORDER BY data_hora DESC";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setTimestamp(1, Timestamp.valueOf(inicio));
			stmt.setTimestamp(2, Timestamp.valueOf(fim));

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		}
		return lista;
	}

	// Lista vendas de um paciente em um intervalo de datas
	public List<Venda> listarPorPacienteEData(int pacienteId, LocalDateTime inicio, LocalDateTime fim)
			throws SQLException {
		List<Venda> lista = new ArrayList<>();
		String sql = """
				SELECT v.*
				FROM venda v
				JOIN atendimento a ON v.atendimento_id = a.id
				WHERE a.paciente_id = ? AND v.data_hora BETWEEN ? AND ?
				ORDER BY v.data_hora DESC
				""";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, pacienteId);
			stmt.setTimestamp(2, Timestamp.valueOf(inicio));
			stmt.setTimestamp(3, Timestamp.valueOf(fim));

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		}
		return lista;
	}

	// ============================
	// MAP ROW
	// ============================
	private Venda mapRow(ResultSet rs) throws SQLException {
		Venda v = new Venda();
		v.setId(rs.getInt("id"));

		int atendimentoId = rs.getInt("atendimento_id");
		if (!rs.wasNull()) {
			Atendimento a = new Atendimento();
			a.setId(atendimentoId);
			v.setAtendimento(a);
		}

		v.setValorTotal(rs.getBigDecimal("valor_total"));
		v.setUsuario(rs.getString("usuario"));
		v.setDataHora(rs.getTimestamp("data_hora") != null ? rs.getTimestamp("data_hora").toLocalDateTime() : null);

		return v;
	}
}
