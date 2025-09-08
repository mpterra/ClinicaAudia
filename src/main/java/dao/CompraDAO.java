package dao;

import model.Compra;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Compra compra, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO compra (fornecedor, usuario) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, compra.getFornecedor());
            stmt.setString(2, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir Compra, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    compra.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public Compra buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM compra WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Compra> listarTodos() throws SQLException {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra ORDER BY data_compra DESC";
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
        String sql = "DELETE FROM compra WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private Compra mapRow(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setId(rs.getInt("id"));
        c.setFornecedor(rs.getString("fornecedor"));
        c.setDataCompra(rs.getTimestamp("data_compra"));
        c.setUsuario(rs.getString("usuario"));
        return c;
    }
}
