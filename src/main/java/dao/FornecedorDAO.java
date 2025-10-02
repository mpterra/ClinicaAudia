package dao;

import model.Fornecedor;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO {

    // -------------------------
    // CREATE
    // -------------------------
    public boolean salvar(Fornecedor fornecedor) throws SQLException {
        String sql = "INSERT INTO fornecedor (nome, cnpj, telefone, email, id_endereco, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fornecedor.getNome());
            stmt.setString(2, fornecedor.getCnpj());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());

            if (fornecedor.getIdEndereco() != null) {
                stmt.setInt(5, fornecedor.getIdEndereco());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, fornecedor.getUsuario());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        fornecedor.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    // -------------------------
    // READ by ID
    // -------------------------
    public Fornecedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM fornecedor WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
            return null;
        }
    }

    // -------------------------
    // READ all
    // -------------------------
    public List<Fornecedor> listarTodos() throws SQLException {
        List<Fornecedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM fornecedor ORDER BY nome";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
            return lista;
        }
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public boolean atualizar(Fornecedor fornecedor) throws SQLException {
        String sql = "UPDATE fornecedor SET nome = ?, cnpj = ?, telefone = ?, email = ?, " +
                     "id_endereco = ?, usuario = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fornecedor.getNome());
            stmt.setString(2, fornecedor.getCnpj());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());

            if (fornecedor.getIdEndereco() != null) {
                stmt.setInt(5, fornecedor.getIdEndereco());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, fornecedor.getUsuario());
            stmt.setInt(7, fornecedor.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM fornecedor WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // -------------------------
    // Buscar fornecedor da última compra de um produto
    // -------------------------
    public Fornecedor buscarFornecedorUltimaCompra(int produtoId) throws SQLException {
        String sql = "SELECT f.id, f.nome " +
                     "FROM fornecedor f " +
                     "JOIN compra_produto cp ON cp.fornecedor_id = f.id " +
                     "JOIN compra c ON cp.compra_id = c.id " +
                     "WHERE cp.produto_id = ? " +
                     "ORDER BY c.data_compra DESC LIMIT 1";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Fornecedor f = new Fornecedor();
                    f.setId(rs.getInt("id"));
                    f.setNome(rs.getString("nome"));
                    return f;
                }
                return null;
            }
        }
    }

    // -------------------------
    // MAP ResultSet -> Fornecedor
    // -------------------------
    private Fornecedor mapResultSet(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setId(rs.getInt("id"));
        f.setNome(rs.getString("nome"));
        f.setCnpj(rs.getString("cnpj"));
        f.setTelefone(rs.getString("telefone"));
        f.setEmail(rs.getString("email"));

        int idEndereco = rs.getInt("id_endereco");
        if (rs.wasNull()) {
            f.setIdEndereco(null);
        } else {
            f.setIdEndereco(idEndereco);
        }

        Timestamp criado = rs.getTimestamp("criado_em");
        if (criado != null) {
            f.setCriadoEm(criado.toLocalDateTime());
        }

        Timestamp atualizado = rs.getTimestamp("atualizado_em");
        if (atualizado != null) {
            f.setAtualizadoEm(atualizado.toLocalDateTime());
        }

        f.setUsuario(rs.getString("usuario"));

        return f;
    }
}