package dao;

import model.EscalaProfissional;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EscalaProfissionalDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(EscalaProfissional escala, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO escala_profissional (profissional_id, dia_semana, hora_inicio, hora_fim, disponivel, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, escala.getProfissionalId());
            stmt.setInt(2, escala.getDiaSemana());
            stmt.setTime(3, escala.getHoraInicio());
            stmt.setTime(4, escala.getHoraFim());
            stmt.setBoolean(5, escala.isDisponivel());
            stmt.setString(6, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir EscalaProfissional, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    escala.setId(generatedKeys.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public EscalaProfissional buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM escala_profissional WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<EscalaProfissional> listarTodos() throws SQLException {
        List<EscalaProfissional> lista = new ArrayList<>();
        String sql = "SELECT * FROM escala_profissional ORDER BY dia_semana, hora_inicio";
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
    public boolean atualizar(EscalaProfissional escala, String usuarioLogado) throws SQLException {
        String sql = "UPDATE escala_profissional SET profissional_id = ?, dia_semana = ?, hora_inicio = ?, hora_fim = ?, " +
                     "disponivel = ?, usuario = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, escala.getProfissionalId());
            stmt.setInt(2, escala.getDiaSemana());
            stmt.setTime(3, escala.getHoraInicio());
            stmt.setTime(4, escala.getHoraFim());
            stmt.setBoolean(5, escala.isDisponivel());
            stmt.setString(6, usuarioLogado);
            stmt.setInt(7, escala.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM escala_profissional WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private EscalaProfissional mapRow(ResultSet rs) throws SQLException {
        EscalaProfissional e = new EscalaProfissional();
        e.setId(rs.getInt("id"));
        e.setProfissionalId(rs.getInt("profissional_id"));
        e.setDiaSemana(rs.getInt("dia_semana"));
        e.setHoraInicio(rs.getTime("hora_inicio"));
        e.setHoraFim(rs.getTime("hora_fim"));
        e.setDisponivel(rs.getBoolean("disponivel"));
        e.setCriadoEm(rs.getTimestamp("criado_em"));
        e.setAtualizadoEm(rs.getTimestamp("atualizado_em"));
        e.setUsuario(rs.getString("usuario"));
        return e;
    }

	public List<EscalaProfissional> listarPorProfissional(int id) {
		List<EscalaProfissional> lista = new ArrayList<>();
		String sql = "SELECT * FROM escala_profissional WHERE profissional_id = ? ORDER BY dia_semana, hora_inicio";
		try (Connection conn = Database.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}

	public void removerTodasEscalasDoProfissional(int id) {
		String sql = "DELETE FROM escala_profissional WHERE profissional_id = ?";
		try (Connection conn = Database.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
}
