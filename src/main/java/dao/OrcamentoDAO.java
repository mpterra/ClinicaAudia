package dao;

import model.Orcamento;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoDAO {

	// ============================
	// CREATE
	// ============================
	public boolean salvar(Orcamento orcamento, String usuarioLogado) throws SQLException {
		String sql = "INSERT INTO orcamento (paciente_id, profissional_id, atendimento_id, valor_total, observacoes, usuario) VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			setNullableInt(stmt, 1, orcamento.getPacienteId());
			setNullableInt(stmt, 2, orcamento.getProfissionalId());
			setNullableInt(stmt, 3, orcamento.getAtendimentoId());

			stmt.setBigDecimal(4, orcamento.getValorTotal());
			stmt.setString(5, orcamento.getObservacoes());
			stmt.setString(6, usuarioLogado);

			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Falha ao inserir Orçamento, nenhuma linha afetada.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					orcamento.setId(rs.getInt(1));
				}
			}
		}

		return true;
	}

	// ============================
	// READ
	// ============================
	public Orcamento buscarPorId(int id) throws SQLException {
		String sql = "SELECT * FROM orcamento WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return mapRow(rs);
			}
		}
		return null;
	}

	public List<Orcamento> listarTodos() throws SQLException {
		List<Orcamento> lista = new ArrayList<>();
		String sql = "SELECT * FROM orcamento ORDER BY data_hora DESC";
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
	// DELETE
	// ============================
	public boolean deletar(int id) throws SQLException {
		String sql = "DELETE FROM orcamento WHERE id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		}
	}

	// ============================
	// MAP ROW
	// ============================
	private Orcamento mapRow(ResultSet rs) throws SQLException {
		Orcamento o = new Orcamento();
		o.setId(rs.getInt("id"));
		o.setPacienteId(getNullableInt(rs, "paciente_id"));
		o.setProfissionalId(getNullableInt(rs, "profissional_id"));
		o.setAtendimentoId(getNullableInt(rs, "atendimento_id"));
		o.setValorTotal(rs.getBigDecimal("valor_total"));
		o.setObservacoes(rs.getString("observacoes"));
		o.setDataHora(rs.getTimestamp("data_hora"));
		o.setUsuario(rs.getString("usuario"));
		return o;
	}

	// ============================
	// MÉTODOS AUXILIARES
	// ============================
	private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
		if (value != null) {
			stmt.setInt(index, value);
		} else {
			stmt.setNull(index, Types.INTEGER);
		}
	}

	private Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
		int value = rs.getInt(columnLabel);
		return rs.wasNull() ? null : value;
	}

	// ============================
	// UPDATE
	// ============================
	public boolean atualizar(Orcamento orcamento, String usuarioLogado) throws SQLException {
	    if (orcamento.getId() <= 0) {
	        throw new IllegalArgumentException("ID inválido para atualização.");
	    }

	    String sql = "UPDATE orcamento SET paciente_id = ?, profissional_id = ?, atendimento_id = ?, valor_total = ?, observacoes = ?, usuario = ? WHERE id = ?";

	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        setNullableInt(stmt, 1, orcamento.getPacienteId());
	        setNullableInt(stmt, 2, orcamento.getProfissionalId());
	        setNullableInt(stmt, 3, orcamento.getAtendimentoId());

	        stmt.setBigDecimal(4, orcamento.getValorTotal());
	        stmt.setString(5, orcamento.getObservacoes());
	        stmt.setString(6, usuarioLogado);
	        stmt.setInt(7, orcamento.getId());

	        return stmt.executeUpdate() > 0;
	    }
	}

}
