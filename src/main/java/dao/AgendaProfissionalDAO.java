package dao;

import model.AgendaProfissional;
import model.Profissional;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AgendaProfissionalDAO {

    // -------------------------
    // CRUD básico
    // -------------------------

    public void inserir(AgendaProfissional agenda) throws SQLException {
        String sql = "INSERT INTO agenda_profissional (profissional_id, data_hora_inicio, data_hora_fim, disponivel, usuario) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, agenda.getProfissional().getId());
            ps.setTimestamp(2, Timestamp.valueOf(agenda.getDataHoraInicio()));
            ps.setTimestamp(3, Timestamp.valueOf(agenda.getDataHoraFim()));
            ps.setBoolean(4, agenda.isDisponivel());
            ps.setString(5, agenda.getUsuario());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    agenda.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(AgendaProfissional agenda) throws SQLException {
        String sql = "UPDATE agenda_profissional SET profissional_id=?, data_hora_inicio=?, data_hora_fim=?, disponivel=?, usuario=? " +
                     "WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, agenda.getProfissional().getId());
            ps.setTimestamp(2, Timestamp.valueOf(agenda.getDataHoraInicio()));
            ps.setTimestamp(3, Timestamp.valueOf(agenda.getDataHoraFim()));
            ps.setBoolean(4, agenda.isDisponivel());
            ps.setString(5, agenda.getUsuario());
            ps.setInt(6, agenda.getId());

            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM agenda_profissional WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public AgendaProfissional buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM agenda_profissional WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAgenda(rs);
                }
            }
        }
        return null;
    }

    // -------------------------
    // Consultas úteis para usuário
    // -------------------------

    public List<AgendaProfissional> listarPorProfissional(int profissionalId) throws SQLException {
        String sql = "SELECT * FROM agenda_profissional WHERE profissional_id=?";
        List<AgendaProfissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profissionalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgenda(rs));
                }
            }
        }
        return lista;
    }

    public List<AgendaProfissional> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = "SELECT * FROM agenda_profissional WHERE data_hora_inicio BETWEEN ? AND ?";
        List<AgendaProfissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgenda(rs));
                }
            }
        }
        return lista;
    }

    public List<AgendaProfissional> listarDisponiveis(int profissionalId) throws SQLException {
        String sql = "SELECT * FROM agenda_profissional WHERE profissional_id=? AND disponivel=1";
        List<AgendaProfissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profissionalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgenda(rs));
                }
            }
        }
        return lista;
    }

    public List<AgendaProfissional> listarPorProfissionalEPeriodo(int profissionalId, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = "SELECT * FROM agenda_profissional WHERE profissional_id=? AND data_hora_inicio BETWEEN ? AND ?";
        List<AgendaProfissional> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profissionalId);
            ps.setTimestamp(2, Timestamp.valueOf(inicio));
            ps.setTimestamp(3, Timestamp.valueOf(fim));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgenda(rs));
                }
            }
        }
        return lista;
    }

    // -------------------------
    // Mapeamento do ResultSet
    // -------------------------
    private AgendaProfissional mapearAgenda(ResultSet rs) throws SQLException {
        AgendaProfissional agenda = new AgendaProfissional();
        agenda.setId(rs.getInt("id"));

        // Criar objeto Profissional só com ID (ou buscar completo se necessário)
        Profissional p = new Profissional();
        p.setId(rs.getInt("profissional_id"));
        agenda.setProfissional(p);

        agenda.setDataHoraInicio(rs.getTimestamp("data_hora_inicio").toLocalDateTime());
        agenda.setDataHoraFim(rs.getTimestamp("data_hora_fim").toLocalDateTime());
        agenda.setDisponivel(rs.getBoolean("disponivel"));
        agenda.setUsuario(rs.getString("usuario"));

        return agenda;
    }
}
