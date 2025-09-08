package dao;

import model.Atendimento;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AtendimentoDAO {

    // ============================
    // CREATE
    // ============================
    public boolean salvar(Atendimento at, String usuarioLogado) throws SQLException {
        String sql = "INSERT INTO atendimento (paciente_id, profissional_id, data_hora, duracao_min, tipo, situacao, notas, valor, usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, at.getPacienteId());
            stmt.setInt(2, at.getProfissionalId());
            stmt.setTimestamp(3, at.getDataHora());
            stmt.setInt(4, at.getDuracaoMin());
            stmt.setString(5, at.getTipo().name());
            stmt.setString(6, at.getSituacao().name());
            stmt.setString(7, at.getNotas());
            stmt.setBigDecimal(8, at.getValor());
            stmt.setString(9, usuarioLogado);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir Atendimento, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    at.setId(rs.getInt(1));
                }
            }
        }

        return true;
    }

    // ============================
    // READ
    // ============================
    public Atendimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT a.*, p.nome AS pacienteNome, pr.nome AS profissionalNome " +
                     "FROM atendimento a " +
                     "JOIN paciente p ON a.paciente_id = p.id " +
                     "JOIN profissional pr ON a.profissional_id = pr.id " +
                     "WHERE a.id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Atendimento> listarTodos() throws SQLException {
        List<Atendimento> lista = new ArrayList<>();
        String sql = "SELECT a.*, p.nome AS pacienteNome, pr.nome AS profissionalNome " +
                     "FROM atendimento a " +
                     "JOIN paciente p ON a.paciente_id = p.id " +
                     "JOIN profissional pr ON a.profissional_id = pr.id " +
                     "ORDER BY a.data_hora";

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
    // LISTAR POR PERÍODO
    // ============================
    public List<Atendimento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<Atendimento> lista = new ArrayList<>();
        String sql = "SELECT a.*, p.nome AS pacienteNome, pr.nome AS profissionalNome " +
                     "FROM atendimento a " +
                     "JOIN paciente p ON a.paciente_id = p.id " +
                     "JOIN profissional pr ON a.profissional_id = pr.id " +
                     "WHERE a.data_hora BETWEEN ? AND ? " +
                     "ORDER BY a.data_hora";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

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
    public boolean atualizar(Atendimento at, String usuarioLogado) throws SQLException {
        String sql = "UPDATE atendimento SET paciente_id = ?, profissional_id = ?, data_hora = ?, duracao_min = ?, tipo = ?, situacao = ?, notas = ?, valor = ?, usuario = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, at.getPacienteId());
            stmt.setInt(2, at.getProfissionalId());
            stmt.setTimestamp(3, at.getDataHora());
            stmt.setInt(4, at.getDuracaoMin());
            stmt.setString(5, at.getTipo().name());
            stmt.setString(6, at.getSituacao().name());
            stmt.setString(7, at.getNotas());
            stmt.setBigDecimal(8, at.getValor());
            stmt.setString(9, usuarioLogado);
            stmt.setInt(10, at.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE
    // ============================
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM atendimento WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================
    // MAP ROW
    // ============================
    private Atendimento mapRow(ResultSet rs) throws SQLException {
        Atendimento at = new Atendimento();
        at.setId(rs.getInt("id"));
        at.setPacienteId(rs.getInt("paciente_id"));
        at.setPacienteNome(rs.getString("pacienteNome"));
        at.setProfissionalId(rs.getInt("profissional_id"));
        at.setProfissionalNome(rs.getString("profissionalNome"));
        at.setDataHora(rs.getTimestamp("data_hora"));
        at.setDuracaoMin(rs.getInt("duracao_min"));
        at.setTipo(Atendimento.Tipo.valueOf(rs.getString("tipo")));
        at.setSituacao(Atendimento.Situacao.valueOf(rs.getString("situacao")));
        at.setNotas(rs.getString("notas"));
        at.setValor(rs.getBigDecimal("valor"));
        at.setCriadoEm(rs.getTimestamp("criado_em"));
        at.setAtualizadoEm(rs.getTimestamp("atualizado_em"));
        at.setUsuario(rs.getString("usuario"));
        return at;
    }
}
