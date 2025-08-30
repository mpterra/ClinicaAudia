package dao;

import model.Produto;
import model.TipoProduto;
import util.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

	// -------------------------
	// Inserir Produto
	// -------------------------
	public void inserir(Produto produto) throws SQLException {
		String sql = "INSERT INTO produto (tipo_produto_id, nome, codigo_serial, descricao, preco, estoque, usuario) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setInt(1, produto.getTipoProduto().getId());
			stmt.setString(2, produto.getNome());
			stmt.setString(3, produto.getCodigoSerial());
			stmt.setString(4, produto.getDescricao());
			stmt.setBigDecimal(5, produto.getPreco());
			stmt.setInt(6, produto.getEstoque());
			stmt.setString(7, produto.getUsuario());

			stmt.executeUpdate();

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					produto.setId(rs.getInt(1));
				}
			}
		}
	}

	// -------------------------
	// Atualizar Produto
	// -------------------------
	public void atualizar(Produto produto) throws SQLException {
		String sql = "UPDATE produto SET tipo_produto_id=?, nome=?, codigo_serial=?, descricao=?, preco=?, estoque=?, usuario=?, atualizado_em=CURRENT_TIMESTAMP "
				+ "WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, produto.getTipoProduto().getId());
			stmt.setString(2, produto.getNome());
			stmt.setString(3, produto.getCodigoSerial());
			stmt.setString(4, produto.getDescricao());
			stmt.setBigDecimal(5, produto.getPreco());
			stmt.setInt(6, produto.getEstoque());
			stmt.setString(7, produto.getUsuario());
			stmt.setInt(8, produto.getId());

			stmt.executeUpdate();
		}
	}

	// -------------------------
	// Deletar Produto
	// -------------------------
	public void deletar(int id) throws SQLException {
		String sql = "DELETE FROM produto WHERE id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
	}

	// -------------------------
	// Buscar por ID
	// -------------------------
	public Produto buscarPorId(int id) throws SQLException {
		String sql = "SELECT * FROM produto p " + "JOIN tipo_produto t ON p.tipo_produto_id = t.id " + "WHERE p.id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProduto(rs);
				}
			}
		}
		return null;
	}

	// -------------------------
	// Listar todos os produtos
	// -------------------------
	public List<Produto> listarTodos() throws SQLException {
		List<Produto> lista = new ArrayList<>();
		String sql = "SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id";
		try (Connection conn = Database.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				lista.add(mapearProduto(rs));
			}
		}
		return lista;
	}

	// -------------------------
	// Buscar por nome (com LIKE)
	// -------------------------
	public List<Produto> buscarPorNome(String nome) throws SQLException {
		List<Produto> lista = new ArrayList<>();
		String sql = "SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id WHERE p.nome LIKE ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, "%" + nome + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProduto(rs));
				}
			}
		}
		return lista;
	}

	// -------------------------
	// Buscar por código serial
	// -------------------------
	public Produto buscarPorCodigoSerial(String codigo) throws SQLException {
		String sql = "SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id WHERE p.codigo_serial=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, codigo);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProduto(rs);
				}
			}
		}
		return null;
	}

	// -------------------------
	// Buscar por tipo de produto
	// -------------------------
	public List<Produto> buscarPorTipo(int tipoProdutoId) throws SQLException {
		List<Produto> lista = new ArrayList<>();
		String sql = "SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id WHERE p.tipo_produto_id=?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, tipoProdutoId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProduto(rs));
				}
			}
		}
		return lista;
	}

	// -------------------------
	// Buscar por faixa de preço
	// -------------------------
	public List<Produto> buscarPorFaixaPreco(BigDecimal precoMin, BigDecimal precoMax) throws SQLException {
		List<Produto> lista = new ArrayList<>();
		String sql = "SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id WHERE p.preco BETWEEN ? AND ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBigDecimal(1, precoMin);
			stmt.setBigDecimal(2, precoMax);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProduto(rs));
				}
			}
		}
		return lista;
	}

	// Busca avançada com query dinâmica
	public List<Produto> buscarProdutosAvancado(String nome, Integer tipoProdutoId, BigDecimal precoMin,
			BigDecimal precoMax, Integer estoqueMin, Integer estoqueMax) throws SQLException {
		List<Produto> lista = new ArrayList<>();
		StringBuilder sql = new StringBuilder(
				"SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id WHERE 1=1");

	// Montando query dinamicamente
		if (nome != null && !nome.isEmpty()) {
			sql.append(" AND p.nome LIKE ?");
		}
		if (tipoProdutoId != null) {
			sql.append(" AND p.tipo_produto_id = ?");
		}
		if (precoMin != null) {
			sql.append(" AND p.preco >= ?");
		}
		if (precoMax != null) {
			sql.append(" AND p.preco <= ?");
		}
		if (estoqueMin != null) {
			sql.append(" AND p.estoque >= ?");
		}
		if (estoqueMax != null) {
			sql.append(" AND p.estoque <= ?");
		}

		try (Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			int index = 1;
			if (nome != null && !nome.isEmpty())
				stmt.setString(index++, "%" + nome + "%");
			if (tipoProdutoId != null)
				stmt.setInt(index++, tipoProdutoId);
			if (precoMin != null)
				stmt.setBigDecimal(index++, precoMin);
			if (precoMax != null)
				stmt.setBigDecimal(index++, precoMax);
			if (estoqueMin != null)
				stmt.setInt(index++, estoqueMin);
			if (estoqueMax != null)
				stmt.setInt(index++, estoqueMax);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProduto(rs));
				}
			}
		}
		return lista;
	}

	// -------------------------
	// Buscar produtos com estoque maior que X
	// -------------------------
	public List<Produto> buscarPorEstoqueMinimo(int minimo) throws SQLException {
		List<Produto> lista = new ArrayList<>();
		String sql = "SELECT * FROM produto p JOIN tipo_produto t ON p.tipo_produto_id = t.id WHERE p.estoque >= ?";
		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, minimo);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProduto(rs));
				}
			}
		}
		return lista;
	}

	// -------------------------
	// Mapear ResultSet para Produto
	// -------------------------
	private Produto mapearProduto(ResultSet rs) throws SQLException {
		TipoProduto tipo = new TipoProduto();
		tipo.setId(rs.getInt("tipo_produto_id"));
		tipo.setNome(rs.getString("t.nome"));

		Produto produto = new Produto();
		produto.setId(rs.getInt("id"));
		produto.setTipoProduto(tipo);
		produto.setNome(rs.getString("nome"));
		produto.setCodigoSerial(rs.getString("codigo_serial"));
		produto.setDescricao(rs.getString("descricao"));
		produto.setPreco(rs.getBigDecimal("preco"));
		produto.setEstoque(rs.getInt("estoque"));
		produto.setUsuario(rs.getString("usuario"));
		produto.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
		produto.setAtualizadoEm(
				rs.getTimestamp("atualizado_em") != null ? rs.getTimestamp("atualizado_em").toLocalDateTime() : null);

		return produto;
	}
}
