package dao;

import model.ValorAtendimento;
import model.ValorAtendimento.Tipo;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ValorAtendimentoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(ValorAtendimento valorAtendimento, String usuarioLogado) throws SQLException {
        String sql = """
            INSERT INTO valor_atendimento (profissional_id, tipo, valor, usuario)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, valorAtendimento.getProfissionalId());
            stmt.setString(2, valorAtendimento.getTipo().name());
            stmt.setBigDecimal(3, valorAtendimento.getValor() != null ? valorAtendimento.getValor() : BigDecimal.ZERO);
            stmt.setString(4, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir ValorAtendimento, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    valorAtendimento.setId(rs.getInt(1));
                }
            }
        }
        return true;
    }

    // ============================
    // READ
    // ============================
    public ValorAtendimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM valor_atendimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<ValorAtendimento> listarTodos() throws SQLException {
        List<ValorAtendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM valor_atendimento ORDER BY criado_em DESC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public List<ValorAtendimento> listarPorProfissional(int profissionalId) throws SQLException {
        List<ValorAtendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM valor_atendimento WHERE profissional_id = ? ORDER BY criado_em DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profissionalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    // ============================
    // UPDATE
    // ============================
    public boolean atualizar(ValorAtendimento valorAtendimento, String usuarioLogado) throws SQLException {
        String sql = """
            UPDATE valor_atendimento
            SET profissional_id = ?, tipo = ?, valor = ?, usuario = ?, atualizado_em = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, valorAtendimento.getProfissionalId());
            stmt.setString(2, valorAtendimento.getTipo().name());
            stmt.setBigDecimal(3, valorAtendimento.getValor() != null ? valorAtendimento.getValor() : BigDecimal.ZERO);
            stmt.setString(4, usuarioLogado);
            stmt.setInt(5, valorAtendimento.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM valor_atendimento WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private ValorAtendimento mapRow(ResultSet rs) throws SQLException {
        ValorAtendimento v = new ValorAtendimento();
        v.setId(rs.getInt("id"));
        v.setProfissionalId(rs.getInt("profissional_id"));
        v.setTipo(Tipo.valueOf(rs.getString("tipo")));
        v.setValor(rs.getBigDecimal("valor"));
        v.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        v.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());
        v.setUsuario(rs.getString("usuario"));
        return v;
    }

	public ValorAtendimento buscarPorProfissionalETipo(int id, model.Atendimento.Tipo tipo) {
		String sql = "SELECT * FROM valor_atendimento WHERE profissional_id = ? AND tipo = ?";
		try (Connection conn = Database.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			stmt.setString(2, tipo.name());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) return mapRow(rs);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
