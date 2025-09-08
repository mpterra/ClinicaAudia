package dao;

import model.Orcamento;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Orcamento orcamento, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO orcamento (paciente_id, profissional_id, valor_total, observacoes, usuario) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (orcamento.getPacienteId() != null) {
                stmt.setInt(1, orcamento.getPacienteId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (orcamento.getProfissionalId() != null) {
                stmt.setInt(2, orcamento.getProfissionalId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setBigDecimal(3, orcamento.getValorTotal());
            stmt.setString(4, orcamento.getObservacoes());
            stmt.setString(5, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir Orçamento, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    orcamento.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public Orcamento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM orcamento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Orcamento> listarTodos() throws SQLException {
        List<Orcamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM orcamento ORDER BY data_hora DESC";
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
        String sql = "DELETE FROM orcamento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private Orcamento mapRow(ResultSet rs) throws SQLException {
        Orcamento o = new Orcamento();

        int pacienteId = rs.getInt("paciente_id");
        if (!rs.wasNull()) o.setPacienteId(pacienteId);

        int profissionalId = rs.getInt("profissional_id");
        if (!rs.wasNull()) o.setProfissionalId(profissionalId);

        o.setId(rs.getInt("id"));
        o.setValorTotal(rs.getBigDecimal("valor_total"));
        o.setObservacoes(rs.getString("observacoes"));
        o.setDataHora(rs.getTimestamp("data_hora"));
        o.setUsuario(rs.getString("usuario"));

        return o;
    }
}
