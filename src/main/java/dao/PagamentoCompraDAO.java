package dao;

import model.PagamentoCompra;
import model.PagamentoCompra.MetodoPagamento;
import model.PagamentoCompra.StatusPagamento;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoCompraDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(PagamentoCompra pc, String usuarioLogado) throws SQLException {
        String sql = """
            INSERT INTO pagamento_compra 
            (compra_id, data_vencimento, valor, metodo_pagamento, parcela, total_parcelas, status, observacoes, usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pc.getCompraId());
            stmt.setDate(2, pc.getDataVencimento());
            stmt.setBigDecimal(3, pc.getValor());
            stmt.setString(4, pc.getMetodoPagamento().name());
            stmt.setInt(5, pc.getParcela());
            stmt.setInt(6, pc.getTotalParcelas());
            stmt.setString(7, pc.getStatus().name());
            stmt.setString(8, pc.getObservacoes());
            stmt.setString(9, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Falha ao inserir PagamentoCompra.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) pc.setId(rs.getInt(1));
            }
        }
        return true;
    }

    // ============================
    // READ
    // ============================
    public PagamentoCompra buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pagamento_compra WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<PagamentoCompra> listarTodos() throws SQLException {
        List<PagamentoCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagamento_compra ORDER BY data_vencimento";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    public List<PagamentoCompra> listarPorCompra(int compraId) throws SQLException {
        List<PagamentoCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagamento_compra WHERE compra_id = ? ORDER BY parcela";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, compraId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(PagamentoCompra pc, String usuarioLogado) throws SQLException {
        String sql = """
            UPDATE pagamento_compra SET 
                compra_id = ?, data_vencimento = ?, valor = ?, metodo_pagamento = ?, 
                parcela = ?, total_parcelas = ?, status = ?, observacoes = ?, usuario = ?
            WHERE id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pc.getCompraId());
            stmt.setDate(2, pc.getDataVencimento());
            stmt.setBigDecimal(3, pc.getValor());
            stmt.setString(4, pc.getMetodoPagamento().name());
            stmt.setInt(5, pc.getParcela());
            stmt.setInt(6, pc.getTotalParcelas());
            stmt.setString(7, pc.getStatus().name());
            stmt.setString(8, pc.getObservacoes());
            stmt.setString(9, usuarioLogado);
            stmt.setInt(10, pc.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM pagamento_compra WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAPEAMENTO
    // ============================
    private PagamentoCompra mapRow(ResultSet rs) throws SQLException {
        PagamentoCompra pc = new PagamentoCompra();
        pc.setId(rs.getInt("id"));
        pc.setCompraId(rs.getInt("compra_id"));
        pc.setDataHora(rs.getTimestamp("data_hora"));
        pc.setDataVencimento(rs.getDate("data_vencimento"));
        pc.setValor(rs.getBigDecimal("valor"));
        pc.setMetodoPagamento(MetodoPagamento.valueOf(rs.getString("metodo_pagamento")));
        pc.setParcela(rs.getInt("parcela"));
        pc.setTotalParcelas(rs.getInt("total_parcelas"));
        pc.setStatus(StatusPagamento.valueOf(rs.getString("status")));
        pc.setObservacoes(rs.getString("observacoes"));
        pc.setUsuario(rs.getString("usuario"));
        return pc;
    }
}
