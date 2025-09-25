package dao;

import model.Produto;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ProdutoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Produto produto, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO produto (tipo_produto_id, nome, codigo, descricao, usuario, garantia_meses, preco_venda, preco_custo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, produto.getTipoProdutoId());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getCodigoSerial());
            stmt.setString(4, produto.getDescricao());
            stmt.setString(5, usuarioLogado);
            stmt.setInt(6, produto.getGarantiaMeses());
            stmt.setBigDecimal(7, produto.getPrecoVenda());
            stmt.setBigDecimal(8, produto.getPrecoCusto());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir Produto, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Produto> listarTodos() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produto ORDER BY nome";
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
    public boolean atualizar(Produto produto, String usuarioLogado) throws SQLException {
        String sql = "UPDATE produto SET tipo_produto_id = ?, nome = ?, codigo_serial = ?, descricao = ?, " +
                     "usuario = ?, atualizado_em = CURRENT_TIMESTAMP, garantia_meses = ?, preco_venda = ?, preco_custo = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produto.getTipoProdutoId());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getCodigoSerial());
            stmt.setString(4, produto.getDescricao());
            stmt.setString(5, usuarioLogado);
            stmt.setInt(6, produto.getGarantiaMeses());
            stmt.setBigDecimal(7, produto.getPrecoVenda());
            stmt.setBigDecimal(8, produto.getPrecoCusto());
            stmt.setInt(9, produto.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setTipoProdutoId(rs.getInt("tipo_produto_id"));
        p.setNome(rs.getString("nome"));
        p.setCodigoSerial(rs.getString("codigo_serial"));
        p.setDescricao(rs.getString("descricao"));
        p.setCriadoEm(rs.getTimestamp("criado_em"));
        p.setAtualizadoEm(rs.getTimestamp("atualizado_em"));
        p.setUsuario(rs.getString("usuario"));
        p.setGarantiaMeses(rs.getInt("garantia_meses"));
        p.setPrecoVenda(rs.getBigDecimal("preco_venda"));
        p.setPrecoCusto(rs.getBigDecimal("preco_custo"));
        return p;
    }
}