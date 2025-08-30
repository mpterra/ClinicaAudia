package dao;

import model.TipoProduto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoProdutoDAO {

    // ============================
    // CREATE
    // ============================
    public TipoProduto salvar(TipoProduto tipoProduto) throws SQLException {
        String sql = "INSERT INTO tipo_produto (nome, descricao, usuario) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tipoProduto.getNome());
            stmt.setString(2, tipoProduto.getDescricao());
            stmt.setString(3, tipoProduto.getUsuario());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir TipoProduto, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tipoProduto.setId(generatedKeys.getInt(1));
                }
            }
        }
        return tipoProduto;
    }

    // ============================
    // READ
    // ============================
    public TipoProduto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM tipo_produto WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<TipoProduto> listarTodos() throws SQLException {
        List<TipoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipo_produto ORDER BY nome";
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
    // BUSCA POR NOME (autocomplete)
    // ============================
    public List<TipoProduto> buscarPorNomeLike(String termo) throws SQLException {
        List<TipoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipo_produto WHERE nome LIKE ? ORDER BY nome";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + termo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(TipoProduto tipoProduto) throws SQLException {
        String sql = "UPDATE tipo_produto SET nome = ?, descricao = ?, atualizado_em = CURRENT_TIMESTAMP, usuario = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoProduto.getNome());
            stmt.setString(2, tipoProduto.getDescricao());
            stmt.setString(3, tipoProduto.getUsuario());
            stmt.setInt(4, tipoProduto.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM tipo_produto WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private TipoProduto mapRow(ResultSet rs) throws SQLException {
        TipoProduto tp = new TipoProduto();
        tp.setId(rs.getInt("id"));
        tp.setNome(rs.getString("nome"));
        tp.setDescricao(rs.getString("descricao"));
        tp.setCriadoEm(rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null);
        tp.setAtualizadoEm(rs.getTimestamp("atualizado_em") != null ? rs.getTimestamp("atualizado_em").toLocalDateTime() : null);
        tp.setUsuario(rs.getString("usuario"));
        return tp;
    }
}
