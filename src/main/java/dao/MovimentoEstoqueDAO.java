package dao;

import model.MovimentoEstoque;
import model.Produto;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimentoEstoqueDAO {

	// Inserir novo movimento
	public void insert(MovimentoEstoque movimento) throws SQLException {
		String sql = "INSERT INTO movimento_estoque "
				+ "(produto_id, quantidade, tipo, observacoes, data_hora, usuario) " + "VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, movimento.getProduto().getId());
			stmt.setInt(2, movimento.getQuantidade());
			stmt.setString(3, movimento.getTipo().name());
			stmt.setString(4, movimento.getObservacoes());
			stmt.setTimestamp(5, Timestamp.valueOf(movimento.getDataHora()));
			stmt.setString(6, movimento.getUsuario());
			stmt.executeUpdate();

			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next()) {
					movimento.setId(keys.getInt(1));
				}
			}
		}
	}

	// Atualizar movimento
	public void update(MovimentoEstoque movimento) throws SQLException {
		String sql = "UPDATE movimento_estoque SET produto_id=?, quantidade=?, tipo=?, observacoes=?, data_hora=?, usuario=? WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, movimento.getProduto().getId());
			stmt.setInt(2, movimento.getQuantidade());
			stmt.setString(3, movimento.getTipo().name());
			stmt.setString(4, movimento.getObservacoes());
			stmt.setTimestamp(5, Timestamp.valueOf(movimento.getDataHora()));
			stmt.setString(6, movimento.getUsuario());
			stmt.setInt(7, movimento.getId());
			stmt.executeUpdate();
		}
	}

	// Deletar movimento
	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM movimento_estoque WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}

	// Buscar por ID
	public MovimentoEstoque findById(int id) throws SQLException {
		String sql = "SELECT * FROM movimento_estoque WHERE id=?";
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

	// Buscar todos os movimentos
	public List<MovimentoEstoque> findAll() throws SQLException {
		String sql = "SELECT * FROM movimento_estoque ORDER BY data_hora";
		List<MovimentoEstoque> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				lista.add(mapResultSet(rs));
			}
		}
		return lista;
	}

	// Buscar por produto
	public List<MovimentoEstoque> findByProduto(Produto produto) throws SQLException {
		String sql = "SELECT * FROM movimento_estoque WHERE produto_id=? ORDER BY data_hora";
		List<MovimentoEstoque> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, produto.getId());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// Buscar movimentos por faixa de datas
	public List<MovimentoEstoque> findByDataRange(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
		String sql = "SELECT * FROM movimento_estoque WHERE data_hora BETWEEN ? AND ? ORDER BY data_hora";
		List<MovimentoEstoque> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(inicio));
			stmt.setTimestamp(2, Timestamp.valueOf(fim));
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// Buscar movimentos por tipo (ENTRADA, SAIDA, AJUSTE)
	public List<MovimentoEstoque> findByTipo(MovimentoEstoque.TipoMovimento tipo) throws SQLException {
		String sql = "SELECT * FROM movimento_estoque WHERE tipo=? ORDER BY data_hora";
		List<MovimentoEstoque> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, tipo.name());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// Buscar movimentos por usuário
	public List<MovimentoEstoque> findByUsuario(String usuario) throws SQLException {
		String sql = "SELECT * FROM movimento_estoque WHERE usuario=? ORDER BY data_hora";
		List<MovimentoEstoque> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, usuario);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// Buscar por produto + faixa de datas
	public List<MovimentoEstoque> findByProdutoAndDataRange(Produto produto, LocalDateTime inicio, LocalDateTime fim)
			throws SQLException {
		String sql = "SELECT * FROM movimento_estoque WHERE produto_id=? AND data_hora BETWEEN ? AND ? ORDER BY data_hora";
		List<MovimentoEstoque> lista = new ArrayList<>();
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, produto.getId());
			stmt.setTimestamp(2, Timestamp.valueOf(inicio));
			stmt.setTimestamp(3, Timestamp.valueOf(fim));
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSet(rs));
				}
			}
		}
		return lista;
	}

	// Mapear ResultSet para objeto
	private MovimentoEstoque mapResultSet(ResultSet rs) throws SQLException {
		MovimentoEstoque m = new MovimentoEstoque();
		m.setId(rs.getInt("id"));
		Produto p = new Produto();
		p.setId(rs.getInt("produto_id"));
		m.setProduto(p);
		m.setQuantidade(rs.getInt("quantidade"));
		m.setTipo(rs.getString("tipo"));
		m.setObservacoes(rs.getString("observacoes"));
		Timestamp ts = rs.getTimestamp("data_hora");
		if (ts != null)
			m.setDataHora(ts.toLocalDateTime());
		m.setUsuario(rs.getString("usuario"));
		return m;
	}
}
