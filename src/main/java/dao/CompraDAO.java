package dao;

import model.Compra;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    // ============================
    // CREATE
    // ============================
    // Salva uma nova compra no banco de dados
    public boolean salvar(Compra compra, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO compra (usuario, data_compra, cancelada) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuarioLogado);
            stmt.setTimestamp(2, compra.getDataCompra() != null ? compra.getDataCompra() : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(3, false); // Compra inicia como não cancelada
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir Compra, nenhuma linha afetada.");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    compra.setId(rs.getInt(1));
                }
            }
            return true;
        }
    }

    // ============================
    // READ
    // ============================
    // Busca uma compra por ID
    public Compra buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM compra WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Lista todas as compras, ordenadas por data de compra (mais recente primeiro)
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
    // UPDATE
    // ============================
    // Cancela uma compra, marcando-a como cancelada
    public boolean cancelar(int compraId, String usuarioLogado) throws SQLException {
        String sql = "UPDATE compra SET cancelada = ?, usuario = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, true);
            stmt.setString(2, usuarioLogado);
            stmt.setInt(3, compraId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    // Deleta uma compra por ID
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
    // Mapeia uma linha do ResultSet para um objeto Compra
    private Compra mapRow(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setId(rs.getInt("id"));
        c.setDataCompra(rs.getTimestamp("data_compra"));
        c.setUsuario(rs.getString("usuario"));
        c.setCancelada(rs.getBoolean("cancelada"));
        return c;
    }
}