package dao;

import model.EvolucaoAtendimento;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvolucaoAtendimentoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(EvolucaoAtendimento evo, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO evolucao_atendimento (atendimento_id, notas, arquivo, usuario) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, evo.getAtendimentoId());
            stmt.setString(2, evo.getNotas());
            stmt.setString(3, evo.getArquivo());
            stmt.setString(4, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir EvolucaoAtendimento, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    evo.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public EvolucaoAtendimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM evolucao_atendimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<EvolucaoAtendimento> listarPorAtendimento(int atendimentoId) throws SQLException {
        List<EvolucaoAtendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM evolucao_atendimento WHERE atendimento_id = ? ORDER BY criado_em";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, atendimentoId);
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
        String sql = "DELETE FROM evolucao_atendimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private EvolucaoAtendimento mapRow(ResultSet rs) throws SQLException {
        EvolucaoAtendimento evo = new EvolucaoAtendimento();
        evo.setId(rs.getInt("id"));
        evo.setAtendimentoId(rs.getInt("atendimento_id"));
        evo.setNotas(rs.getString("notas"));
        evo.setArquivo(rs.getString("arquivo"));
        evo.setCriadoEm(rs.getTimestamp("criado_em"));
        evo.setUsuario(rs.getString("usuario"));
        return evo;
    }
}
