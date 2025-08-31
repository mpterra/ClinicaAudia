package dao;

import model.PagamentoAtendimento;
import util.Database;
import model.Atendimento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoAtendimentoDAO {

    // -----------------------------
    // Inserir pagamento
    // -----------------------------
    public void insert(PagamentoAtendimento pagamento) throws SQLException {
        String sql = "INSERT INTO pagamento_atendimento (atendimento_id, valor, metodo_pagamento, observacoes, usuario) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pagamento.getAtendimento().getId());
            stmt.setBigDecimal(2, pagamento.getValor());
            stmt.setString(3, pagamento.getMetodoPagamento().name());
            stmt.setString(4, pagamento.getObservacoes());
            stmt.setString(5, pagamento.getUsuario());

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
    public void update(PagamentoAtendimento pagamento) throws SQLException {
        String sql = "UPDATE pagamento_atendimento SET valor=?, metodo_pagamento=?, observacoes=?, usuario=?, atualizado_em=NOW() " +
                     "WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, pagamento.getValor());
            stmt.setString(2, pagamento.getMetodoPagamento().name());
            stmt.setString(3, pagamento.getObservacoes());
            stmt.setString(4, pagamento.getUsuario());
            stmt.setInt(5, pagamento.getId());

            stmt.executeUpdate();
        }
    }

    // -----------------------------
    // Deletar pagamento
    // -----------------------------
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pagamento_atendimento WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // -----------------------------
    // Buscar por ID
    // -----------------------------
    public PagamentoAtendimento findById(int id) throws SQLException {
        String sql = "SELECT * FROM pagamento_atendimento WHERE id=?";
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
    // Buscar por atendimento
    // -----------------------------
    public List<PagamentoAtendimento> findByAtendimento(Atendimento atendimento) throws SQLException {
        String sql = "SELECT * FROM pagamento_atendimento WHERE atendimento_id=? ORDER BY data_hora";
        List<PagamentoAtendimento> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, atendimento.getId());
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
    public List<PagamentoAtendimento> findAll() throws SQLException {
        String sql = "SELECT * FROM pagamento_atendimento ORDER BY data_hora";
        List<PagamentoAtendimento> lista = new ArrayList<>();
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
    private PagamentoAtendimento mapResultSet(ResultSet rs) throws SQLException {
        PagamentoAtendimento pagamento = new PagamentoAtendimento();
        pagamento.setId(rs.getInt("id"));

        Atendimento atendimento = new Atendimento();
        atendimento.setId(rs.getInt("atendimento_id"));
        pagamento.setAtendimento(atendimento);

        pagamento.setValor(rs.getBigDecimal("valor"));
        pagamento.setMetodoPagamento(PagamentoAtendimento.MetodoPagamento.valueOf(rs.getString("metodo_pagamento")));
        pagamento.setObservacoes(rs.getString("observacoes"));
        pagamento.setUsuario(rs.getString("usuario"));

        // dataHora é preenchido pelo banco, apenas leitura
        Timestamp ts = rs.getTimestamp("data_hora");
        if (ts != null) {
            // aqui você pode armazenar LocalDateTime em algum campo temporário se necessário
        }

        return pagamento;
    }
}
