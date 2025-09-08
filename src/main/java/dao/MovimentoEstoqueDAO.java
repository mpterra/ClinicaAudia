package dao;

import model.MovimentoEstoque;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimentoEstoqueDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(MovimentoEstoque mov, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO movimento_estoque (produto_id, quantidade, tipo, observacoes, usuario) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mov.getProdutoId());
            stmt.setInt(2, mov.getQuantidade());
            stmt.setString(3, mov.getTipo().name());
            stmt.setString(4, mov.getObservacoes());
            stmt.setString(5, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir MovimentoEstoque, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    mov.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public MovimentoEstoque buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM movimento_estoque WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<MovimentoEstoque> listarTodos() throws SQLException {
        List<MovimentoEstoque> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimento_estoque ORDER BY data_hora DESC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public List<MovimentoEstoque> listarPorProduto(int produtoId) throws SQLException {
        List<MovimentoEstoque> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimento_estoque WHERE produto_id = ? ORDER BY data_hora DESC";
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
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM movimento_estoque WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private MovimentoEstoque mapRow(ResultSet rs) throws SQLException {
        MovimentoEstoque mov = new MovimentoEstoque();
        mov.setId(rs.getInt("id"));
        mov.setProdutoId(rs.getInt("produto_id"));
        mov.setQuantidade(rs.getInt("quantidade"));
        mov.setTipo(MovimentoEstoque.Tipo.valueOf(rs.getString("tipo")));
        mov.setObservacoes(rs.getString("observacoes"));
        mov.setDataHora(rs.getTimestamp("data_hora"));
        mov.setUsuario(rs.getString("usuario"));
        return mov;
    }
}
