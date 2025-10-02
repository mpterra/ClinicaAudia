package dao;

import model.PagamentoVenda;
import model.Venda;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoVendaDAO {

    // -----------------------------
    // Inserir pagamento
    // -----------------------------
    public void insert(PagamentoVenda pagamento) throws SQLException {
        String sql = "INSERT INTO pagamento_venda (venda_id, data_vencimento, valor, metodo_pagamento, parcela, total_parcelas, observacoes, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pagamento.getVenda().getId());
            stmt.setDate(2, Date.valueOf(pagamento.getDataVencimento()));
            stmt.setBigDecimal(3, pagamento.getValor());
            stmt.setString(4, pagamento.getMetodoPagamento().name());
            stmt.setInt(5, pagamento.getParcela());
            stmt.setInt(6, pagamento.getTotalParcelas());
            stmt.setString(7, pagamento.getObservacoes());
            stmt.setString(8, pagamento.getUsuario());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setId(rs.getInt(1));
                }
            }
        }
    }

    // -----------------------------
    // Atualizar pagamento
    // -----------------------------
    public void update(PagamentoVenda pagamento) throws SQLException {
        String sql = "UPDATE pagamento_venda SET data_vencimento=?, valor=?, metodo_pagamento=?, parcela=?, total_parcelas=?, observacoes=?, usuario=?, atualizado_em=NOW() " +
                     "WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(pagamento.getDataVencimento()));
            stmt.setBigDecimal(2, pagamento.getValor());
            stmt.setString(3, pagamento.getMetodoPagamento().name());
            stmt.setInt(4, pagamento.getParcela());
            stmt.setInt(5, pagamento.getTotalParcelas());
            stmt.setString(6, pagamento.getObservacoes());
            stmt.setString(7, pagamento.getUsuario());
            stmt.setInt(8, pagamento.getId());

            stmt.executeUpdate();
        }
    }

    // -----------------------------
    // Deletar pagamento
    // -----------------------------
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pagamento_venda WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // -----------------------------
    // Buscar por ID
    // -----------------------------
    public PagamentoVenda findById(int id) throws SQLException {
        String sql = "SELECT * FROM pagamento_venda WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // -----------------------------
    // Buscar por venda
    // -----------------------------
    public List<PagamentoVenda> findByVenda(Venda venda) throws SQLException {
        String sql = "SELECT * FROM pagamento_venda WHERE venda_id=? ORDER BY data_hora";
        List<PagamentoVenda> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venda.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        }
        return lista;
    }

    // -----------------------------
    // Listar todos
    // -----------------------------
    public List<PagamentoVenda> findAll() throws SQLException {
        String sql = "SELECT * FROM pagamento_venda ORDER BY data_hora";
        List<PagamentoVenda> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    // -----------------------------
    // Mapear ResultSet
    // -----------------------------
    private PagamentoVenda mapResultSet(ResultSet rs) throws SQLException {
        PagamentoVenda pagamento = new PagamentoVenda();
        pagamento.setId(rs.getInt("id"));

        Venda venda = new Venda();
        venda.setId(rs.getInt("venda_id"));
        pagamento.setVenda(venda);

        pagamento.setValor(rs.getBigDecimal("valor"));
        pagamento.setMetodoPagamento(PagamentoVenda.MetodoPagamento.valueOf(rs.getString("metodo_pagamento")));
        pagamento.setParcela(rs.getInt("parcela"));
        pagamento.setTotalParcelas(rs.getInt("total_parcelas"));
        pagamento.setObservacoes(rs.getString("observacoes"));
        pagamento.setUsuario(rs.getString("usuario"));

        Timestamp ts = rs.getTimestamp("data_hora");
        if (ts != null) {
            pagamento.setDataHora(ts.toLocalDateTime());
        }

        Date dv = rs.getDate("data_vencimento");
        if (dv != null) {
            pagamento.setDataVencimento(dv.toLocalDate());
        }

        return pagamento;
    }
}
