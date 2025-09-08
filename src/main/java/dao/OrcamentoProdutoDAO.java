package dao;

import model.OrcamentoProduto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoProdutoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(OrcamentoProduto op) throws SQLException {
        String sql = "INSERT INTO orcamento_produto (orcamento_id, produto_id, quantidade, preco_unitario, data_registro) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, op.getOrcamentoId());
            stmt.setInt(2, op.getProdutoId());
            stmt.setInt(3, op.getQuantidade());
            stmt.setBigDecimal(4, op.getPrecoUnitario());
            stmt.setTimestamp(5, op.getDataRegistro());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // READ
    // ============================
    public List<OrcamentoProduto> listarPorOrcamento(int orcamentoId) throws SQLException {
        List<OrcamentoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM orcamento_produto WHERE orcamento_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orcamentoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    public List<OrcamentoProduto> listarPorProduto(int produtoId) throws SQLException {
        List<OrcamentoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM orcamento_produto WHERE produto_id = ?";
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
    public boolean deletar(int orcamentoId, int produtoId) throws SQLException {
        String sql = "DELETE FROM orcamento_produto WHERE orcamento_id = ? AND produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orcamentoId);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private OrcamentoProduto mapRow(ResultSet rs) throws SQLException {
        OrcamentoProduto op = new OrcamentoProduto();
        op.setOrcamentoId(rs.getInt("orcamento_id"));
        op.setProdutoId(rs.getInt("produto_id"));
        op.setQuantidade(rs.getInt("quantidade"));
        op.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        op.setDataRegistro(rs.getTimestamp("data_registro"));
        return op;
    }
}
