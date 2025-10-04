package dao;

import model.VendaProduto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO para operações no banco de dados da tabela venda_produto
public class VendaProdutoDAO {

    // Salva um item de venda no banco
    public boolean salvar(VendaProduto vp) throws SQLException {
        String sql = "INSERT INTO venda_produto (venda_id, produto_id, quantidade, preco_unitario, desconto, data_venda, garantia_meses, fim_garantia, codigo_serial) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vp.getVendaId());
            stmt.setInt(2, vp.getProdutoId());
            stmt.setInt(3, vp.getQuantidade());
            stmt.setBigDecimal(4, vp.getPrecoUnitario());
            stmt.setBigDecimal(5, vp.getDesconto());
            stmt.setTimestamp(6, vp.getDataVenda());
            stmt.setInt(7, vp.getGarantiaMeses());
            stmt.setDate(8, vp.getFimGarantia());
            stmt.setString(9, vp.getCogidoSerial());
            return stmt.executeUpdate() > 0;
        }
    }

    // Lista todos os itens de uma venda específica
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

    // Lista todos os itens associados a um produto
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

    // Remove um item de venda com base na venda e produto
    public boolean deletar(int vendaId, int produtoId) throws SQLException {
        String sql = "DELETE FROM venda_produto WHERE venda_id = ? AND produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendaId);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Verifica se um código serial já existe no banco
    public boolean serialExiste(String codigoSerial) throws SQLException {
        String sql = "SELECT COUNT(*) FROM venda_produto WHERE codigo_serial = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigoSerial);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Mapeia um ResultSet para um objeto VendaProduto
    private VendaProduto mapRow(ResultSet rs) throws SQLException {
        VendaProduto vp = new VendaProduto();
        vp.setVendaId(rs.getInt("venda_id"));
        vp.setProdutoId(rs.getInt("produto_id"));
        vp.setQuantidade(rs.getInt("quantidade"));
        vp.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        vp.setDesconto(rs.getBigDecimal("desconto"));
        vp.setDataVenda(rs.getTimestamp("data_venda"));
        vp.setGarantiaMeses(rs.getInt("garantia_meses"));
        vp.setFimGarantia(rs.getDate("fim_garantia"));
        vp.setCogidoSerial(rs.getString("codigo_serial"));
        return vp;
    }
}