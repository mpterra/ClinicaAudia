package dao;

import model.VendaProduto;
import util.Database;
import model.Venda;
import model.Produto;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaProdutoDAO {

	// ============================
	// CREATE
	// ============================
	public VendaProduto salvar(VendaProduto vp) throws SQLException {
		String sql = "INSERT INTO venda_produto (venda_id, produto_id, quantidade, preco_unitario, data_venda, garantia_meses, fim_garantia) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, vp.getVenda().getId());
			stmt.setInt(2, vp.getProduto().getId());
			stmt.setInt(3, vp.getQuantidade());
			stmt.setBigDecimal(4, vp.getPrecoUnitario());
			stmt.setTimestamp(5, Timestamp.valueOf(vp.getDataVenda()));
			stmt.setInt(6, vp.getGarantiaMeses());
			stmt.setDate(7, vp.getFimGarantia() != null ? Date.valueOf(vp.getFimGarantia()) : null);
			stmt.executeUpdate();
		}
		return vp;
	}

	// ============================
	// LISTAR TODOS
	// ============================
	public List<VendaProduto> listarTodos() throws SQLException {
		List<VendaProduto> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda_produto";
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
	// LISTAR POR VENDA
	// ============================
	public List<VendaProduto> listarPorVenda(int vendaId) throws SQLException {
		List<VendaProduto> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda_produto WHERE venda_id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, vendaId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		}
		return lista;
	}

	// ============================
	// LISTAR POR PRODUTO
	// ============================
	public List<VendaProduto> listarPorProduto(int produtoId) throws SQLException {
		List<VendaProduto> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda_produto WHERE produto_id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, produtoId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		}
		return lista;
	}

	// ============================
	// LISTAR POR DATA
	// ============================
	public List<VendaProduto> listarPorData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
		List<VendaProduto> lista = new ArrayList<>();
		String sql = "SELECT * FROM venda_produto WHERE data_venda BETWEEN ? AND ?";
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

	// ============================
	// LISTAR POR PACIENTE/CLIENTE
	// ============================
	public List<VendaProduto> listarPorPaciente(int pacienteId) throws SQLException {
		List<VendaProduto> lista = new ArrayList<>();
		String sql = """
				SELECT vp.* FROM venda_produto vp
				INNER JOIN venda v ON vp.venda_id = v.id
				INNER JOIN atendimento a ON v.atendimento_id = a.id
				WHERE a.paciente_id = ?
				ORDER BY vp.data_venda
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

	// ============================
	// LISTAR POR PACIENTE E INTERVALO DE DATAS
	// ============================
	public List<VendaProduto> listarPorPacienteEData(int pacienteId, LocalDateTime inicio, LocalDateTime fim)
			throws SQLException {
		List<VendaProduto> lista = new ArrayList<>();
		String sql = """
				SELECT vp.* FROM venda_produto vp
				INNER JOIN venda v ON vp.venda_id = v.id
				INNER JOIN atendimento a ON v.atendimento_id = a.id
				WHERE a.paciente_id = ? AND vp.data_venda BETWEEN ? AND ?
				ORDER BY vp.data_venda
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
	// TOTAL VENDIDO POR PACIENTE
	// ============================
	public BigDecimal totalVendidoPorPaciente(int pacienteId) throws SQLException {
		String sql = """
				    SELECT SUM(vp.preco_unitario * vp.quantidade) AS total
				    FROM venda_produto vp
				    INNER JOIN venda v ON vp.venda_id = v.id
				    INNER JOIN atendimento a ON v.atendimento_id = a.id
				    WHERE a.paciente_id = ?
				""";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, pacienteId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
				}
			}
		}
		return BigDecimal.ZERO;
	}

	// ============================
	// TOTAL VENDIDO POR PRODUTO
	// ============================
	public BigDecimal totalVendidoPorProduto(int produtoId) throws SQLException {
		String sql = "SELECT SUM(preco_unitario * quantidade) AS total FROM venda_produto WHERE produto_id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, produtoId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
				}
			}
		}
		return BigDecimal.ZERO;
	}

	// ============================
	// QUANTIDADE VENDIDA POR PRODUTO
	// ============================
	public int quantidadeVendidaPorProduto(int produtoId) throws SQLException {
		String sql = "SELECT SUM(quantidade) AS totalQtd FROM venda_produto WHERE produto_id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, produtoId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("totalQtd");
				}
			}
		}
		return 0;
	}

	// ============================
	// TOTAL VENDIDO POR INTERVALO DE DATAS
	// ============================
	public BigDecimal totalVendidoPorData(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
		String sql = "SELECT SUM(preco_unitario * quantidade) AS total FROM venda_produto WHERE data_venda BETWEEN ? AND ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(inicio));
			stmt.setTimestamp(2, Timestamp.valueOf(fim));
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
				}
			}
		}
		return BigDecimal.ZERO;
	}

	// ============================
	// TOTAL VENDIDO POR PACIENTE E DATA
	// ============================
	public BigDecimal totalVendidoPorPacienteEData(int pacienteId, LocalDateTime inicio, LocalDateTime fim)
			throws SQLException {
		String sql = """
				    SELECT SUM(vp.preco_unitario * vp.quantidade) AS total
				    FROM venda_produto vp
				    INNER JOIN venda v ON vp.venda_id = v.id
				    INNER JOIN atendimento a ON v.atendimento_id = a.id
				    WHERE a.paciente_id = ? AND vp.data_venda BETWEEN ? AND ?
				""";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, pacienteId);
			stmt.setTimestamp(2, Timestamp.valueOf(inicio));
			stmt.setTimestamp(3, Timestamp.valueOf(fim));
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
				}
			}
		}
		return BigDecimal.ZERO;
	}

	// ============================
	// DELETE
	// ============================
	public boolean deletar(int vendaId, int produtoId) throws SQLException {
		String sql = "DELETE FROM venda_produto WHERE venda_id = ? AND produto_id = ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, vendaId);
			stmt.setInt(2, produtoId);
			return stmt.executeUpdate() > 0;
		}
	}

	// ============================
	// MAP ROW
	// ============================
	private VendaProduto mapRow(ResultSet rs) throws SQLException {
		VendaProduto vp = new VendaProduto();
		vp.setVenda(new Venda());
		vp.getVenda().setId(rs.getInt("venda_id"));
		vp.setProduto(new Produto());
		vp.getProduto().setId(rs.getInt("produto_id"));
		vp.setQuantidade(rs.getInt("quantidade"));
		vp.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
		vp.setDataVenda(rs.getTimestamp("data_venda").toLocalDateTime());
		vp.setGarantiaMeses(rs.getInt("garantia_meses"));
		vp.setFimGarantia(rs.getDate("fim_garantia") != null ? rs.getDate("fim_garantia").toLocalDate() : null);
		return vp;
	}
}
