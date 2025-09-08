package dao;

import model.CompraProduto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraProdutoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(CompraProduto cp) throws SQLException {
        String sql = "INSERT INTO compra_produto (compra_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cp.getCompraId());
            stmt.setInt(2, cp.getProdutoId());
            stmt.setInt(3, cp.getQuantidade());
            stmt.setBigDecimal(4, cp.getPrecoUnitario());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // READ
    // ============================
    public List<CompraProduto> listarPorCompra(int compraId) throws SQLException {
        List<CompraProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra_produto WHERE compra_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, compraId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    public List<CompraProduto> listarPorProduto(int produtoId) throws SQLException {
        List<CompraProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra_produto WHERE produto_id = ?";
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
    public boolean deletar(int compraId, int produtoId) throws SQLException {
        String sql = "DELETE FROM compra_produto WHERE compra_id = ? AND produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, compraId);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private CompraProduto mapRow(ResultSet rs) throws SQLException {
        CompraProduto cp = new CompraProduto();
        cp.setCompraId(rs.getInt("compra_id"));
        cp.setProdutoId(rs.getInt("produto_id"));
        cp.setQuantidade(rs.getInt("quantidade"));
        cp.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        return cp;
    }
}
