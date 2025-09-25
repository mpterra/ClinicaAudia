package dao;

import model.VendaProduto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaProdutoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(VendaProduto vp) throws SQLException {
        String sql = "INSERT INTO venda_produto (venda_id, produto_id, quantidade, preco_unitario, data_venda, garantia_meses, fim_garantia, codigo_serial) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vp.getVendaId());
            stmt.setInt(2, vp.getProdutoId());
            stmt.setInt(3, vp.getQuantidade());
            stmt.setBigDecimal(4, vp.getPrecoUnitario());
            stmt.setTimestamp(5, vp.getDataVenda());
            stmt.setInt(6, vp.getGarantiaMeses());
            stmt.setDate(7, vp.getFimGarantia());
            stmt.setString(8, vp.getCogidoSerial());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // READ
    // ============================
    public List<VendaProduto> listarPorVenda(int vendaId) throws SQLException {
        List<VendaProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM venda_produto WHERE venda_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    public List<VendaProduto> listarPorProduto(int produtoId) throws SQLException {
        List<VendaProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM venda_produto WHERE produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
    // DELETE
    // ============================
    public boolean deletar(int vendaId, int produtoId) throws SQLException {
        String sql = "DELETE FROM venda_produto WHERE venda_id = ? AND produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
        vp.setVendaId(rs.getInt("venda_id"));
        vp.setProdutoId(rs.getInt("produto_id"));
        vp.setQuantidade(rs.getInt("quantidade"));
        vp.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        vp.setDataVenda(rs.getTimestamp("data_venda"));
        vp.setGarantiaMeses(rs.getInt("garantia_meses"));
        vp.setFimGarantia(rs.getDate("fim_garantia"));
        vp.setCogidoSerial(rs.getString("codigo_serial"));
        return vp;
    }

	public boolean serialExiste(String codigoSerial) {
		String sql = "SELECT COUNT(*) FROM venda_produto WHERE codigo_serial = ?";
		try (Connection conn = Database.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, codigoSerial);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
