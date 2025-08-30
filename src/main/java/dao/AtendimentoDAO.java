package dao;

import model.Atendimento;
import model.Paciente;
import model.Profissional;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoDAO {

    // -------------------------
    // CRUD básico
    // -------------------------

    public void inserir(Atendimento atendimento) throws SQLException {
        String sql = "INSERT INTO atendimento (paciente_id, profissional_id, data_hora, duracao_min, tipo, situacao, notas, valor, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, atendimento.getPaciente().getId());
            ps.setInt(2, atendimento.getProfissional().getId());
            ps.setTimestamp(3, Timestamp.valueOf(atendimento.getDataHora()));
            ps.setInt(4, atendimento.getDuracaoMin());
            ps.setString(5, atendimento.getTipo().name());
            ps.setString(6, atendimento.getSituacao().name());
            ps.setString(7, atendimento.getNotas());
            ps.setBigDecimal(8, atendimento.getValor());
            ps.setString(9, atendimento.getUsuario());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    atendimento.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Atendimento atendimento) throws SQLException {
        String sql = "UPDATE atendimento SET paciente_id=?, profissional_id=?, data_hora=?, duracao_min=?, tipo=?, situacao=?, notas=?, valor=?, usuario=? " +
                     "WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, atendimento.getPaciente().getId());
            ps.setInt(2, atendimento.getProfissional().getId());
            ps.setTimestamp(3, Timestamp.valueOf(atendimento.getDataHora()));
            ps.setInt(4, atendimento.getDuracaoMin());
            ps.setString(5, atendimento.getTipo().name());
            ps.setString(6, atendimento.getSituacao().name());
            ps.setString(7, atendimento.getNotas());
            ps.setBigDecimal(8, atendimento.getValor());
            ps.setString(9, atendimento.getUsuario());
            ps.setInt(10, atendimento.getId());

            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM atendimento WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Atendimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM atendimento WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAtendimento(rs);
                }
            }
        }
        return null;
    }

    // -------------------------
    // Consultas úteis para usuário
    // -------------------------

    public List<Atendimento> listarPorPaciente(int pacienteId) throws SQLException {
        String sql = "SELECT * FROM atendimento WHERE paciente_id=?";
        List<Atendimento> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAtendimento(rs));
                }
            }
        }
        return lista;
    }

    public List<Atendimento> listarPorProfissional(int profissionalId) throws SQLException {
        String sql = "SELECT * FROM atendimento WHERE profissional_id=?";
        List<Atendimento> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profissionalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAtendimento(rs));
                }
            }
        }
        return lista;
    }

    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = "SELECT * FROM atendimento WHERE data_hora BETWEEN ? AND ?";
        List<Atendimento> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAtendimento(rs));
                }
            }
        }
        return lista;
    }

    public List<Atendimento> listarPorSituacao(Atendimento.Situacao situacao) throws SQLException {
        String sql = "SELECT * FROM atendimento WHERE situacao=?";
        List<Atendimento> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, situacao.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAtendimento(rs));
                }
            }
        }
        return lista;
    }

    // -------------------------
    // Mapeamento do ResultSet
    // -------------------------
    private Atendimento mapearAtendimento(ResultSet rs) throws SQLException {
        Atendimento atendimento = new Atendimento();
        atendimento.setId(rs.getInt("id"));

        // Criar objetos Paciente e Profissional com ID
        Paciente p = new Paciente();
        p.setId(rs.getInt("paciente_id"));
        atendimento.setPaciente(p);

        Profissional prof = new Profissional();
        prof.setId(rs.getInt("profissional_id"));
        atendimento.setProfissional(prof);

        atendimento.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        atendimento.setDuracaoMin(rs.getInt("duracao_min"));
        atendimento.setTipo(Atendimento.TipoAtendimento.valueOf(rs.getString("tipo")));
        atendimento.setSituacao(Atendimento.Situacao.valueOf(rs.getString("situacao")));
        atendimento.setNotas(rs.getString("notas"));
        atendimento.setValor(rs.getBigDecimal("valor"));
        atendimento.setUsuario(rs.getString("usuario"));

        Timestamp criadoTs = rs.getTimestamp("criado_em");
        if (criadoTs != null) atendimento.setCriadoEm(criadoTs.toLocalDateTime());

        Timestamp atualizadoTs = rs.getTimestamp("atualizado_em");
        if (atualizadoTs != null) atendimento.setAtualizadoEm(atualizadoTs.toLocalDateTime());

        return atendimento;
    }
}
