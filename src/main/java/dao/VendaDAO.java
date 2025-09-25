package dao;

import model.Venda;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Venda venda, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO venda (atendimento_id, paciente_id, orcamento_id, valor_total, usuario) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // atendimento_id
            if (venda.getAtendimentoId() != null) {
                stmt.setInt(1, venda.getAtendimentoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            // paciente_id
            if (venda.getPacienteId() != null) {
                stmt.setInt(2, venda.getPacienteId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            // orcamento_id
            if (venda.getOrcamentoId() != null) {
                stmt.setInt(3, venda.getOrcamentoId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            // valor_total e usuario
            stmt.setBigDecimal(4, venda.getValorTotal());
            stmt.setString(5, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir Venda, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    venda.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public Venda buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM venda WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Venda> listarTodos() throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT * FROM venda ORDER BY data_hora DESC";
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
    public boolean atualizar(Venda venda, String usuarioLogado) throws SQLException {
        String sql = "UPDATE venda SET atendimento_id = ?, paciente_id = ?, orcamento_id = ?, " +
                     "valor_total = ?, usuario = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // atendimento_id
            if (venda.getAtendimentoId() != null) {
                stmt.setInt(1, venda.getAtendimentoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            // paciente_id
            if (venda.getPacienteId() != null) {
                stmt.setInt(2, venda.getPacienteId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            // orcamento_id
            if (venda.getOrcamentoId() != null) {
                stmt.setInt(3, venda.getOrcamentoId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            // valor_total
            stmt.setBigDecimal(4, venda.getValorTotal());

            // usuário logado
            stmt.setString(5, usuarioLogado);

            // id da venda (WHERE)
            stmt.setInt(6, venda.getId());

            return stmt.executeUpdate() > 0;
        }
    }


    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM venda WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private Venda mapRow(ResultSet rs) throws SQLException {
        Venda v = new Venda();

        // id
        v.setId(rs.getInt("id"));

        // atendimento_id
        int atendimentoId = rs.getInt("atendimento_id");
        if (!rs.wasNull()) v.setAtendimentoId(atendimentoId);

        // paciente_id
        int pacienteId = rs.getInt("paciente_id");
        if (!rs.wasNull()) v.setPacienteId(pacienteId);

        // orcamento_id
        int orcamentoId = rs.getInt("orcamento_id");
        if (!rs.wasNull()) v.setOrcamentoId(orcamentoId);

        // demais campos
        v.setValorTotal(rs.getBigDecimal("valor_total"));
        v.setDataHora(rs.getTimestamp("data_hora"));
        v.setUsuario(rs.getString("usuario"));

        return v;
    }
}
