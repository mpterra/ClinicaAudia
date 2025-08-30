package dao;

import model.EvolucaoAtendimento;
import util.Database;
import model.Atendimento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvolucaoAtendimentoDAO {

	// Inserir nova evolução
	public void insert(EvolucaoAtendimento evolucao) throws SQLException {
		String sql = "INSERT INTO evolucao_atendimento (atendimento_id, notas, arquivo, usuario) VALUES (?, ?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, evolucao.getAtendimento().getId());
			stmt.setString(2, evolucao.getNotas());
			stmt.setString(3, evolucao.getArquivo());
			stmt.setString(4, evolucao.getUsuario());
			stmt.executeUpdate();

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					evolucao.setId(rs.getInt(1));
				}
			}
		}
	}

	// Atualizar evolução
	public void update(EvolucaoAtendimento evolucao) throws SQLException {
		String sql = "UPDATE evolucao_atendimento SET notas=?, arquivo=? WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, evolucao.getNotas());
			stmt.setString(2, evolucao.getArquivo());
			stmt.setInt(3, evolucao.getId());
			stmt.executeUpdate();
		}
	}

	// Deletar evolução
	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM evolucao_atendimento WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}

	// Buscar por ID
	public EvolucaoAtendimento findById(int id) throws SQLException {
		String sql = "SELECT * FROM evolucao_atendimento WHERE id=?";
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

	// Buscar todas evoluções de um atendimento
	public List<EvolucaoAtendimento> findByAtendimento(Atendimento atendimento) throws SQLException {
		String sql = "SELECT * FROM evolucao_atendimento WHERE atendimento_id=? ORDER BY criado_em";
		List<EvolucaoAtendimento> evolucoes = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, atendimento.getId());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					evolucoes.add(mapResultSet(rs));
				}
			}
		}
		return evolucoes;
	}

	// Buscar evoluções por usuário
	public List<EvolucaoAtendimento> findByUsuario(String usuario) throws SQLException {
		String sql = "SELECT * FROM evolucao_atendimento WHERE usuario=? ORDER BY criado_em";
		List<EvolucaoAtendimento> evolucoes = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, usuario);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					evolucoes.add(mapResultSet(rs));
				}
			}
		}
		return evolucoes;
	}

	// Buscar evoluções em uma faixa de datas (criado_em)
	public List<EvolucaoAtendimento> findByDateRange(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
		String sql = "SELECT * FROM evolucao_atendimento WHERE criado_em BETWEEN ? AND ? ORDER BY criado_em";
		List<EvolucaoAtendimento> evolucoes = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(inicio));
			stmt.setTimestamp(2, Timestamp.valueOf(fim));
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					evolucoes.add(mapResultSet(rs));
				}
			}
		}
		return evolucoes;
	}

	// Mapear ResultSet para EvolucaoAtendimento
	private EvolucaoAtendimento mapResultSet(ResultSet rs) throws SQLException {
		EvolucaoAtendimento e = new EvolucaoAtendimento();
		e.setId(rs.getInt("id"));
		Atendimento at = new Atendimento();
		at.setId(rs.getInt("atendimento_id"));
		e.setAtendimento(at);
		e.setNotas(rs.getString("notas"));
		e.setArquivo(rs.getString("arquivo"));
		Timestamp criadoTs = rs.getTimestamp("criado_em");
		if (criadoTs != null)
			e.setCriadoEm(criadoTs.toLocalDateTime());
		e.setUsuario(rs.getString("usuario"));
		return e;
	}
}
