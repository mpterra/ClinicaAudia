package dao;

import model.Estoque;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstoqueDAO {

    // ============================
    // CREATE ou UPDATE (UPSERT)
    // ============================
    public boolean salvarOuAtualizar(Estoque estoque, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO estoque (produto_id, quantidade, estoque_minimo, usuario) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantidade = ?, estoque_minimo = ?, usuario = ?, atualizado_em = CURRENT_TIMESTAMP";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estoque.getProdutoId());
            stmt.setInt(2, estoque.getQuantidade());
            stmt.setInt(3, estoque.getEstoqueMinimo());
            stmt.setString(4, usuarioLogado);

            stmt.setInt(5, estoque.getQuantidade());
            stmt.setInt(6, estoque.getEstoqueMinimo());
            stmt.setString(7, usuarioLogado);

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // READ
    // ============================
    public Estoque buscarPorProdutoId(int produtoId) throws SQLException {
        String sql = "SELECT * FROM estoque WHERE produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Estoque> listarTodos() throws SQLException {
        List<Estoque> lista = new ArrayList<>();
        String sql = "SELECT * FROM estoque ORDER BY produto_id";
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
    public boolean deletar(int produtoId) throws SQLException {
        String sql = "DELETE FROM estoque WHERE produto_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private Estoque mapRow(ResultSet rs) throws SQLException {
        Estoque e = new Estoque();
        e.setProdutoId(rs.getInt("produto_id"));
        e.setQuantidade(rs.getInt("quantidade"));
        e.setEstoqueMinimo(rs.getInt("estoque_minimo"));
        e.setCriadoEm(rs.getTimestamp("criado_em"));
        e.setAtualizadoEm(rs.getTimestamp("atualizado_em"));
        e.setUsuario(rs.getString("usuario"));
        return e;
    }
}
